package com.doculibre.constellio.wicket.models;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.doculibre.constellio.entities.skos.SkosConcept;

@SuppressWarnings("serial")
public class SkosConceptPrefLabelModel extends LoadableDetachableModel {
    
    private ReloadableEntityModel<SkosConcept> skosConceptModel;
    private IModel localeModel;

    public SkosConceptPrefLabelModel(SkosConcept skosConcept, IModel localeModel) {
        this.skosConceptModel = new ReloadableEntityModel<SkosConcept>(skosConcept);
        this.localeModel = localeModel;
    }

    @Override
    protected Object load() {
    	Locale locale = (Locale) localeModel.getObject();
        return skosConceptModel.getObject().getPrefLabel(locale);
    }

    @Override
    protected void onDetach() {
        skosConceptModel.detach();
        localeModel.detach();
        super.onDetach();
    }
    
}
