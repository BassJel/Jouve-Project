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
package com.doculibre.constellio.wicket.panels.admin.relevance.collection.dto;

import org.apache.wicket.model.IDetachable;

import com.doculibre.constellio.entities.relevance.BoostRule;
import com.doculibre.constellio.entities.relevance.RecordCollectionBoost;
import com.doculibre.constellio.wicket.models.EntityModel;

@SuppressWarnings("serial")
public class BoostRuleDTO implements IDetachable {
    
    private String regex;
    
    private Double boost;
    
    private Long boostRuleId;

    private EntityModel<RecordCollectionBoost> recordCollectionBoostModel;

    public BoostRuleDTO(BoostRule boostRule) {
        this.boostRuleId = boostRule.getId();
        this.regex = boostRule.getRegex();
        this.boost = boostRule.getBoost();
        this.recordCollectionBoostModel = new EntityModel<RecordCollectionBoost>(boostRule.getRecordCollectionBoost());
    }
    
    public BoostRule toBoostRule() {
    	BoostRule boostRule = new BoostRule();
    	boostRule.setId(boostRuleId);
    	boostRule.setRecordCollectionBoost(getCollectionBoost());
    	boostRule.setRegex(regex);
    	boostRule.setBoost(boost);
    	return boostRule;
    }

    public Long getId() {
        return boostRuleId;
    }

    public void setId(Long categorizationRuleId) {
        this.boostRuleId = categorizationRuleId;
    }

    public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public Double getBoost() {
		return boost;
	}

	public void setBoost(Double boost) {
		this.boost = boost;
	}

	public RecordCollectionBoost getCollectionBoost() {
        return recordCollectionBoostModel.getObject();
    }
    
    public void setCollectionBoost(RecordCollectionBoost recordCollectionBoost) {
        if (this.recordCollectionBoostModel == null) {
            this.recordCollectionBoostModel = new EntityModel<RecordCollectionBoost>(recordCollectionBoost);
        } else {
            this.recordCollectionBoostModel.setObject(recordCollectionBoost);
        }
    }

    @Override
    public void detach() {
        if (recordCollectionBoostModel != null) {
        	recordCollectionBoostModel.detach();
        }
    }

}
