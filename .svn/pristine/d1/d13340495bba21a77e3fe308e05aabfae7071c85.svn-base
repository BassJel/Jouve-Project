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
package com.doculibre.constellio.wicket.panels.thesaurus;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.doculibre.constellio.entities.skos.SkosConcept;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;
import com.doculibre.constellio.wicket.models.SkosConceptPrefLabelModel;
import com.doculibre.constellio.wicket.panels.AjaxPanel;

@SuppressWarnings("serial")
public class SkosConceptModalPanel extends AjaxPanel {
    
    private ReloadableEntityModel<SkosConcept> skosConceptModel;
    
    private AbstractLink detailsLink;
    private Label prefLabel;
    private ListView altLabelsListView;
    private Label skosNotesLabel;
    private ListView broaderListView;
    private ListView narrowerListView;
    private ListView relatedListView;

    public SkosConceptModalPanel(String id, SkosConcept skosConcept) {
        super(id);
        this.skosConceptModel = new ReloadableEntityModel<SkosConcept>(skosConcept);
        
        final IModel localeModel = new PropertyModel(this, "locale");
        
        detailsLink = newDetailsLink("detailsLink", skosConcept);
        prefLabel = new Label("prefLabel", new SkosConceptPrefLabelModel(skosConcept, localeModel));
        
        altLabelsListView = new ListView("altLabels", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                SkosConcept skosConcept = skosConceptModel.getObject();
                return new ArrayList<String>(skosConcept.getAltLabels(getLocale()));
            }
        }){
            @Override
            protected void populateItem(ListItem item) {
                String altLabel = (String) item.getModelObject();
                item.add(new Label("altLabel", altLabel));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                List<String> items = (List<String>) getModelObject();
                return !items.isEmpty();
            }
        };
        
        skosNotesLabel = new Label("skosNotes", skosConcept.getSkosNotes());         
        
        broaderListView = new ListView("broader", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                SkosConcept skosConcept = skosConceptModel.getObject();
                return new ArrayList<SkosConcept>(skosConcept.getBroader());
            }
        }){
            @Override
            protected void populateItem(ListItem item) {
                SkosConcept broader = (SkosConcept) item.getModelObject();
                AbstractLink detailsLink = newDetailsLink("detailsLink", broader);
                item.add(detailsLink);
                detailsLink.add(new Label("prefLabel", new SkosConceptPrefLabelModel(broader, localeModel)));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                List<SkosConcept> items = (List<SkosConcept>) getModelObject();
                return !items.isEmpty();
            }
        };
        
        narrowerListView = new ListView("narrower", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                SkosConcept skosConcept = skosConceptModel.getObject();
                return new ArrayList<SkosConcept>(skosConcept.getNarrower());
            }
        }){
            @Override
            protected void populateItem(ListItem item) {
                SkosConcept narrower = (SkosConcept) item.getModelObject();
                AbstractLink detailsLink = newDetailsLink("detailsLink", narrower);
                item.add(detailsLink);
                detailsLink.add(new Label("prefLabel", new SkosConceptPrefLabelModel(narrower, localeModel)));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                List<SkosConcept> items = (List<SkosConcept>) getModelObject();
                return !items.isEmpty();
            }
        };
        
        relatedListView = new ListView("related", new LoadableDetachableModel() {
            @Override
            protected Object load() {
                SkosConcept skosConcept = skosConceptModel.getObject();
                return new ArrayList<SkosConcept>(skosConcept.getRelated());
            }
        }){
            @Override
            protected void populateItem(ListItem item) {
                SkosConcept related = (SkosConcept) item.getModelObject();
                AbstractLink detailsLink = newDetailsLink("detailsLink", related);
                item.add(detailsLink);
                detailsLink.add(new Label("prefLabel", new SkosConceptPrefLabelModel(related, localeModel)));
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                List<SkosConcept> items = (List<SkosConcept>) getModelObject();
                return !items.isEmpty();
            }
        };
        
        add(detailsLink);
        detailsLink.add(prefLabel);
        add(altLabelsListView);
        add(skosNotesLabel);
        add(broaderListView);
        add(narrowerListView);
        add(relatedListView);
    }

    @Override
    public void detachModels() {
        skosConceptModel.detach();
        super.detachModels();
    }
    
    protected AbstractLink newDetailsLink(String id, SkosConcept skosConcept) {
        Link link = new Link(id) {
            @Override
            public void onClick() {
            }
        };
        link.setEnabled(false);
        return link;
    }

}
