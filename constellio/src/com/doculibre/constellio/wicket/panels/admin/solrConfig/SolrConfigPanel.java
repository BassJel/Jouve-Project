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
package com.doculibre.constellio.wicket.panels.admin.solrConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.PropertyResolver;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.SolrConfigServices;
import com.doculibre.constellio.services.SolrServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.components.holders.CheckBoxHolder;
import com.doculibre.constellio.wicket.components.holders.ModalImgLinkHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.pages.BaseConstellioPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.tabs.AdminTopMenuPanel;

/**
 * 
 * @author francisbaril
 * 
 */
@SuppressWarnings("serial")
public class SolrConfigPanel extends AjaxPanel {

	public static int MODAL_HEIGHT = 500;
	public static int MODAL_WIDTH = 800;
	public static String CSS_MODAL = ModalWindow.CSS_CLASS_GRAY;

	private ReloadableEntityModel<SolrConfig> solrConfigModel;

	public SolrConfigPanel(String id) {
		super(id);
		
		setSolrConfigModel();
        
		Form form = new Form("form");

		Link editDefaultConfigLink = new Link("editDefaultConfig") {

			@Override
			public void onClick() {
				AdminTopMenuPanel topMenu = (AdminTopMenuPanel) findParent(AdminTopMenuPanel.class);
				topMenu.goToServerTab(10);
			}
			
			@Override
			public boolean isVisible() {
				return solrConfigModel.getObject().getRecordCollection() != null;
			}
		};
		
		Link synchronizeSolrconfigLink = new Link("synchronizeSolrconfigLink") {

			@Override
			public void onClick() {
				synchronize();
			}
		};
		
		
		List<PropertyRow> propertyRows = new ArrayList<PropertyRow>();
		propertyRows.add(new CachePropertyRow("filterCacheConfig"));
		propertyRows.add(new CachePropertyRow("queryResultCacheConfig"));
		propertyRows.add(new CachePropertyRow("documentCacheConfig"));
		propertyRows.add(new CachePropertyRow("fieldValueCacheConfig"));
		propertyRows.add(new PropertyRow("useFilterForSortedQuery"));
		propertyRows.add(new PropertyRow("queryResultWindowSize"));
		propertyRows.add(new PropertyRow("hashDocSetMaxSize"));
		propertyRows.add(new PropertyRow("hashDocSetLoadFactor"));

		form.add(getPropertiesListView(propertyRows));
		form.add(editDefaultConfigLink);
		form.add(synchronizeSolrconfigLink);
		this.add(form);
	}

