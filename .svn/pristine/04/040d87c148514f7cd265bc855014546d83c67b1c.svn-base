package com.doculibre.constellio.wicket.panels.admin.searchInterface.context.params;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContext;
import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContextParam;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.components.holders.ImgHolder;
import com.doculibre.constellio.wicket.components.holders.LinkHolder;
import com.doculibre.constellio.wicket.components.holders.ModalLinkHolder;
import com.doculibre.constellio.wicket.components.links.DownloadInputStreamLink;
import com.doculibre.constellio.wicket.components.searchInterfaceContext.SearchInterfaceContextParamResourceReference;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;
import com.doculibre.constellio.wicket.panels.admin.searchInterface.context.SearchInterfaceContextListPanel;

@SuppressWarnings("serial")
public class SearchInterfaceContextParamListPanel extends SingleColumnCRUDPanel {
	
	private ReloadableEntityModel<SearchInterfaceContext> contextModel;

	public SearchInterfaceContextParamListPanel(String id, SearchInterfaceContext context) {
		super(id);
		if (context != null) {
			contextModel = new ReloadableEntityModel<SearchInterfaceContext>(context);
		}
        setModel(new LoadableDetachableModel() {
            @Override
            protected Object load() {
            	SearchInterfaceContext context = contextModel.getObject();
            	List<SearchInterfaceContextParam> contextParams = new ArrayList<SearchInterfaceContextParam>();
            	contextParams.addAll(context.getEffectiveContextParams());
            	Collections.sort(contextParams, new Comparator<SearchInterfaceContextParam>() {
					@Override
					public int compare(SearchInterfaceContextParam o1, SearchInterfaceContextParam o2) {
						SearchInterfaceContext context = contextModel.getObject();
						Boolean o1Inherited = context.isInheritedContextParam(o1.getParamName());
						Boolean o2Inherited = context.isInheritedContextParam(o2.getParamName());
						return o1Inherited.compareTo(o2Inherited);
					}
            	});
            	return contextParams;
            }
        });
	}

	@Override
	public void detachModels() {
		if (contextModel != null) {
			contextModel.detach();
		}
		super.detachModels();
	}

	@Override
	protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();

