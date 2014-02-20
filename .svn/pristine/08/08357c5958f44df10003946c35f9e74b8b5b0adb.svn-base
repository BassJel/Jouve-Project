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
package com.doculibre.constellio.wicket.panels.facets;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.search.FacetValue;
import com.doculibre.constellio.entities.search.SearchableFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.plugins.PluginFactory;
import com.doculibre.constellio.plugins.api.wicket.PageFactoryPlugin;
import com.doculibre.constellio.services.RecordCollectionServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.NumberFormatUtils;
import com.doculibre.constellio.wicket.data.FacetsDataProvider;
import com.doculibre.constellio.wicket.pages.SearchResultsPage;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class ClusterPanel extends AjaxPanel {

	public ClusterPanel(String id, final SearchableFacet clusterFacet, final FacetsDataProvider dataProvider) {
		super(id);
		
		TreeModel treeModel = convertToTreeModel(clusterFacet, dataProvider);
	    final BaseTree tree = new LinkTree("tree", treeModel) {
			@Override
			protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target) {
				FacetValue facetValue = (FacetValue) ((DefaultMutableTreeNode) node).getUserObject();
				SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
				SimpleSearch clone = simpleSearch.clone();
				clone.removeSearchedFacet(clusterFacet);
				clone.addSearchedFacet(clusterFacet, facetValue);
				
//				ConstellioSession.get().addSearchHistory(clone);
				clone.setPage(0);

		        PageFactoryPlugin pageFactoryPlugin = PluginFactory.getPlugin(PageFactoryPlugin.class);
				setResponsePage(pageFactoryPlugin.getSearchResultsPage(), SearchResultsPage.getParameters(clone));
			}

			@Override
			protected IModel getNodeTextModel(IModel nodeModel) {
				IModel nodeTextModel;
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodeModel.getObject();
				if (treeNode.getUserObject() == null) {
					nodeTextModel = new Model("null");
				} else if (treeNode.getUserObject() instanceof FacetValue) {
					FacetValue facetValue = (FacetValue) treeNode.getUserObject();
					
					RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
					SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
					String collectionName = simpleSearch.getCollectionName();
					RecordCollection collection = collectionServices.get(collectionName);
					Locale displayLocale = collection.getDisplayLocale(getLocale());
					
					String label;
					if (facetValue.getSubValues().isEmpty()) {
					    int numFound = facetValue.getDocCount();
		                String numFoundFormatted = NumberFormatUtils.format(numFound, displayLocale);
						label = facetValue.getLabel(displayLocale) + " (" + numFoundFormatted + ")";
					} else {
						label = facetValue.getLabel(displayLocale);
					}
					nodeTextModel = new Model(label);
				} else {
					nodeTextModel = super.getNodeTextModel(nodeModel);
				}
				return nodeTextModel;
			}
	    };
        tree.getTreeState().expandAll();
        add(tree);
	}

    private TreeModel convertToTreeModel(SearchableFacet clusterFacet, FacetsDataProvider dataProvider) {
		RecordCollectionServices collectionServices = ConstellioSpringUtils.getRecordCollectionServices();
		SimpleSearch simpleSearch = dataProvider.getSimpleSearch();
		String collectionName = simpleSearch.getCollectionName();
		RecordCollection collection = collectionServices.get(collectionName);
		Locale displayLocale = collection.getDisplayLocale(getLocale());
		
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(clusterFacet.getLabels().get(displayLocale));
        add(rootNode, clusterFacet.getValues());
        return new DefaultTreeModel(rootNode);
    }

    private void add(DefaultMutableTreeNode parent, List<FacetValue> facetValues) {
        for (Iterator<FacetValue> i = facetValues.iterator(); i.hasNext();) {
            FacetValue facetValue = i.next();
            //;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(facetValue);
            parent.add(child);
			// Recursive call
            add(child, facetValue.getSubValues());
        }
    }

}
