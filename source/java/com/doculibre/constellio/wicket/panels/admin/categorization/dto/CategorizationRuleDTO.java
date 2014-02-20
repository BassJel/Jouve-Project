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
package com.doculibre.constellio.wicket.panels.admin.categorization.dto;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.model.IDetachable;

import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.wicket.models.EntityModel;

@SuppressWarnings("serial")
public class CategorizationRuleDTO implements IDetachable {
    
    private String matchRegexp;
    
    private Long categorizationRuleId;

    private EntityModel<IndexField> indexFieldModel;
    
    private EntityModel<Categorization> categorizationModel;

    private Set<String> matchRegexpIndexedValues = new HashSet<String>();
    
    public CategorizationRuleDTO(CategorizationRule categorizationRule) {
        this.categorizationRuleId = categorizationRule.getId();
        this.matchRegexp = categorizationRule.getMatchRegexp();
        this.indexFieldModel = new EntityModel<IndexField>(categorizationRule.getIndexField());
        this.categorizationModel = new EntityModel<Categorization>(categorizationRule.getCategorization());
        this.matchRegexpIndexedValues.addAll(categorizationRule.getMatchRegexpIndexedValues());
    }
    
    public CategorizationRule toCategorizationRule() {
        CategorizationRule categorizationRule = new CategorizationRule();
        categorizationRule.setId(categorizationRuleId);
        categorizationRule.setMatchRegexp(matchRegexp);
        categorizationRule.setIndexField(getIndexField());
        categorizationRule.setCategorization(getCategorization());
        categorizationRule.getMatchRegexpIndexedValues().addAll(matchRegexpIndexedValues);
        return categorizationRule;
    }

    public Long getId() {
        return categorizationRuleId;
    }

    public void setId(Long categorizationRuleId) {
        this.categorizationRuleId = categorizationRuleId;
    }

    public String getMatchRegexp() {
        return matchRegexp;
    }
    
    public void setMatchRegexp(String matchRegexp) {
        this.matchRegexp = matchRegexp;
    }

    /**
     * Normally, we would have JoinColumn(nullable = false, updatable = false)
     * 
     * However, for cascade delete purposes, we cannot use these.
     * 
     * @return
     */
    public IndexField getIndexField() {
        return indexFieldModel.getObject();
    }
    
    public void setIndexField(IndexField indexField) {
        if (this.indexFieldModel == null) {
            this.indexFieldModel = new EntityModel<IndexField>(indexField);
        } else {
            this.indexFieldModel.setObject(indexField);
        }
    }

    public Categorization getCategorization() {
        return categorizationModel.getObject();
    }
    
    public void setCategorization(Categorization categorization) {
        if (this.categorizationModel == null) {
            this.categorizationModel = new EntityModel<Categorization>(categorization);
        } else {
            this.categorizationModel.setObject(categorization);
        }
    }

    public Set<String> getMatchRegexpIndexedValues() {
        return matchRegexpIndexedValues;
    }
    
    public void setMatchRegexpIndexedValues(Set<String> matchRegexpIndexedValues) {
        this.matchRegexpIndexedValues = matchRegexpIndexedValues;
    }
    
    public void addMatchRegexpIndexedValue(String matchRegexpIndexValue) {
        this.matchRegexpIndexedValues.add(matchRegexpIndexValue);
    }

    //FIXME Don't rely on indexField.hashCode() (external object)
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((indexFieldModel == null) ? 0 : indexFieldModel.hashCode());
        result = prime * result
                + ((matchRegexp == null) ? 0 : matchRegexp.hashCode());
        result = prime
                * result
                + ((matchRegexpIndexedValues == null) ? 0
                        : matchRegexpIndexedValues.hashCode());
        return result;
    }

    //FIXME Don't rely on indexField and matchRegexpIndexedValues (external object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (super.equals(obj))
            return true;
        if (getClass() != obj.getClass())
            return false;
        CategorizationRuleDTO other = (CategorizationRuleDTO) obj;
        if (indexFieldModel == null) {
            if (other.indexFieldModel != null)
                return false;
        } else if (!indexFieldModel.equals(other.indexFieldModel))
            return false;
        if (matchRegexp == null) {
            if (other.matchRegexp != null)
                return false;
        } else if (!matchRegexp.equals(other.matchRegexp))
            return false;
        if (matchRegexpIndexedValues == null) {
            if (other.matchRegexpIndexedValues != null)
                return false;
        } else if (!matchRegexpIndexedValues
                .equals(other.matchRegexpIndexedValues))
            return false;
        return true;
    }

    @Override
    public void detach() {
        if (indexFieldModel != null) {
            indexFieldModel.detach();
        }
        if (categorizationModel != null) {
            categorizationModel.detach();
        }
    }

}
