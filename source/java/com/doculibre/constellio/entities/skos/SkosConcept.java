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
package com.doculibre.constellio.entities.skos;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.ConstellioLabelledEntity;
import com.doculibre.constellio.entities.I18NLabel;

@SuppressWarnings("serial")
@Entity
public class SkosConcept extends ConstellioLabelledEntity {
    
    public static final String PREF_LABEL = "prefLabel";

    private String rdfAbout;
    
    private String skosNotes;

    private Thesaurus thesaurus;
    
    private Set<SkosConcept> broader = new HashSet<SkosConcept>();
    
    private Set<SkosConcept> narrower = new HashSet<SkosConcept>();

    private Set<SkosConcept> related = new HashSet<SkosConcept>();

    private Set<I18NLabel> labels = new HashSet<I18NLabel>();
    
    private Set<SkosConceptAltLabel> altLabels = new HashSet<SkosConceptAltLabel>();

    public String getRdfAbout() {
        return rdfAbout;
    }

    public void setRdfAbout(String rdfAbout) {
        this.rdfAbout = rdfAbout;
    }
    
    @Lob
    @Column (length = 100 * 1024)
    public String getSkosNotes() {
        return skosNotes;
    }

    public void setSkosNotes(String skosNotes) {
        this.skosNotes = skosNotes;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @ManyToMany(mappedBy="narrower")
    public Set<SkosConcept> getBroader() {
		return broader;
	}

	public void setBroader(Set<SkosConcept> broader) {
		this.broader = broader;
	}

    public void addBroader(SkosConcept skosConcept) {
        this.broader.add(skosConcept);
    }

    @ManyToMany(cascade = { CascadeType.REMOVE })
    @JoinTable(name = "SkosConcept_Narrower", joinColumns = { @JoinColumn(name = "broaderSkosConcept_id") }, inverseJoinColumns = { @JoinColumn(name = "narrowerSkosConcept_id") })
	public Set<SkosConcept> getNarrower() {
		return narrower;
	}

	public void setNarrower(Set<SkosConcept> narrower) {
		this.narrower = narrower;
	}
    
    public void addNarrower(SkosConcept skosConcept) {
        this.narrower.add(skosConcept);
    }

    @ManyToMany(cascade = { CascadeType.REMOVE })
    @JoinTable(name = "SkosConcept_Relations", joinColumns = { @JoinColumn(name = "sourceSkosConcept_id") }, inverseJoinColumns = { @JoinColumn(name = "relatedSkosConcept_id") })
    public Set<SkosConcept> getRelated() {
        return related;
    }

    public void setRelated(Set<SkosConcept> related) {
        this.related = related;
    }
    
    public void addRelated(SkosConcept skosConcept) {
        this.related.add(skosConcept);
    }

    @Override
    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name = "SkosConcept_Labels", joinColumns = { @JoinColumn(name = "skosConcept_id") }, inverseJoinColumns = { @JoinColumn(name = "label_id") })
    public Set<I18NLabel> getLabels() {
        return this.labels;
    }

    @Override
    protected void setLabels(Set<I18NLabel> labels) {
        this.labels = labels;
    }
    
    @Transient
    public Set<I18NLabel> getPrefLabels() {
        return getLabels();
    }

    public String getPrefLabel(Locale locale) {
        return getLabel(PREF_LABEL, locale);
    }

    public void setPrefLabel(String value, Locale locale) {
        setLabel(PREF_LABEL, value, locale);
    }
    
    @OneToMany(mappedBy = "skosConcept", cascade = { CascadeType.ALL }, orphanRemoval = true)
    public Set<SkosConceptAltLabel> getAltLabels() {
        return altLabels;
    }

    public void setAltLabels(Set<SkosConceptAltLabel> altLabels) {
        this.altLabels = altLabels;
    }
    
    public Set<String> getAltLabels(Locale locale) {
        SkosConceptAltLabel match = null;
        for (SkosConceptAltLabel altLabel : altLabels) {
            if (altLabel.getLocale().equals(locale)) {
                match = altLabel;
                break;
            }
        }
        if (match == null) {
            match = new SkosConceptAltLabel();
            match.setLocale(locale);
            match.setSkosConcept(this);
            altLabels.add(match);
        }
        return match.getValues();
    }
    
    public void addAltLabel(Locale locale, String value) {
        getAltLabels(locale).add(value);
    }

//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((rdfAbout == null) ? 0 : rdfAbout.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (super.equals(obj))
//            return true;
//        if (getClass() != obj.getClass())
//            return false;
//        SkosConcept other = (SkosConcept) obj;
//        if (rdfAbout == null) {
//            if (other.rdfAbout != null)
//                return false;
//        } else if (!rdfAbout.equals(other.rdfAbout))
//            return false;
//        return true;
//    }

}
