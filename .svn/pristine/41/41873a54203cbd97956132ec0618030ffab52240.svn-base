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
package com.doculibre.constellio.entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.util.crypt.Base64;
import org.apache.wicket.util.crypt.Base64UrlSafe;

@SuppressWarnings("serial")
@Entity
public class ConstellioUser extends BaseConstellioEntity {

    public static final String ROLE_COLLABORATOR = "COLLABORATOR";

    private String passwordHash;

    private String username;

    private String firstName;

    private String lastName;

    private Locale locale;

    private String localeCode;
    
    private String domain;

    private Set<String> roles = new HashSet<String>(1);

    private Set<GroupParticipation> participations = new HashSet<GroupParticipation>();

    private Set<CollectionPermission> collectionPermissions = new HashSet<CollectionPermission>();
    
    private Set<UserCredentials> userCredentials = new HashSet<UserCredentials>();

    public ConstellioUser() {
        super();
    }

    public ConstellioUser(String username, String password, Locale locale) {
        super();
        this.username = username;
        if (password == null) {
            password = "";
        }
        this.passwordHash = getHash(password);
        setLocale(locale);
        roles.add(Roles.USER);
    }

    @Column(unique = true, nullable = false)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Transient
    public Locale getLocale() {
        if (locale == null && localeCode != null) {
            locale = parseLocale(localeCode);
        }
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.setLocaleCode(locale.getLanguage());
    }

    @Column(length = 5, nullable = false)
    public String getLocaleCode() {
        return this.localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
        this.locale = parseLocale(localeCode);
    }

    private static Locale parseLocale(String localeCode) {
        Locale locale;
        StringTokenizer st = new StringTokenizer(localeCode, "_");
        String language = st.nextToken();
        if (st.hasMoreTokens()) {
            locale = new Locale(language, st.nextToken());
        } else {
            locale = new Locale(language);
        }
        return locale;
    }

    @Column(length = 28, nullable = false)
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    
    @Column(length = 50)
    public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
     * Password is not retained, but this method satisfies some bean utils.
     * 
     * @return always null
     */
    public String getPassword() {
        return null;
    }

    /**
     * @param password
     *            new password for user
     */
    public void setPassword(String password) {
        if (password == null) {
            password = "";
        }
        passwordHash = getHash(password);
    }

    /**
     * Performs hash on given password and compares it to the correct hash.
     * 
     * @return true if hashed password is correct
     */
    public boolean checkPassword(String password) {
        return passwordHash.equals(getHash(password));
    }

    /**
     * @return true if validRoles and this user have any
     *         roles in common
     */
    public boolean hasAnyRole(Roles validRoles) {
        for (String role : roles)
            if (validRoles.hasRole(role))
                return true;
        return false;
    }

    public boolean hasAnyRole(String roles) {
        return hasAnyRole(new Roles(roles));
    }

    @ElementCollection
    @CollectionTable(name = "ConstellioUser_Roles", joinColumns = @JoinColumn(name = "constellioUser_id"))
    @Column(name = "role")
    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public boolean isAdmin() {
        return hasAnyRole(Roles.ADMIN);
    }

    public void setAdmin(boolean admin) {
        if (admin && !isAdmin()) {
            roles.add(Roles.ADMIN);
        } else if (!admin && isAdmin()) {
            roles.remove(Roles.ADMIN);
        }
    }

    public boolean isCollaborator() {
        return hasAnyRole(ROLE_COLLABORATOR);
    }

    public void setCollaborator(boolean collaborator) {
        if (collaborator && !isCollaborator()) {
            roles.add(ROLE_COLLABORATOR);
        } else if (!collaborator && isCollaborator()) {
            roles.remove(ROLE_COLLABORATOR);
        }
    }

    public boolean hasSearchPermission(RecordCollection collection) {
        boolean hasPermission = false;
        if (hasAdminPermission(collection) || hasCollaborationPermission(collection)) {
            hasPermission = true;
        } else if (collection.getCollectionPermissions().isEmpty()) {
            hasPermission = true;
        } else {
            for (CollectionPermission collectionPermission : getCollectionPermissions()) {
                if (collectionPermission.getRecordCollection().equals(collection)
                    && collectionPermission.isSearch()) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                for (GroupParticipation participation : getParticipations()) {
                    ConstellioGroup group = participation.getConstellioGroup();
                    for (CollectionPermission collectionPermission : group.getCollectionPermissions()) {
                        if (collectionPermission.getRecordCollection().equals(collection)
                            && collectionPermission.isSearch()) {
                            hasPermission = true;
                            break;
                        }
                    }
                }
            }
        }
        return hasPermission;
    }

