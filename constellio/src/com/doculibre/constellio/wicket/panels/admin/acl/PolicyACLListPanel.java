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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.acl.PolicyACL;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class PolicyACLListPanel extends SingleColumnCRUDPanel {

	public PolicyACLListPanel(String id) {
		super(id);
        
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
                AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
                RecordCollection collection = collectionAdminPanel.getCollection();
                return new ArrayList<PolicyACL>(collection.getPolicyACLs());
            }
        });
	}
    
    protected boolean isUseModalsAddEdit() {
        return false;
    }
	
	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditPolicyACLPanel(id, new PolicyACL());
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
	    PolicyACL policyACL = (PolicyACL) entityModel.getObject();
		return new AddEditPolicyACLPanel(id, policyACL);
	}

	@Override
	protected String getDetailsLabel(Object entity) {
	    PolicyACL policyACL = (PolicyACL) entity;
		return policyACL.getName();
	}

    @Override
    protected boolean isDetailsLink(IModel entityModel, int index) {
        return true;
    }

    @Override
    protected boolean isEditColumn() {
        return false;
    }

    @Override
    protected WebMarkupContainer createDetailsContent(String id, IModel entityModel) {
        PolicyACL policy = (PolicyACL) entityModel.getObject();
        return new PolicyACLEntryListPanel(id, policy);
    }

    @Override
    protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
        return ConstellioSpringUtils.getACLServices();
    }

}
