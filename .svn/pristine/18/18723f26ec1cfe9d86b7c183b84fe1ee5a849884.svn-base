package com.doculibre.constellio.solr.handler.component;

import java.util.HashSet;
import java.util.Set;

public class CategorizationRule{

	private String srcField;
	private String matchRegexp;
	private Set<String> matchRegexpIndexedValues = new HashSet<String>();

//	public CategorizationRule(String srcField, String matchRegexp, Set<String> matchRegexpIndexedValues) {
//		this.srcField = srcField;
//		this.matchRegexp = matchRegexp;
//		this.matchRegexpIndexedValues = matchRegexpIndexedValues;
//	}
	
	public String getSrcField() {
		return srcField;
	}
	
	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}
	
	public String getMatchRegexp() {
		return matchRegexp;
	}
	
	public void setMatchRegexp(String matchRegexp) {
		this.matchRegexp = matchRegexp;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((srcField == null) ? 0 : srcField.hashCode());
		result = prime * result
				+ ((matchRegexp == null) ? 0 : matchRegexp.hashCode());
		result = prime
				* result
				+ ((matchRegexpIndexedValues == null) ? 0
						: matchRegexpIndexedValues.hashCode());
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
		CategorizationRule other = (CategorizationRule) obj;
		if (srcField == null) {
			if (other.srcField != null)
				return false;
		} else if (!srcField.equals(other.srcField))
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
}
