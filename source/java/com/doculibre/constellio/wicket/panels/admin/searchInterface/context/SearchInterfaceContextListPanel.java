package com.doculibre.constellio.wicket.panels.admin.searchInterface.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContext;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.services.SearchInterfaceContextServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.context.params.SearchInterfaceContextParamListPanel;

@SuppressWarnings("serial")
public class SearchInterfaceContextListPanel extends SingleColumnCRUDPanel {
	
	private ReloadableEntityModel<SearchInterfaceContext> parentContextModel;

	public SearchInterfaceContextListPanel(String id) {
		this(id, null);
	}

	public SearchInterfaceContextListPanel(String id, SearchInterfaceContext parentContext) {
		super(id);
		if (parentContext != null) {
			parentContextModel = new ReloadableEntityModel<SearchInterfaceContext>(parentContext);
		}
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
            	List<SearchInterfaceContext> contexts;
            	SearchInterfaceContextServices contextServices = ConstellioSpringUtils.getSearchInterfaceContextServices();
            	if (parentContextModel != null) {
            		SearchInterfaceContext parentContext = parentContextModel.getObject();
            		contexts = new ArrayList<SearchInterfaceContext>(parentContext.getSubContexts());
            	} else {
            		contexts = contextServices.listFirstLevel();
            	}
            	return contexts;
            }
        });
	}

	@Override
	public void detachModels() {
		if (parentContextModel != null) {
			parentContextModel.detach();
		}
		super.detachModels();
	}

	@Override
	protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();
        
        dataColumns.add(new AbstractColumn(new Model()) {
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				SearchInterfaceContext context = (SearchInterfaceContext) rowModel.getObject();
				if (context.isExternalFiles()) {
					cellItem.add(new Label(componentId, ""));
				} else {
					final ReloadableEntityModel<SearchInterfaceContext> contextModel = 
							new ReloadableEntityModel<SearchInterfaceContext>(context);
					cellItem.add(new LinkHolder(componentId, new StringResourceModel("subContexts", null)) {
						@Override
						protected WebMarkupContainer newLink(String id) {
							return new Link(id) {
								@Override
								public void onClick() {
									SearchInterfaceContext context = contextModel.getObject();
									String panelId = SearchInterfaceContextListPanel.this.getId();
									SearchInterfaceContextListPanel.this.replaceWith(new SearchInterfaceContextListPanel(panelId, context));
								}
							};
						}

						@Override
						public void detachModels() {
							contextModel.detach();
							super.detachModels();
						}
					});
				}
			}
		});
        
        dataColumns.add(new HeaderlessColumn() {
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				SearchInterfaceContext context = (SearchInterfaceContext) rowModel.getObject();
				final ReloadableEntityModel<SearchInterfaceContext> contextModel = 
						new ReloadableEntityModel<SearchInterfaceContext>(context);
				if (context.isExternalFiles()) {
					cellItem.add(new Label(componentId, ""));
				} else {
					cellItem.add(new LinkHolder(componentId, new StringResourceModel("contextParams", null)) {
						@Override
						protected WebMarkupContainer newLink(String id) {
							return new Link(id) {
								@Override
								public void onClick() {
									String panelId = SearchInterfaceContextListPanel.this.getId();
									SearchInterfaceContext context = contextModel.getObject();
									SearchInterfaceContextParamListPanel replacement = 
											new SearchInterfaceContextParamListPanel(panelId, context);
									SearchInterfaceContextListPanel.this.replaceWith(replacement);
								}
							};
						}
	
						@Override
						public void detachModels() {
							contextModel.detach();
							super.detachModels();
						}
					});
				}	
			}
        });	
		
        return dataColumns;
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		SearchInterfaceContext context = (SearchInterfaceContext) entity;
		return context.getContextName();
	}

	@Override
	protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
		return ConstellioSpringUtils.getSearchInterfaceContextServices();
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		SearchInterfaceContext parentContext;
		if (parentContextModel != null) {
			parentContext = parentContextModel.getObject();
		} else {
			parentContext = null;
		}
		SearchInterfaceContext context = new SearchInterfaceContext();
		context.setParentContext(parentContext);
		return new AddEditSearchInterfaceContextPanel(id, context);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel rowItemModel, int index) {
		SearchInterfaceContext context = (SearchInterfaceContext) rowItemModel.getObject();
		return new AddEditSearchInterfaceContextPanel(id, context);
	}

	@Override
	protected IModel getTitleModel() {
		IModel titleModel;
		if (parentContextModel != null) {
			SearchInterfaceContext parentContext = parentContextModel.getObject();
			titleModel = new StringResourceModel("panelTitleSubContexts", null, new String[] { parentContext.getContextName() });
		} else {
			titleModel = new StringResourceModel("panelTitle", null);
		}
		return titleModel;
	}

    @Override
    protected boolean isUseModals() {
        return false;
    }

	@Override
	public Component createDetailsColumnHeader(String componentId) {
		IModel titleModel = new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String title;
				if (parentContextModel != null) {
					SearchInterfaceContext parentContext = parentContextModel.getObject();
					String titleKey;
					if (parentContext.getParentContext() != null) {
						titleKey = "parentContext";
					} else {
						titleKey = "rootContexts";
					}
					title = getLocalizer().getString(titleKey, SearchInterfaceContextListPanel.this);
				} else {
					title = "";
				}
				return title;
			}
		};
		return new LinkHolder(componentId, titleModel) {
			@Override
			protected WebMarkupContainer newLink(String id) {
				return new Link(id) {
					@Override
					public void onClick() {
						String panelId = SearchInterfaceContextListPanel.this.getId();
						SearchInterfaceContext parentContext = parentContextModel.getObject();
						SearchInterfaceContextListPanel replacement;
						if (parentContext.getParentContext() != null) {
							replacement = new SearchInterfaceContextListPanel(panelId, parentContext.getParentContext());
						} else {
							replacement = new SearchInterfaceContextListPanel(panelId);
						}
						SearchInterfaceContextListPanel.this.replaceWith(replacement);
					}
				};
			}
			
			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					visible = parentContextModel != null;
				}
				return visible;
			}
		};
	}

}
