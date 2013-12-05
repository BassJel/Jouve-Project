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
package com.doculibre.constellio.wicket.panels.admin.featuredLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.settings.TinyMCESettings;

import com.doculibre.constellio.entities.FeaturedLink;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.services.BaseCRUDServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.wicket.panels.admin.collection.AdminCollectionPanel;
import com.doculibre.constellio.wicket.panels.admin.crud.SingleColumnCRUDPanel;

@SuppressWarnings("serial")
public class FeaturedLinkListPanel extends SingleColumnCRUDPanel implements IHeaderContributor {

	public FeaturedLinkListPanel(String id) {
		super(id);

		setModel(new LoadableDetachableModel() {
			@Override
			protected Object load() {
				AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
				RecordCollection collection = collectionAdminPanel.getCollection();
				return new ArrayList<FeaturedLink>(collection.getFeaturedLinks());
			}
		});
	}

	@Override
	protected WebMarkupContainer createAddContent(String id) {
		return new AddEditFeaturedLinkPanel(id, new FeaturedLink());
	}

	@Override
	protected WebMarkupContainer createEditContent(String id, IModel entityModel, int index) {
		FeaturedLink featuredLink = (FeaturedLink) entityModel.getObject();
		return new AddEditFeaturedLinkPanel(id, featuredLink);
	}

    @Override
    protected List<IColumn> getDataColumns() {
        List<IColumn> dataColumns = super.getDataColumns();
        IColumn idColumn = new PropertyColumn(new StringResourceModel("id", this, null), "id");
        dataColumns.add(0, idColumn);
        return dataColumns;
    }

	@Override
	protected String getDetailsLabel(Object entity) {
		FeaturedLink featuredLink = (FeaturedLink) entity;
		AdminCollectionPanel collectionAdminPanel = (AdminCollectionPanel) findParent(AdminCollectionPanel.class);
		RecordCollection collection = collectionAdminPanel.getCollection();
		Locale displayLocale = collection.getDisplayLocale(getLocale());
		return featuredLink.getTitle(displayLocale);
	}

	/**
	 * This is needed because even though {@link TinyMceBehavior} implements IHeaderContributor,
	 * the header doesn't get contributed when the component is first rendered though an AJAX call.
	 * @see https://issues.apache.org/jira/browse/WICKET-618 (which was closed WontFix) 
	 */
	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(TinyMCESettings.javaScriptReference());
	}

	@Override
	protected BaseCRUDServices<FeaturedLink> getServices() {
		return ConstellioSpringUtils.getFeaturedLinkServices();
	}

    @Override
    protected boolean isUseModals() {
        return false;
    }

}
