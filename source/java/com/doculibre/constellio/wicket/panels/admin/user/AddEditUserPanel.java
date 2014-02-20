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
package com.doculibre.constellio.wicket.panels.admin.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.UserCredentials;
import com.doculibre.constellio.services.FederationServices;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.EncryptionUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.EntityModel;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.panels.admin.SaveCancelFormPanel;
import com.doculibre.constellio.wicket.panels.admin.server.AdminServerPanel;

@SuppressWarnings("serial")
public class AddEditUserPanel extends SaveCancelFormPanel {

    private ReloadableEntityModel<ConstellioUser> userModel;

    private PasswordTextField password;

    private IModel visibleCredentialGroupsModel;

    private ListView credentialGroupsListView;

    private Map<Long, EntityModel<UserCredentials>> userCredentialsModelMap = new HashMap<Long, EntityModel<UserCredentials>>();

    public AddEditUserPanel(String id, ConstellioUser user) {
        super(id, true);
        this.userModel = new ReloadableEntityModel<ConstellioUser>(user);

        Form form = getForm();
        form.setModel(new CompoundPropertyModel(userModel));

        TextField username = new RequiredTextField("username");
        form.add(username);

        password = new PasswordTextField("password", new Model());
        form.add(password);
        password.setRequired(user.getId() == null);

        PasswordTextField passwordConfirmation = new PasswordTextField("passwordConfirmation", new Model());
        form.add(passwordConfirmation);
        passwordConfirmation.setRequired(user.getId() == null);

        TextField firstName = new RequiredTextField("firstName");
        form.add(firstName);

        TextField lastName = new RequiredTextField("lastName");
        form.add(lastName);

        IModel languagesModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                List<String> supportedLanguages = new ArrayList<String>();
                List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
                for (Locale supportedLocale : supportedLocales) {
                    supportedLanguages.add(supportedLocale.getLanguage());
                }
                return supportedLanguages;
            }
        };

        IChoiceRenderer languagesRenderer = new ChoiceRenderer() {
            @Override
            public Object getDisplayValue(Object object) {
                return new Locale((String) object).getDisplayLanguage(getLocale());
            }
        };

        DropDownChoice languageField = new DropDownChoice("localeCode", languagesModel, languagesRenderer);
        form.add(languageField);
        languageField.setRequired(true);
        languageField.setNullValid(false);

        CheckBox admin = new CheckBox("admin");
        form.add(admin);

        CheckBox collaborator = new CheckBox("collaborator");
        form.add(collaborator);

        form.add(new EqualPasswordInputValidator(password, passwordConfirmation));

        visibleCredentialGroupsModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                Set<CredentialGroup> visibleCredentialGroups = new HashSet<CredentialGroup>();
                ConstellioUser user = userModel.getObject();
                RecordCollectionServices collectionServices = ConstellioSpringUtils
                    .getRecordCollectionServices();
                FederationServices federationServices = ConstellioSpringUtils.getFederationServices();
                for (RecordCollection collection : collectionServices.list()) {
                    if (user.hasSearchPermission(collection)) {
                        Collection<CredentialGroup> searchCredentialGroups;
                        if (collection.isFederationOwner()) {
                            searchCredentialGroups = federationServices.listCredentialGroups(collection);
                        } else {
                            searchCredentialGroups = collection.getCredentialGroups();
                        }
                        for (CredentialGroup credentialGroup : searchCredentialGroups) {
                            visibleCredentialGroups.add(credentialGroup);
                        }
                    }
                }
                return new ArrayList<CredentialGroup>(visibleCredentialGroups);
            }
        };

        credentialGroupsListView = new ListView("credentialGroups", visibleCredentialGroupsModel) {
            @Override
            protected void populateItem(ListItem item) {
                CredentialGroup credentialGroup = (CredentialGroup) item.getModelObject();
                RecordCollection collection = credentialGroup.getRecordCollection();
                Locale displayLocale = collection.getDisplayLocale(getLocale());
                ConstellioUser user = userModel.getObject();
                String credentialGroupLabel = credentialGroup.getName() + " ("
                    + collection.getTitle(displayLocale) + ")";

                EntityModel<UserCredentials> userCredentialsModel = userCredentialsModelMap
                    .get(credentialGroup.getId());
                if (userCredentialsModel == null) {
                    UserCredentials userCredentials = user.getUserCredentials(credentialGroup);
                    if (userCredentials == null) {
                        userCredentials = new UserCredentials();
                    }
                    userCredentialsModel = new EntityModel<UserCredentials>(userCredentials);
                    userCredentialsModelMap.put(credentialGroup.getId(), userCredentialsModel);
                }

                final TextField usernameField = new TextField("username", new PropertyModel(
                    userCredentialsModel, "username"));
                final EntityModel<UserCredentials> finalUserCredentialsModel = userCredentialsModel;
                PasswordTextField encrypedPasswordField = new PasswordTextField("encryptedPassword",
                    new Model() {
                        @Override
                        public Object getObject() {
                            UserCredentials userCredentials = finalUserCredentialsModel.getObject();
                            return EncryptionUtils.decrypt(userCredentials.getEncryptedPassword());
                        }

                        @Override
                        public void setObject(Object object) {
                            UserCredentials userCredentials = finalUserCredentialsModel.getObject();
                            String encryptedPassword = EncryptionUtils.encrypt((String) object);
                            if (encryptedPassword != null || StringUtils.isEmpty(usernameField.getInput())) {
                                userCredentials.setEncryptedPassword(encryptedPassword);
                            }
                        }
                    });
                encrypedPasswordField.setRequired(false);
                TextField domainField = new TextField("domain", new PropertyModel(userCredentialsModel,
                    "domain"));

                item.add(new Label("name", credentialGroupLabel));
                item.add(usernameField);
                item.add(encrypedPasswordField);
                item.add(domainField);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                boolean visible = super.isVisible();
                if (visible) {
                    List<CredentialGroup> credentialGroups = (List<CredentialGroup>) visibleCredentialGroupsModel
                        .getObject();
                    visible = !credentialGroups.isEmpty();
                }
                return visible;
            }
        };
        form.add(credentialGroupsListView);
    }

    @Override
    public void detachModels() {
        userModel.detach();
        for (EntityModel<UserCredentials> userCredentialsModel : userCredentialsModelMap.values()) {
            userCredentialsModel.detach();
        }
        super.detachModels();
    }

    @Override
    protected IModel getTitleModel() {
        return new LoadableDetachableModel() {
            @Override
            protected Object load() {
                String titleKey = userModel.getObject().getId() == null ? "add" : "edit";
                return new StringResourceModel(titleKey, AddEditUserPanel.this, null).getObject();
            }
        };
    }

    @Override
    protected Component newReturnComponent(String id) {
        return null;
    }

    @Override
    protected void onSave(AjaxRequestTarget target) {
        ConstellioUser user = userModel.getObject();
        if (password.getModelObject() != null) {
            user.setPassword((String) password.getModelObject());
        }

        UserServices userServices = ConstellioSpringUtils.getUserServices();
        RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
        for (EntityModel<UserCredentials> userCredentialsModel : userCredentialsModelMap.values()) {
            UserCredentials userCredentials = userCredentialsModel.getObject();
            if (userCredentials.getId() == null) {
                user.addUserCredentials(userCredentials);

                for (Long credentialGroupId : userCredentialsModelMap.keySet()) {
                    CredentialGroup userCredentialsGroup = null;
                    for (RecordCollection collection : collectionServices.list()) {
                        for (CredentialGroup credentialGroup : collection.getCredentialGroups()) {
                            if (credentialGroup.getId().equals(credentialGroupId)) {
                                userCredentialsGroup = credentialGroup;
                                break;
                            }
                        }
                    }
                    userCredentials.setCredentialGroup(userCredentialsGroup);
                }
            }
        }

        EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        userServices.makePersistent(user);
        entityManager.getTransaction().commit();
    }

    @Override
    protected void defaultReturnAction(AjaxRequestTarget target) {
        super.defaultReturnAction(target);
        AdminServerPanel serverAdminPanel = (AdminServerPanel) findParent(AdminServerPanel.class);
        target.addComponent(serverAdminPanel);
    }
}
