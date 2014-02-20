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
package com.doculibre.constellio.wicket.panels.admin.acl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.acl.PolicyACL;
import com.doculibre.constellio.entities.acl.PolicyACLEntry;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.SortableListModel;
import com.doculibre.constellio.wicket.panels.admin.crud.DataPanel;

@SuppressWarnings("serial")
public class PolicyACLEntryListPanel extends DataPanel<PolicyACLEntry> {
    
    private ReloadableEntityModel<PolicyACL> policyModel;

    public PolicyACLEntryListPanel(String id, PolicyACL policy) {
        super(id);
        this.policyModel = new ReloadableEntityModel<PolicyACL>(policy);
    }

    @Override
    protected SortableListModel<PolicyACLEntry> getSortableListModel() {
        return new SortableListModel<PolicyACLEntry>() {
            @Override
            protected List<PolicyACLEntry> load(String orderByProperty, Boolean orderByAsc) {
                PolicyACL policy = policyModel.getObject();
                return new ArrayList<PolicyACLEntry>(policy.getEntries());
            }
        };
    }

    @Override
    protected List<IColumn> getColumns() {
        List<IColumn> columns = new ArrayList<IColumn>();
        IColumn indexFieldColumn = new AbstractColumn(new StringResourceModel("indexField", this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                PolicyACLEntry entry = (PolicyACLEntry) rowModel.getObject();
                IndexField indexField = entry.getIndexField();
                cellItem.add(new Label(componentId, indexField.getName()));
            }
        };
        IColumn regexpColumn = new PropertyColumn(new StringResourceModel("matchRegexp", this, null), "matchRegexp");
        IColumn usersColumn = new AbstractColumn(new StringResourceModel("users", this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                PolicyACLEntry entry = (PolicyACLEntry) rowModel.getObject();
                StringBuffer usersSB = new StringBuffer();
                Set<ConstellioUser> users = entry.getUsers();
                for (ConstellioUser user : users) {
                    usersSB.append(user.getUsername());
                    usersSB.append("\n");
                }
                cellItem.add(new MultiLineLabel(componentId, usersSB.toString()));
            }
        };
        IColumn groupsColumn = new AbstractColumn(new StringResourceModel("groups", this, null)) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                PolicyACLEntry entry = (PolicyACLEntry) rowModel.getObject();
                StringBuffer groupsSB = new StringBuffer();
                Set<ConstellioGroup> groups = entry.getGroups();
                for (ConstellioGroup group : groups) {
                    groupsSB.append(group.getName());
                    groupsSB.append("\n");
                }
                cellItem.add(new MultiLineLabel(componentId, groupsSB.toString()));
            }
        };
        columns.add(indexFieldColumn);
        columns.add(regexpColumn);
        columns.add(usersColumn);
        columns.add(groupsColumn);
        return columns;
    }

    @Override
    public void detachModels() {
        policyModel.detach();
        super.detachModels();
    }

}
