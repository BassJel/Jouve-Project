package com.doculibre.constellio.wicket.panels.admin.searchInterface.context.params;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContext;
import com.doculibre.constellio.entities.searchInterface.SearchInterfaceContextParam;
import com.doculibre.constellio.services.SearchInterfaceContextServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;

@SuppressWarnings("serial")
public class AddEditSearchInterfaceContextParamPanel extends SaveCancelFormPanel {
	
	private boolean adding;
	private boolean copying;
	
	private EntityModel<SearchInterfaceContextParam> contextParamModel;
	private ReloadableEntityModel<SearchInterfaceContext> contextModel;
	
	private TextField paramNameField;
	private DropDownChoice paramTypeField;
	private Label inheritedLabel;
	private TextArea textValueField;
	private IModel binaryValueModel;
	private FileUploadField binaryValueField;

	public AddEditSearchInterfaceContextParamPanel(String id, SearchInterfaceContextParam contextParam, SearchInterfaceContext context) {
		super(id, false);
		
		if (contextParam.getSearchInterfaceContext().equals(context)) {
			this.contextParamModel = new EntityModel<SearchInterfaceContextParam>(contextParam);
			adding = contextParam.getId() == null;
			copying = false;
		} else {
			SearchInterfaceContextParam copiedContextParam = new SearchInterfaceContextParam(contextParam);
			this.contextParamModel = new EntityModel<SearchInterfaceContextParam>(copiedContextParam);
			adding = false;
			copying = true;
		}

		contextModel = new ReloadableEntityModel<SearchInterfaceContext>(context);

		String inheritedSuffix = "" + context.isInheritedContextParam(contextParam.getParamName());
		inheritedLabel = new Label("inherited", new StringResourceModel("inherited." + inheritedSuffix, null));

		Form form = getForm();
		form.setModel(new CompoundPropertyModel(contextParamModel));
		
		paramNameField = new RequiredTextField("paramName");
		
		List<String> paramTypes = new ArrayList<String>();
		paramTypes.add(SearchInterfaceContextParam.TEXT_PARAM);
		paramTypes.add(SearchInterfaceContextParam.IMG_PARAM);
		paramTypes.add(SearchInterfaceContextParam.TEXT_FILE_PARAM);
		paramTypes.add(SearchInterfaceContextParam.BINARY_FILE_PARAM);
		paramTypes.add(SearchInterfaceContextParam.REQUEST_PARAM);
		
		IChoiceRenderer paramTypeRenderer = new ChoiceRenderer() {
			@Override
			public Object getDisplayValue(Object object) {
				String paramTypeValue = (String) object; 
				return getLocalizer().getString("paramType." + paramTypeValue, AddEditSearchInterfaceContextParamPanel.this);
			}
		};
		
		paramTypeField = new DropDownChoice("paramType", paramTypes, paramTypeRenderer) {
			@Override
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}

			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					if (adding && !copying) {
						visible = true;
					} else {
						visible = false;
					}
				}
				return visible;
			}
		};
		
		textValueField = new TextArea("textValue") {
			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					SearchInterfaceContextParam contextParam = contextParamModel.getObject();
					visible = contextParam.isTextParam();
				}
				return visible;
			}
		};
		
		binaryValueModel = new Model();
		binaryValueField = new FileUploadField("binaryValue", binaryValueModel) {
			@Override
			public boolean isVisible() {
				boolean visible = super.isVisible();
				if (visible) {
					SearchInterfaceContextParam contextParam = contextParamModel.getObject();
					visible = contextParam.isBinaryFileParam();
				}
				return visible;
			}
		};
			
		form.add(paramNameField);
		form.add(paramTypeField);
		form.add(inheritedLabel);
		form.add(textValueField);
		form.add(binaryValueField);
	}
	
	@Override
	public void detachModels() {
		contextModel.detach();
		super.detachModels();
	}

	@Override
	protected void onSave(AjaxRequestTarget target) {
		SearchInterfaceContextParam contextParam = contextParamModel.getObject();
		if (contextParam.getId() == null && contextModel != null) {
			SearchInterfaceContext context = contextModel.getObject();
			contextParam.setSearchInterfaceContext(context);
		}
		
		FileUpload fileUpload = (FileUpload) binaryValueModel.getObject();
		if (fileUpload != null) {
			byte[] uploadedBytes = fileUpload.getBytes();
			String fileName = fileUpload.getClientFileName();
			long fileLength = fileUpload.getSize();
			String mimeType = fileUpload.getContentType();
			
			contextParam.setFileName(fileName);
			contextParam.setFileLength(fileLength);
			contextParam.setContentType(mimeType);
			contextParam.setBinaryValue(uploadedBytes);
			contextParam.setLastModified(new Date());
		}
		
		SearchInterfaceContextServices contextServices = ConstellioSpringUtils.getSearchInterfaceContextServices();
		EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		if (copying) {
			SearchInterfaceContext context = contextModel.getObject();
			SearchInterfaceContext parentContext = context.getParentContext();
			SearchInterfaceContextParam copyFrom = parentContext.getEffectiveContextParam(contextParam.getParamName());
			if (!copyFrom.isSameValues(contextParam)) {
				contextServices.makePersistent(contextParam);
			}
		} else {
			contextServices.makePersistent(contextParam);
		}
		entityManager.getTransaction().commit();
	}

	@Override
	protected IModel getTitleModel() {
		return new LoadableDetachableModel() {
			@Override
			protected Object load() {
				String titleKey = adding ? "add" : "edit";
				return new StringResourceModel(titleKey, AddEditSearchInterfaceContextParamPanel.this, null).getObject();
			}
		};
	}

	@Override
	protected Component newReturnComponent(String id) {
		SearchInterfaceContext context = contextModel.getObject();
		SearchInterfaceContextParamListPanel replacement = new SearchInterfaceContextParamListPanel(id, context);
		return replacement;
	}

}