	private ListView getPropertiesListView(List<PropertyRow> propertyRows) {
		return new ListView("properties", propertyRows) {

			@Override
			protected void populateItem(final ListItem item) {

				final PropertyRow propertyRow = (PropertyRow) item
						.getModelObject();
				item.setOutputMarkupId(true);
				item.add(new Label("property",
						new StringResourceModel(propertyRow.getMessageKey(),
								SolrConfigPanel.this, null)));
				IModel stateModel = new LoadableDetachableModel() {

					protected Object load() {
						SolrConfig config = solrConfigModel.getObject();
						boolean isSet = PropertyResolver.getValue(
								propertyRow.getProperty(), config) != null;
						return new StringResourceModel(isSet ? "yes" : "no",
								SolrConfigPanel.this, null).getObject();
					}

				};
				item.add(new Label("state", stateModel));
				AjaxLink defaultValueLink = new AjaxLink("defaultValue") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						SolrConfig config = solrConfigModel.getObject();
						PropertyResolver.setValue(propertyRow.getProperty(),
								config, null, null);
						target.addComponent(item);
						save();
					}

					@Override
					public boolean isVisible() {
						SolrConfig config = solrConfigModel.getObject();
						return PropertyResolver.getValue(
								propertyRow.getProperty(), config) != null;
					}
				};
				item.add(defaultValueLink);
				IModel titleLabelModel = new LoadableDetachableModel() {
					
					@Override
					protected Object load() {
						boolean isServerConfig = solrConfigModel.getObject().getRecordCollection() == null;
						String resourceKey = isServerConfig ? "defaultValue" : "defaultServerValue";
						return new StringResourceModel(resourceKey, SolrConfigPanel.this, null).getObject();
					}
				};
				defaultValueLink.add(new Label("label", titleLabelModel));

				if (propertyRow.isModalEdit()) {
					item.add(new ModalImgLinkHolder("editComponent") {
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink(id) {
								@Override
								public void onClick(AjaxRequestTarget target) {
									ModalWindow editModal = getModalWindow();
									editModal.setInitialHeight(MODAL_HEIGHT);
									editModal.setInitialWidth(MODAL_WIDTH);
									String editMsg = getLocalizer().getString(
											"edit", SolrConfigPanel.this);
									editModal.setTitle(editMsg);

									WebMarkupContainer editContent = propertyRow
											.getEditLinkPanel(editModal
													.getContentId());
									editModal.setContent(editContent);
									editModal.show(target);
									target.addComponent(item);
								}
							};
						}

						@Override
						protected Component newImg(String id) {
							return new NonCachingImage(id,
									new ResourceReference(
											BaseConstellioPage.class,
											"images/ico_crayon.png"));
						}
					});
				} else if (propertyRow.getType().equals(Boolean.class)){
					final IModel valueModel = propertyRow.createModel();
					item.add(new CheckBoxHolder("editComponent") {
						
						
						@Override
						protected WebMarkupContainer newInput(String id) {
							return new AjaxCheckBox(id, valueModel) {
								
								@Override
								protected void onUpdate(AjaxRequestTarget target) {
									save();
									target.addComponent(item);
								}

								@Override
								public void detachModels() {
									super.detachModels();
									valueModel.detach();
								}
								
							};
						}
					});
				} else {
					final IModel valueModel = propertyRow.createModel();
					item.add(new AjaxEditableLabel("editComponent", valueModel) {

						@Override
						protected void onSubmit(AjaxRequestTarget target) {
							super.onSubmit(target);
							save();
							target.addComponent(item);
						}

						@Override
						public void detachModels() {
							super.detachModels();
							valueModel.detach();
						}
						
					});
				}

			}

		};
	}

	/**
	 * Set the "solrConfigModel" and "isDefaultConfig" attributes
	 */
	private void setSolrConfigModel() {
		final IModel solrConfigRetrievalModel = new LoadableDetachableModel() {

			protected Object load() {
				SolrConfig config;
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);

				if (collectionAdminPanel != null) {
					RecordCollection collection = collectionAdminPanel
							.getCollection();
					if (collection.getSolrConfiguration() == null) {
						SolrConfigServices solrConfigServices = ConstellioSpringUtils
								.getSolrConfigServices();
						SolrConfig defaultConfig = solrConfigServices
								.getDefaultConfig();
						try {
							config = (SolrConfig) defaultConfig.clone();
							if (config.getId() != null) {
								throw new RuntimeException("Id must be null");
							}
						} catch (CloneNotSupportedException e) {
							throw new RuntimeException(e);
						}
					} else {
						config = collection.getSolrConfiguration();
					}
				} else {
					EntityManager entityManager = ConstellioPersistenceContext
							.getCurrentEntityManager();
					if (!entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().begin();
					}
					SolrConfigServices solrConfigServices = ConstellioSpringUtils
							.getSolrConfigServices();
					config = solrConfigServices.getDefaultConfig();
					entityManager.getTransaction().commit();
				}
				return config;
			}

		};
		solrConfigModel = new ReloadableEntityModel<SolrConfig>(
				solrConfigRetrievalModel) {

		};
	}

	private void synchronize() {
		SolrServices solrServices = ConstellioSpringUtils.getSolrServices();
		solrServices.updateConfig();
		if (solrConfigModel.getObject().getRecordCollection() != null) {
			solrServices.initCore(solrConfigModel.getObject().getRecordCollection());
		} else {
			RecordCollectionServices recordCollectionServices = ConstellioSpringUtils.getRecordCollectionServices();
			for(RecordCollection collection : recordCollectionServices.list()) {
				solrServices.initCore(collection);
			}
		}
		
		
	}

	/**
	 * Save the solr configuration
	 */
	private void save() {
		EntityManager entityManager = ConstellioPersistenceContext
				.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		SolrConfig config = solrConfigModel.getObject();
		SolrConfigServices solrConfigServices = ConstellioSpringUtils
				.getSolrConfigServices();

		solrConfigServices.makePersistent(config);
		entityManager.getTransaction().commit();
	}

	@Override
	public void detachModels() {
		solrConfigModel.detach();
		super.detachModels();
	}

	/**
	 * A property row for cache
	 * 
	 * @author francisbaril
	 * 
	 */
	protected class CachePropertyRow extends PropertyRow {

		CachePropertyRow(String property) {
			super(property, true);
		}

		@Override
		public Panel getEditLinkPanel(String id) {
			return new AddEditCachePanel(id, solrConfigModel, getProperty());
		}

	}

	/**
	 * Implementations of this class will be converted by a form's line.
	 * 
	 * @author francisbaril
	 * 
	 */
	protected class PropertyRow implements Serializable {

		private String messageKey;
		private String property;
		private Object defaultValue = null;
		private boolean modalEdit = false;

		PropertyRow(String messageKey, String property) {
			this.messageKey = messageKey;
			this.property = property;
		}

		PropertyRow(String property) {
			this.messageKey = property;
			this.property = property;
		}

		PropertyRow(String messageKey, String property, Object defaultValue) {
			this.messageKey = messageKey;
			this.property = property;
			this.defaultValue = defaultValue;
		}

		PropertyRow(String property, Object defaultValue) {
			this.messageKey = property;
			this.property = property;
			this.defaultValue = defaultValue;
		}

		PropertyRow(String messageKey, String property, boolean modalEdit) {
			this.messageKey = messageKey;
			this.property = property;
			this.modalEdit = modalEdit;
		}

		PropertyRow(String property, boolean modalEdit) {
			this.messageKey = property;
			this.property = property;
			this.modalEdit = modalEdit;
		}

		PropertyRow(String messageKey, String property, Object defaultValue,
				boolean modalEdit) {
			this.messageKey = messageKey;
			this.property = property;
			this.defaultValue = defaultValue;
			this.modalEdit = modalEdit;
		}

		PropertyRow(String property, Object defaultValue, boolean modalEdit) {
			this.messageKey = property;
			this.property = property;
			this.defaultValue = defaultValue;
			this.modalEdit = modalEdit;
		}

		public Component getEditComponent(String id) {
			return new WebMarkupContainer(id).setVisible(false);
		}

		public WebMarkupContainer getEditLinkPanel(String id) {
			return null;
		}

		public String getMessageKey() {
			return messageKey;
		}

		public String getProperty() {
			return property;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public boolean isModalEdit() {
			return modalEdit;
		}

		public Class<?> getType() {
			SolrConfig config = solrConfigModel.getObject();
			return PropertyResolver.getPropertyField(getProperty(), config).getType();
		}
		
		public IModel createModel() {
			return new IModel() {

				@Override
				public void detach() {
				}

				@Override
				public Object getObject() {
					SolrConfig config = solrConfigModel.getObject();
					return PropertyResolver.getValue(getProperty(), config);
				}

				@Override
				public void setObject(Object object) {
					SolrConfig config = solrConfigModel.getObject();
					PropertyResolver.setValue(getProperty(), config, object,
							null);
				}

			};
		}

	}
}
