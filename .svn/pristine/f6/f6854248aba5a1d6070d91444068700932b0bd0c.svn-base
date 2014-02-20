package com.doculibre.constellio.solr.handler.component;

import java.util.HashSet;
import java.util.Set;

public class Categorization{
	
	private String name;
	private String destField;
	
	private Set<CategorizationRule> categorizationRules = new HashSet<CategorizationRule>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDestField() {
		return destField;
	}

	public void setDestField(String destField) {
		this.destField = destField;
	}
	
	public Set<CategorizationRule> getCategorizationRules() {
		return categorizationRules;
	}
	
	public void setCategorizationRules(Set<CategorizationRule> categorizationRules) {
		this.categorizationRules = categorizationRules;
	}
	
	public void addCategorizationRule(CategorizationRule categorizationRule) {
		this.categorizationRules.add(categorizationRule);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((destField == null) ? 0 : destField.hashCode());
		result = prime
				* result
				+ ((categorizationRules == null) ? 0
						: categorizationRules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (super.equals(obj))
			return true;
		if (getClass() != obj.getClass())
			return false;
		Categorization other = (Categorization) obj;
		if (destField == null) {
			if (other.destField != null)
				return false;
		} else if (!destField.equals(other.destField))
			return false;
		if (categorizationRules == null) {
			if (other.categorizationRules != null)
				return false;
		} else if (!categorizationRules
				.equals(other.categorizationRules))
			return false;
		return true;
	}
	
}

