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
package com.doculibre.constellio.wicket.panels.admin.indexField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.AutocompleteServices;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.FieldTypeServices;
import com.doculibre.constellio.services.IndexFieldServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.behaviors.SetFocusBehavior;
import com.doculibre.constellio.wicket.components.locale.LocaleNameLabel;
import com.doculibre.constellio.wicket.components.locale.MultiLocaleComponentHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.fieldType.AddEditFieldTypePanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.copyField.CopyFieldListPanel;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.ConnectorInstanceMetaDTO;
import com.doculibre.constellio.wicket.panels.admin.indexField.dto.CopyFieldDTO;
import com.doculibre.constellio.wicket.panels.admin.indexField.meta.MetaListPanel;

@SuppressWarnings("serial")
public class AddEditIndexFieldPanel extends AjaxPanel {

	private ReloadableEntityModel<IndexField> indexFieldModel;
	
	private List<CopyFieldDTO> copyFieldsDest = new ArrayList<CopyFieldDTO>();
	
	private List<CopyFieldDTO> copyFieldsSource = new ArrayList<CopyFieldDTO>();
	
	private List<ConnectorInstanceMetaDTO> metas = new ArrayList<ConnectorInstanceMetaDTO>();
	
	public AddEditIndexFieldPanel(String id, IndexField indexField) {
		super(id);
		this.indexFieldModel = new ReloadableEntityModel<IndexField>(indexField);
		for (CopyField copyFieldDest : indexField.getCopyFieldsDest()) {
			copyFieldsDest.add(new CopyFieldDTO(copyFieldDest));
		}
		for (CopyField copyFieldSource : indexField.getCopyFieldsDest()) {
			copyFieldsSource.add(new CopyFieldDTO(copyFieldSource));
		}
		for (ConnectorInstanceMeta meta : indexField.getConnectorInstanceMetas()) {
			metas.add(new ConnectorInstanceMetaDTO(meta));
		}
		
		add(new FeedbackPanel("feedback"));
		
		Form form = new Form("form", new CompoundPropertyModel(indexFieldModel));
		add(form);
		form.add(new SetFocusBehavior(form));

		String titleKey = indexField.getId() == null ? "add" : "edit";
		IModel titleModel = new StringResourceModel(titleKey, this, null);
		form.add(new Label("title", titleModel));

		TextField nameField = new RequiredTextField("name");
		nameField.add(new StringValidator.MaximumLengthValidator(255));
		nameField.setEnabled(!indexField.isInternalField());
		form.add(nameField);
		
		final CheckBox indexedCheckbox = new CheckBox("indexed");
		indexedCheckbox.setEnabled(!indexField.isInternalField());
		form.add(indexedCheckbox);
		
//		final CheckBox storedCheckbox = new CheckBox("stored");
//		storedCheckbox.setEnabled(!indexField.isInternalField());
//		form.add(storedCheckbox);
		
		final CheckBox dynamicFieldCheckbox = new CheckBox("dynamicField");
		dynamicFieldCheckbox.setEnabled(!indexField.isInternalField());
		form.add(dynamicFieldCheckbox);
		
		final CheckBox multiValuedCheckbox = new CheckBox("multiValued");
		multiValuedCheckbox.setEnabled(!indexField.isInternalField());
		form.add(multiValuedCheckbox);
		
		final CheckBox sortableCheckbox = new CheckBox("sortable");
		form.add(sortableCheckbox);
		
		final CheckBox highlightedCheckbox = new CheckBox("highlighted");
		form.add(highlightedCheckbox);
		
		final ModalWindow fieldTypeModal = new ModalWindow("fieldTypeModal");
		form.add(fieldTypeModal);
		fieldTypeModal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		IModel fieldTypesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				FieldTypeServices fieldTypeServices = ConstellioSpringUtils.getFieldTypeServices();
				return fieldTypeServices.list();
			}
		};

		IChoiceRenderer fieldTypeRenderer = new ChoiceRenderer("name");

		final DropDownChoice fieldTypeField = new DropDownChoice("fieldType", fieldTypesModel,
				fieldTypeRenderer);
		form.add(fieldTypeField);
		fieldTypeField.setOutputMarkupId(true);
		fieldTypeField.setEnabled(indexField.isAnalyzingCustomizable());
		
		AjaxLink addFieldTypeLink = new AjaxLink("addFieldTypeLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				AddEditFieldTypePanel addEditFieldTypePanel = 
					new AddEditFieldTypePanel(fieldTypeModal.getContentId(), new FieldType(), fieldTypeField);
				fieldTypeModal.setContent(addEditFieldTypePanel);
				fieldTypeModal.show(target);
			}
		};
		form.add(addFieldTypeLink);
		addFieldTypeLink.setVisible(indexField.isAnalyzingCustomizable());
		
		form.add(new MetaListPanel("metas").setVisible(indexField.isAnalyzingCustomizable()));
		form.add(new CopyFieldListPanel("copyFieldsDest", true).setVisible(indexField.isAnalyzingCustomizable()));
		
		IModel localesModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return collection.getLocales();
			}
		};
		MultiLocaleComponentHolder nameHolder = 
			new MultiLocaleComponentHolder("label", indexFieldModel, localesModel) {
				@Override
				protected void onPopulateItem(ListItem item, IModel componentModel, Locale locale) {
					TextField nameField = new TextField("nameLocale", componentModel);
					item.add(nameField);
					item.add(new LocaleNameLabel("localeName", locale, true) {
						@Override
						public boolean isVisible() {
							AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
							RecordCollection collection = collectionAdminPanel.getCollection();
							return collection.getLocales().size() > 1;
						}
					});
				}
		};
		form.add(nameHolder);
		
		Button submitButton = new AjaxButton("submitButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				IndexField indexField = indexFieldModel.getObject(); 

				EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (!entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().begin();
				}

				List<ConnectorInstanceMeta> existingMetas = new ArrayList<ConnectorInstanceMeta>(indexField.getConnectorInstanceMetas());
				// Remove deleted metas
				List<ConnectorInstanceMeta> formMetas = new ArrayList<ConnectorInstanceMeta>();
				for (ConnectorInstanceMetaDTO metaDTO : getMetas()) {
					formMetas.add(metaDTO.toConnectorInstanceMeta());
				}
				for (Iterator<ConnectorInstanceMeta> it = existingMetas.iterator(); it.hasNext();) {
					ConnectorInstanceMeta meta = it.next();
					meta = entityManager.merge(meta);
					if (!hasMeta(meta, formMetas)) {
					    indexField.removeConnectorInstanceMeta(meta);
					}
				}
				// Add new
				for (ConnectorInstanceMetaDTO metaDTO : getMetas()) {
					ConnectorInstanceMeta meta = metaDTO.toConnectorInstanceMeta();
					if (!hasMeta(meta, existingMetas)) {
						meta = entityManager.merge(meta);
						indexField.addConnectorInstanceMeta(meta);
					}
				}
				
				Set<CopyField> existingCopyFieldsSource = indexField.getCopyFieldsSource();
				// Remove deleted copy fields
				for (Iterator<CopyField> it = existingCopyFieldsSource.iterator(); it.hasNext();) {
					CopyField copyField = it.next();
					if (!getCopyFieldsSource().contains(new CopyFieldDTO(copyField))) {
						it.remove();
					}
				}
				// Add new or update set
				for (CopyFieldDTO copyFieldDTO : getCopyFieldsSource()) {
					CopyField copyField = copyFieldDTO.toCopyField();
					copyField.setIndexFieldSource(indexField);
					if (copyField.getId() != null) {
						entityManager.merge(copyField);
					} else {
						existingCopyFieldsSource.add(copyField);
					}
				}
				
				Set<CopyField> existingCopyFieldsDest = indexField.getCopyFieldsDest();
				// Remove deleted copy fields
				for (Iterator<CopyField> it = existingCopyFieldsDest.iterator(); it.hasNext();) {
					CopyField copyField = it.next();
					if (!getCopyFieldsDest().contains(new CopyFieldDTO(copyField))) {
						it.remove();
					}
				}
				// Add new or update set
				for (CopyFieldDTO copyFieldDTO : getCopyFieldsDest()) {
					CopyField copyField = copyFieldDTO.toCopyField();
					copyField.setIndexFieldDest(indexField);
					if (copyField.getId() != null) {
						entityManager.merge(copyField);
					} else {
						existingCopyFieldsDest.add(copyField);
					}
				}
				
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				indexField.setRecordCollection(collection);
				
				IndexFieldServices indexFieldServices = ConstellioSpringUtils.getIndexFieldServices();
				FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
				
				if (indexField.isSortable()) {
					if (indexFieldServices.getSortFieldOf(indexField) == null) {
						indexFieldServices.newSortFieldFor(indexField);
					}
				}
				
				AutocompleteServices autocompleteServices = ConstellioSpringUtils.getAutocompleteServices();
				if (indexField.isAutocompleted()) {
					autocompleteServices.setAutoCompleteToField(indexField);
				} else {
					autocompleteServices.removeAutoCompleteFromField(indexField);
				}
				
                if (collection.isIncludedInFederation()) {
                    List<String> conflicts = new ArrayList<String>();
                    List<IndexField> newFederationFields = new ArrayList<IndexField>();
                    List<RecordCollection> ownerCollections = federationServices.listOwnerCollections(collection);
                    for (RecordCollection ownerCollection : ownerCollections) {
                        String indexFieldName = indexField.getName();
                        IndexField ownerIndexField = ownerCollection.getIndexField(indexFieldName);
                        if (ownerIndexField == null) {
                            IndexField copy = federationServices.copy(indexField, ownerCollection);
                            newFederationFields.add(copy);
                        }
                        if (federationServices.isConflict(indexFieldName, ownerCollection, collection)) {
                            Locale displayLocale = ownerCollection.getDisplayLocale(getLocale());
                            conflicts.add(ownerCollection.getTitle(displayLocale));
                        }
                    }
                    if (conflicts.isEmpty()) {
                        indexFieldServices.makePersistent(indexField);
                        for (IndexField newFederationField : newFederationFields) {
                            indexFieldServices.makePersistent(newFederationField);
                        }
                    } else {
                        for (String collectionTitle : conflicts) {
                            error(getLocalizer().getString("conflict", this) + " : " + collectionTitle);
                        }
                    }
                } else {
                    indexFieldServices.makePersistent(indexField);
                }
                
				entityManager.getTransaction().commit();
				
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);
				
				target.addComponent(modalWindow.findParent(AdminIndexFieldsPanel.class));
			}
		};
		form.add(submitButton);

		Button cancelButton = new AjaxButton("cancelButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				ModalWindow modalWindow = (ModalWindow) findParent(ModalWindow.class);
				modalWindow.close(target);
			}
		}.setDefaultFormProcessing(false);
		form.add(cancelButton);
	}

	public IndexField getIndexField() {
		return indexFieldModel.getObject();
	}

	@Override
	public void detachModels() {
		indexFieldModel.detach();
		super.detachModels();
	}

	public List<CopyFieldDTO> getCopyFieldsDest() {
		return copyFieldsDest;
	}

	public List<CopyFieldDTO> getCopyFieldsSource() {
		return copyFieldsSource;
	}

	public List<ConnectorInstanceMetaDTO> getMetas() {
		return metas;
	}
	
	private boolean hasMeta(ConnectorInstanceMeta meta, List<ConnectorInstanceMeta> metas) {
	    boolean hasMeta = false;
	    for (ConnectorInstanceMeta existingMeta : metas) {
	        if (existingMeta.getName().equals(meta.getName())) {
	            ConnectorInstance metaConnector = meta.getConnectorInstance();
	            ConnectorInstance existingConnector = existingMeta.getConnectorInstance();
	            if (metaConnector.equals(existingConnector)) {
	                hasMeta = true;
	                break;
	            }
	        }
        }
	    return hasMeta;
	}

}