        dataColumns.add(new AbstractColumn(new StringResourceModel("value", null)) {
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) rowModel.getObject();
				final ReloadableEntityModel<SearchInterfaceContextParam> contextParamModel = 
						new ReloadableEntityModel<SearchInterfaceContextParam>(contextParam);
				if (contextParam.isTextParam()) {
					cellItem.add(new Label(componentId, contextParam.getTextValue()) {
						@Override
						public void detachModels() {
							contextParamModel.detach();
							super.detachModels();
						}
					});
				} else {
					if (contextParam.isImgParam()) {
						cellItem.add(new ImgHolder(componentId) {
							@Override
							protected Image newImg(String id) {
								SearchInterfaceContextParam contextParam = contextParamModel.getObject();
								ResourceReference contextParamResourceReference = new SearchInterfaceContextParamResourceReference(contextParam);
								return new NonCachingImage(id, contextParamResourceReference) {
									@Override
									public void detachModels() {
										contextParamModel.detach();
										super.detachModels();
									}
								};
							}
						});
					} else if (contextParam.isTextFileParam()) {
						cellItem.add(new ModalLinkHolder(componentId, new StringResourceModel("file", null)) {
							@Override
							protected WebMarkupContainer newLink(String id) {
						        return new AjaxLink(id) {
						            @Override
						            public void onClick(AjaxRequestTarget target) {    
										SearchInterfaceContextParam contextParam = contextParamModel.getObject();
										byte[] binaryValue = contextParam.getBinaryValue();
										try {
											List<String> fileLines = IOUtils.readLines(new ByteArrayInputStream(binaryValue));
									        StringBuffer fileLinesSB = new StringBuffer();
									        for (String fileLine : fileLines) {
												fileLinesSB.append(fileLine);
												fileLinesSB.append("\n");
											}

											ModalWindow modalWindow = getModalWindow();
											MultiLineLabel multiLineLabel = new MultiLineLabel(modalWindow.getContentId(), fileLinesSB.toString());
											multiLineLabel.add(new SimpleAttributeModifier("style", "text-align:left;"));
							                modalWindow.setContent(multiLineLabel);
							                modalWindow.show(target);
										} catch (IOException e) {
											throw new WicketRuntimeException(e);
										}
						            }
						        };
							}

							@Override
							public void detachModels() {
								contextParamModel.detach();
								super.detachModels();
							}
						});
					} else {
						cellItem.add(new ModalLinkHolder(componentId, new StringResourceModel("file", null)) {
							@Override
							protected WebMarkupContainer newLink(String id) {
								SearchInterfaceContextParam contextParam = contextParamModel.getObject();
								IModel inputStreamModel = new LoadableDetachableModel() {
									@Override
									protected Object load() {
										SearchInterfaceContextParam contextParam = contextParamModel.getObject();
										return new ByteArrayInputStream(contextParam.getBinaryValue());
									}
								};
								String fileName = contextParam.getFileName();
								String contentType = contextParam.getContentType();
								long length = contextParam.getFileLength();
								Date lastModified = contextParam.getLastModified();
						        return new DownloadInputStreamLink(id, inputStreamModel, fileName, contentType, length, lastModified);
							}

							@Override
							public void detachModels() {
								contextParamModel.detach();
								super.detachModels();
							}
						});
					}
				}
			}
		});
        
        dataColumns.add(new AbstractColumn(new StringResourceModel("paramType", null)) {
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) rowModel.getObject();
				cellItem.add(new Label(componentId, new StringResourceModel("paramType." + contextParam.getParamType(), null)));
			}
        });	
        
        dataColumns.add(new AbstractColumn(new StringResourceModel("inherited", null)) {
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) rowModel.getObject();
				SearchInterfaceContext context = contextModel.getObject();
				String inheritedSuffix = "" + context.isInheritedContextParam(contextParam.getParamName());
				cellItem.add(new Label(componentId, new StringResourceModel("inherited." + inheritedSuffix, null)));
			}
        });	
		
        return dataColumns;
	}

	@Override
	protected String getDetailsLabel(Object entity) {
		SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) entity;
		return contextParam.getParamName();
	}

	@Override
	protected BaseCRUDServices<? extends ConstellioEntity> getServices() {
		return ConstellioSpringUtils.getSearchInterfaceContextServices();
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		SearchInterfaceContext context = contextModel.getObject();
		SearchInterfaceContextParam contextParam = new SearchInterfaceContextParam();
		contextParam.setSearchInterfaceContext(context);
		return new AddEditSearchInterfaceContextParamPanel(id, contextParam, context);
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel rowItemModel, int index) {
		SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) rowItemModel.getObject();
		SearchInterfaceContext context = contextModel.getObject();
		return new AddEditSearchInterfaceContextParamPanel(id, contextParam, context);
	}

    @Override
    protected boolean isUseModals() {
        return false;
    }

	@Override
	public Component createDetailsColumnHeader(String componentId) {
		return new LinkHolder(componentId, new StringResourceModel("context", null)) {
			@Override
			protected WebMarkupContainer newLink(String id) {
				return new Link(id) {
					@Override
					public void onClick() {
						String panelId = SearchInterfaceContextParamListPanel.this.getId();
						SearchInterfaceContext context = contextModel.getObject();
						SearchInterfaceContext parentContext = context.getParentContext();
						SearchInterfaceContextListPanel replacement;
						if (parentContext != null) {
							replacement = new SearchInterfaceContextListPanel(panelId, parentContext);
						} else {
							replacement = new SearchInterfaceContextListPanel(panelId);
						}
						SearchInterfaceContextParamListPanel.this.replaceWith(replacement);
					}
				};
			}
			
			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					visible = contextModel != null;
				}
				return visible;
			}
		};
	}

	@Override
	protected boolean isDeleteLink(IModel rowItemModel, int index) {
		SearchInterfaceContext context = contextModel.getObject();
		SearchInterfaceContextParam contextParam = (SearchInterfaceContextParam) rowItemModel.getObject();
		return super.isDeleteLink(rowItemModel, index) && contextParam.getSearchInterfaceContext().equals(context);
	}

	@Override
	protected IModel getTitleModel() {
		SearchInterfaceContext context = contextModel.getObject();
		return new StringResourceModel("panelTitle", null, new String[] { context.getContextName() });
	}

}
