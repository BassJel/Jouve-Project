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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.acl.PolicyACL;
import com.doculibre.constellio.entities.acl.PolicyACLEntry;
import com.doculibre.constellio.services.ACLServices;
import com.doculibre.constellio.services.RecordServices;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;

@SuppressWarnings("serial")
public class AddEditPolicyACLPanel extends SaveCancelFormPanel {
	
	private ReloadableEntityModel<PolicyACL> policyACLModel;
	
	private IModel fileUploadModel = new Model();
	
	private FileUploadField fileUploadField;
	
	public AddEditPolicyACLPanel(String id, PolicyACL policyACL) {
		super(id, false);
		this.policyACLModel = new ReloadableEntityModel<PolicyACL>(policyACL);
		
		Form form = getForm();
		form.setModel(new CompoundPropertyModel(policyACLModel));
		form.add(new SetFocusBehavior(form));

		fileUploadField = new FileUploadField("file", fileUploadModel);
		fileUploadField.setRequired(true);
		form.add(fileUploadField);
	}

	@Override
	public void detachModels() {
		policyACLModel.detach();
		super.detachModels();
	}

	@Override
	protected IModel getTitleModel() {
      return new LoadableDetachableModel() {
			@Override
			protected Object load() {
		        return new StringResourceModel("add", AddEditPolicyACLPanel.this, null).getObject();
			}
      };
	}

	@Override
	protected Component newReturnComponent(String id) {
		return new PolicyACLListPanel(getId());
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
	    PolicyACL policyACL = policyACLModel.getObject();
	    
	    FileUpload fileUpload = (FileUpload) fileUploadModel.getObject();
	    InputStream aclInputStream;
        try {
            aclInputStream = fileUpload.getInputStream();
        } catch (IOException e) {
            throw new WicketRuntimeException(e);
        }
		
        ACLServices aclServices = ConstellioSpringUtils.getACLServices();
	    RecordServices recordServices = ConstellioSpringUtils.getRecordServices();

		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
        RecordCollection collection = collectionAdminPanel.getCollection();
        List<PolicyACLEntry> entries = aclServices.parse(aclInputStream, collection);
        if (!entries.isEmpty()) {
            policyACL.setName(fileUpload.getClientFileName());
            policyACL.setUploadDate(new Date());
            for (PolicyACLEntry entry : entries) {
                policyACL.addEntry(entry);
            }
            collection.addPolicyACL(policyACL);


			SolrServer solrServer = SolrCoreContext.getSolrServer(collection);
			try {
            	ConstellioPersistenceUtils.beginTransaction();                
	            recordServices.markRecordsForComputeACLEntries(collection);
	            aclServices.makePersistent(policyACL);
                try {
					solrServer.commit();
				} catch (Throwable t) {
					try {
						solrServer.rollback();
					} catch (Exception e) {
						throw new RuntimeException(t);
					}
				}
			} finally {
				ConstellioPersistenceUtils.finishTransaction(false);
			}
//            info(getLocalizer().getString("effectiveAfterIndexing", this));
        } else {
            error(getLocalizer().getString("noValidEntry", this));
        }
	}

}
