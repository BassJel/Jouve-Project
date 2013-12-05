/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.doculibre.constellio.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.GroupParticipation;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.Record;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.acl.PolicyACL;
import com.doculibre.constellio.entities.acl.PolicyACLEntry;
import com.doculibre.constellio.entities.acl.RecordPolicyACLEntry;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class ACLServicesImpl extends BaseCRUDServicesImpl<PolicyACL> implements ACLServices {

    private static final Logger LOGGER = Logger.getLogger(ACLServicesImpl.class.getName());

    public ACLServicesImpl(EntityManager entityManager) {
        super(PolicyACL.class, entityManager);
    }

	@Override
	public PolicyACLEntry getEntry(Long id) {
        return (PolicyACLEntry) getEntityManager().find(PolicyACLEntry.class, id);
	}

    @Override
    public List<PolicyACLEntry> computeACLEntries(Record record) {
        List<PolicyACLEntry> matchingEntries = new ArrayList<PolicyACLEntry>();
        ConnectorInstance connectorInstance = record.getConnectorInstance();
        RecordCollection collection = connectorInstance.getRecordCollection();
        for (PolicyACL policyACL : collection.getPolicyACLs()) {
            for (PolicyACLEntry entry : policyACL.getEntries()) {
                if (matches(record, entry, null, null)) {
                    matchingEntries.add(entry);
                }
            }
        }
        return matchingEntries;
    }

    @Override
    public boolean hasACLPermission(Record record, ConstellioUser user) {
        boolean hasACLPermission = false;
        List<ConstellioGroup> groups = new ArrayList<ConstellioGroup>();
        for (GroupParticipation participation : user.getParticipations()) {
            ConstellioGroup userGroup = participation.getConstellioGroup();
            groups.add(userGroup);
        }
        List<ConstellioUser> users = new ArrayList<ConstellioUser>();
        users.add(user);
        for (RecordPolicyACLEntry recordEntry : record.getRecordPolicyACLEntries()) {
            PolicyACLEntry entry = recordEntry.getEntry();
            if (matches(record, entry, groups, users)) {
                hasACLPermission = true;
                break;
            }
        }
        return hasACLPermission;
    }

    @Override
    public List<Record> removeAuthorizedRecords(List<Record> privateRecords, ConstellioUser user) {
        List<Record> unevaluatedPrivateRecords = new ArrayList<Record>(privateRecords);
        // Remove ACL authorized records
        for (Iterator<Record> it = unevaluatedPrivateRecords.iterator(); it.hasNext();) {
            Record privateRecord = it.next();
            if (hasACLPermission(privateRecord, user)) {
                it.remove();
            }
        }
        return unevaluatedPrivateRecords;
    }

    @Override
    public List<PolicyACLEntry> parse(InputStream aclInputStream, RecordCollection collection) {
        List<PolicyACLEntry> entries = new ArrayList<PolicyACLEntry>();
        List<String> aclLines;
        try {
            aclLines = IOUtils.readLines(aclInputStream);
            IOUtils.closeQuietly(aclInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String aclLine : aclLines) {
            try {
                String fieldName = null;
                String regexp = null;
                Set<String> userNames = new HashSet<String>();
                Set<String> groupNames = new HashSet<String>();
                StringTokenizer aclTokens = new StringTokenizer(aclLine, " ");
                while (aclTokens.hasMoreTokens()) {
                    String aclToken = aclTokens.nextToken();
                    int indexOfColon = aclToken.indexOf(":");
                    if (indexOfColon != -1) {
                        String fieldOrGroupOrUserName = aclToken.substring(0, indexOfColon);
                        String fieldOrGroupOrUserValue = aclToken.substring(indexOfColon + 1);
                        if (StringUtils.isNotBlank(fieldOrGroupOrUserValue)) {
                            if ("group".equalsIgnoreCase(fieldOrGroupOrUserName)) {
                                groupNames.add(fieldOrGroupOrUserValue);
                            } else if ("user".equalsIgnoreCase(fieldOrGroupOrUserName)) {
                                userNames.add(fieldOrGroupOrUserValue);
                            } else {
                                fieldName = fieldOrGroupOrUserName;
                                regexp = fieldOrGroupOrUserValue;
                            }
                        } else {
                            // Skip token
                            LOGGER.log(Level.SEVERE, "Invalid ACL token skipped : " + aclToken);
                            continue;
                        }
                    } else {
                        fieldName = IndexField.URL_FIELD;
                        regexp = aclToken;
                    }
                }

                if (fieldName != null && regexp != null && (!userNames.isEmpty() || !groupNames.isEmpty())) {
                    IndexField indexField = collection.getIndexField(fieldName);
                    if (indexField == null) {
                        // A URL will probably start with http: or https: which will have been misinterpreted
                        // as a field name
                        indexField = collection.getIndexField(IndexField.URL_FIELD);
                        regexp = fieldName + ":" + regexp;
                    }
                    Set<ConstellioUser> users = new HashSet<ConstellioUser>();
                    Set<ConstellioGroup> groups = new HashSet<ConstellioGroup>();
                    UserServices userServices = ConstellioSpringUtils.getUserServices();
                    GroupServices groupServices = ConstellioSpringUtils.getGroupServices();
                    for (String userName : userNames) {
                        ConstellioUser user = userServices.get(userName);
                        if (user != null) {
                            users.add(user);
                        } else {
                            // Skip line
                            LOGGER.log(Level.SEVERE, "Invalid username skipped : " + userName);
                        }
                    }
                    for (String groupName : groupNames) {
                        ConstellioGroup group = groupServices.get(groupName);
                        if (group != null) {
                            groups.add(group);
                        } else {
                            // Skip line
                            LOGGER.log(Level.SEVERE, "Invalid group name skipped : " + groupName);
                        }
                    }
                    if (!users.isEmpty() || !groups.isEmpty()) {
                        PolicyACLEntry entry = new PolicyACLEntry();
                        entry.setIndexField(indexField);
                        entry.setMatchRegexp(regexp);
                        entry.setGroups(groups);
                        entry.setUsers(users);
                        entries.add(entry);
                    } else {
                        // Skip line
                        LOGGER.log(Level.SEVERE, "Invalid ACL line skipped : " + aclLine);
                    }
                } else {
                    // Skip line
                    LOGGER.log(Level.SEVERE, "Invalid ACL line skipped : " + aclLine);
                }
            } catch (Exception e) {
                // Skip line
                LOGGER.log(Level.SEVERE, "Exception while parsing line of ACL input stream", e);
            }
        }
        return entries;
    }

    private boolean matches(Record record, PolicyACLEntry entry, Collection<ConstellioGroup> groups,
        Collection<ConstellioUser> users) {
        boolean match = false;

        IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
        IndexField indexField = entry.getIndexField();
        String regexp = entry.getMatchRegexp();

        List<Object> fieldValues = indexFieldServices.extractFieldValues(record, indexField);
        for (Object fieldValue : fieldValues) {
            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fieldValue.toString());
            if (matcher.find()) {
                if (groups != null || users != null) {
                    if (groups != null && CollectionUtils.containsAny(entry.getGroups(), groups)) {
                        match = true;
                        break;
                    } else if (users != null && CollectionUtils.containsAny(entry.getUsers(), users)) {
                        match = true;
                        break;
                    }
                } else {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

}