    public boolean hasCollaborationPermission(RecordCollection collection) {
        boolean hasPermission = false;
        if (hasAdminPermission(collection)) {
            hasPermission = true;
        } else if (isCollaborator() && collection.getCollectionPermissions().isEmpty()) {
            hasPermission = true;
        } else {
            for (CollectionPermission collectionPermission : getCollectionPermissions()) {
                if (collectionPermission.getRecordCollection().equals(collection)
                    && collectionPermission.isCollaboration()) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                for (GroupParticipation participation : getParticipations()) {
                    ConstellioGroup group = participation.getConstellioGroup();
                    for (CollectionPermission collectionPermission : group.getCollectionPermissions()) {
                        if (collectionPermission.getRecordCollection().equals(collection)
                            && collectionPermission.isCollaboration()) {
                            hasPermission = true;
                            break;
                        }
                    }
                }
            }
        }
        return hasPermission;
    }

    public boolean hasAdminPermission(RecordCollection collection) {
        boolean hasPermission = false;
        if (isAdmin()) {
            hasPermission = true;
        } else {
            for (CollectionPermission collectionPermission : getCollectionPermissions()) {
                if (collectionPermission.getRecordCollection().equals(collection)
                    && collectionPermission.isAdmin()) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                for (GroupParticipation participation : getParticipations()) {
                    ConstellioGroup group = participation.getConstellioGroup();
                    for (CollectionPermission collectionPermission : group.getCollectionPermissions()) {
                        if (collectionPermission.getRecordCollection().equals(collection)
                            && collectionPermission.isAdmin()) {
                            hasPermission = true;
                            break;
                        }
                    }
                }
            }
        }
        return hasPermission;
    }

    /**
     * @return username
     */
    @Override
    public String toString() {
        return username;
    }

    @OneToMany(mappedBy = "constellioUser", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<GroupParticipation> getParticipations() {
        return participations;
    }

    public void setParticipations(Set<GroupParticipation> participations) {
        this.participations = participations;
    }

    public void addParticipation(GroupParticipation participation) {
        this.participations.add(participation);
        participation.setConstellioUser(this);
    }

    @OneToMany(mappedBy = "constellioUser", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<CollectionPermission> getCollectionPermissions() {
        return collectionPermissions;
    }

    public void setCollectionPermissions(Set<CollectionPermission> collectionPermissions) {
        this.collectionPermissions = collectionPermissions;
    }

    public void addCollectionPermission(CollectionPermission collectionPermission) {
        this.collectionPermissions.add(collectionPermission);
        collectionPermission.setConstellioUser(this);
    }

    @OneToMany(mappedBy = "user", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<UserCredentials> getUserCredentials() {
        return userCredentials;
    }

    public void setUserCredentials(Set<UserCredentials> userCredentials) {
        this.userCredentials = userCredentials;
    }
    
    public void addUserCredentials(UserCredentials credentials) {
        this.userCredentials.add(credentials);
        credentials.setUser(this);
    }
    
    public UserCredentials getUserCredentials(CredentialGroup credentialGroup) {
        UserCredentials match = null;
        for (UserCredentials credentials : userCredentials) {
            if (credentials.getCredentialGroup().equals(credentialGroup)) {
                match = credentials;
                break;
            }
        }
        return match;
    }

    /**
     * @param location
     *            IP address or other identifier
     * @return restricted token as URL-safe hash for user, password, and location parameter
     */
    @Transient
    public String getToken(String location) {
        MessageDigest md = getMessageDigest();
        md.update(getSalt());
        md.update(getPasswordHash().getBytes());
        md.update(location.getBytes());
        byte[] hash = md.digest(getUsername().getBytes());
        return new String(Base64UrlSafe.encodeBase64(hash));
    }

    /**
     * Generates a hash for password using salt from IAuthSettings.getSalt()
     * and returns the hash encoded as a Base64 String.
     * 
     * @see IAuthSettings#getSalt()
     * @param password
     *            to encode
     * @return base64 encoded SHA hash, 28 characters
     */
    public static String getHash(String password) {
        MessageDigest md = getMessageDigest();
        md.update(getSalt());
        byte[] hash = md.digest(password.getBytes());
        // using a Base64 string for the hash because putting a
        // byte[] into a blob isn't working consistently.
        return new String(Base64.encodeBase64(hash));
    }

    protected static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA Hash algorithm not found.", e);
        }
    }

    private static byte[] getSalt() {
        return "constellio".getBytes();
    }

}
