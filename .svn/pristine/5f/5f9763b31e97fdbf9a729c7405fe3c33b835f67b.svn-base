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
package com.doculibre.constellio.wicket.components.locale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.wicket.models.ToArrayListModel;

@SuppressWarnings("serial")
public abstract class MultiLocaleComponentHolder extends ListView {

    private IModel labelledEntityModel;
    private String labelKey;
    private String propertyName;

    /**
     * @param id
     *            (same as labelKey)
     * @param labelledEntity
     * @param locales
     */
    public MultiLocaleComponentHolder(String id, IModel labelledEntityModel, Collection<Locale> locales) {
        this(id, labelledEntityModel, new Model(new ArrayList<Locale>(locales)));
    }

    /**
     * @param id
     *            (same as labelKey)
     * @param labelledEntity
     * @param locales
     */
    public MultiLocaleComponentHolder(String id, IModel labelledEntityModel, IModel localesModel) {
        this(id, id, labelledEntityModel, null, localesModel);
    }

    public MultiLocaleComponentHolder(String id, String labelKey, IModel labelledEntityModel,
        Collection<Locale> locales) {
        this(id, labelKey, labelledEntityModel, new Model(new ArrayList<Locale>(locales)));
    }

    public MultiLocaleComponentHolder(String id, String labelKey, IModel labelledEntityModel,
        IModel localesModel) {
        this(id, labelKey, labelledEntityModel, null, localesModel);
    }

    public MultiLocaleComponentHolder(String id, String labelKey, IModel labelledEntityModel,
        String propertyName, Collection<Locale> locales) {
        this(id, labelKey, labelledEntityModel, propertyName, new Model(new ArrayList<Locale>(locales)));
    }

    public MultiLocaleComponentHolder(String id, String labelKey, IModel labelledEntityModel,
        String propertyName, IModel localesModel) {
        super(id, new ToArrayListModel(localesModel));
        this.labelledEntityModel = labelledEntityModel;
        this.labelKey = labelKey;
        this.propertyName = propertyName;
    }

    @Override
    protected void populateItem(ListItem item) {
        Locale locale = (Locale) item.getModelObject();
        IModel componentModel = new LocalePropertyModel(labelledEntityModel, labelKey, propertyName, locale);
        onPopulateItem(item, componentModel, locale);
    }

    @Override
    public void detachModels() {
        labelledEntityModel.detach();
        super.detachModels();
    }

    /**
     * Since the user of a MultiLocaleComponentHolder is responsible with providing
     * the actual markup, he gets to choose the id of the component. That's the
     * reason why no id is provided to this method.
     * 
     * @return
     */
    protected abstract void onPopulateItem(ListItem item, IModel componentModel, Locale locale);

}
