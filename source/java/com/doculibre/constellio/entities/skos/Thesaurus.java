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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.RecordCollection;

@SuppressWarnings("serial")
@Entity
public class Thesaurus extends BaseConstellioEntity {

    private String rdfAbout;
    private String dcTitle;
    private String dcDescription;
    private String dcCreator;
    private Date dcDate;
    private Locale dcLanguage;

    private RecordCollection recordCollection;

    private Set<SkosConcept> topConcepts = new HashSet<SkosConcept>();

    public Thesaurus() {
    }

    public Thesaurus(String dcTitle) {
        this.dcTitle = dcTitle;
    }

    public String getRdfAbout() {
        return rdfAbout;
    }

    public void setRdfAbout(String rdfAbout) {
        this.rdfAbout = rdfAbout;
    }

    public String getDcTitle() {
        return dcTitle;
    }

    public void setDcTitle(String dcTitle) {
        this.dcTitle = dcTitle;
    }

    public String getDcDescription() {
        return dcDescription;
    }

    public void setDcDescription(String dcDescription) {
        this.dcDescription = dcDescription;
    }

    public String getDcCreator() {
        return dcCreator;
    }

    public void setDcCreator(String dcCreator) {
        this.dcCreator = dcCreator;
    }

    public Date getDcDate() {
        return dcDate;
    }

    public void setDcDate(Date dcDate) {
        this.dcDate = dcDate;
    }

    public Locale getDcLanguage() {
        return dcLanguage;
    }

    public void setDcLanguage(Locale dcLanguage) {
        this.dcLanguage = dcLanguage;
    }

    @OneToOne
    public RecordCollection getRecordCollection() {
        return recordCollection;
    }

    public void setRecordCollection(RecordCollection recordCollection) {
        this.recordCollection = recordCollection;
    }

    @OneToMany(mappedBy = "thesaurus", fetch = FetchType.LAZY)
    public Set<SkosConcept> getTopConcepts() {
        return topConcepts;
    }

    public void setTopConcepts(Set<SkosConcept> topConcepts) {
        this.topConcepts = topConcepts;
    }

    public void addTopConcept(SkosConcept topConcept) {
        topConcept.getBroader().clear();
        this.topConcepts.add(topConcept);
        topConcept.setThesaurus(this);
    }

    @Transient
    public Map<String, SkosConcept> getFlattenedConcepts() {
        Map<String, SkosConcept> flattenedConcepts = new HashMap<String, SkosConcept>();
        for (SkosConcept topConcept : getTopConcepts()) {
            flattenedConcepts.put(topConcept.getRdfAbout(), topConcept);
            addNarrower(topConcept, flattenedConcepts);
        }
        return flattenedConcepts;
    }

    private void unflattenConcepts(Map<String, SkosConcept> flattenedConcepts) {
        for (SkosConcept flattenedConcept : flattenedConcepts.values()) {
            Set<SkosConcept> broader = flattenedConcept.getBroader();
            if (broader.isEmpty()) {
                this.addTopConcept(flattenedConcept);
            } else {
            	for (SkosConcept broaderConcept : broader) {
                    broaderConcept.getNarrower().add(flattenedConcept);
				}
            }
        }
    }

    private static void addNarrower(SkosConcept skosConcept, Map<String, SkosConcept> loadedConcepts) {
        for (SkosConcept narrower : skosConcept.getNarrower()) {
            loadedConcepts.put(narrower.getRdfAbout(), narrower);
            // Recursive call
            addNarrower(narrower, loadedConcepts);
        }
        skosConcept.getNarrower().clear(); // Reduce size
    }

    public boolean equalsRdfAbout(Thesaurus obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (super.equals(obj))
            return true;
        if (getClass() != obj.getClass())
            return false;
        Thesaurus other = (Thesaurus) obj;
        if (rdfAbout == null) {
            if (other.rdfAbout != null)
                return false;
        } else if (!rdfAbout.equals(other.rdfAbout))
            return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        rdfAbout = (String) fields.get("rdfAbout", null);
        dcTitle = (String) fields.get("dcTitle", null);
        dcDescription = (String) fields.get("dcDescription", null);
        dcDate = (Date) fields.get("dcDate", null);
        dcCreator = (String) fields.get("dcCreator", null);
        topConcepts = new HashSet<SkosConcept>();

        Map<String, SkosConcept> flattenedConcepts = (Map<String, SkosConcept>) ois.readObject();
        unflattenConcepts(flattenedConcepts);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Convert to version one types
        ObjectOutputStream.PutField fields = oos.putFields();
        Map<String, SkosConcept> flattenedConcepts = getFlattenedConcepts();
        for (SkosConcept concept : flattenedConcepts.values()) {
            concept.getNarrower().clear();
        }
        topConcepts.clear();
        fields.put("rdfAbout", rdfAbout);
        fields.put("dcTitle", dcTitle);
        fields.put("dcDescription", dcDescription);
        fields.put("dcDate", dcDate);
        fields.put("dcCreator", dcCreator);

        // Write version one types
        oos.writeFields();
        oos.writeObject(flattenedConcepts);
    }

}
