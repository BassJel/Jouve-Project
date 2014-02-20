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
package com.doculibre.constellio.entities.search.advanced.indexFieldRules;

import com.doculibre.constellio.entities.search.advanced.AbstractSearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRule;
import com.doculibre.constellio.entities.search.advanced.SearchRulesGroup;
import com.doculibre.constellio.entities.search.advanced.enums.MathEquation;
import com.doculibre.constellio.utils.SimpleParams;

@SuppressWarnings("serial")
public abstract class AbstractNumericSearchRule<T extends Comparable<T>>
		extends AbstractSearchRule implements IndexFieldSearchRule {

	private String indexFieldName;

	private T comparisonValue;

	private T secondComparisonValue;

	private MathEquation equation = MathEquation.DEFAULT;

	public static final String PARAM_VALUE_1 = "v1";
	public static final String PARAM_VALUE_2 = "v2";
	public static final String PARAM_EQUATION = "eq";
	
	public AbstractNumericSearchRule() {
	}

	public AbstractNumericSearchRule(SimpleParams params,
			SearchRulesGroup parent, String lookupPrefix) {
		super(parent);
		this.indexFieldName = params.getString(lookupPrefix + DELIM + PARAM_INDEX_FIELD);
		String param1 = params.getString(lookupPrefix + DELIM + PARAM_VALUE_1);
		String param2 = params.getString(lookupPrefix + DELIM + PARAM_VALUE_2);
		String eq = params.getString(lookupPrefix + DELIM + PARAM_EQUATION);
		if (param1 != null) {
			this.comparisonValue = fromHTTPParam(param1);
		}
		if (param2 != null) {
			this.secondComparisonValue = fromHTTPParam(param2);
		}
		if (eq != null) {
			this.equation = MathEquation.valueOf(eq);
		}
	}

	@Override
	public SearchRule cloneRule() {
		try {
			@SuppressWarnings("unchecked")
			AbstractNumericSearchRule<T> newRule = getClass().newInstance();
			if (comparisonValue != null) {
				newRule.comparisonValue = cloneValue(comparisonValue);
			}
			if (secondComparisonValue != null) {
				newRule.secondComparisonValue = cloneValue(secondComparisonValue);
			}
			newRule.equation = equation;
			newRule.indexFieldName = indexFieldName;
			return newRule;
		} catch (InstantiationException e) {
			throw new RuntimeException();

		} catch (IllegalAccessException e) {
			throw new RuntimeException();
		}
	}

	protected T cloneValue(T value) {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractNumericSearchRule<?> other = (AbstractNumericSearchRule<?>) obj;
		if (comparisonValue == null) {
			if (other.comparisonValue != null)
				return false;
		} else if (!comparisonValue.equals(other.comparisonValue))
			return false;
		if (equation != other.equation)
			return false;
		if (indexFieldName == null) {
			if (other.indexFieldName != null)
				return false;
		} else if (!indexFieldName.equals(other.indexFieldName))
			return false;
		if (secondComparisonValue == null) {
			if (other.secondComparisonValue != null)
				return false;
		} else if (!secondComparisonValue.equals(other.secondComparisonValue))
			return false;
		return true;
	}

	protected abstract T fromHTTPParam(String param);

	public T getComparisonValue() {
		return comparisonValue;
	}

	public MathEquation getEquation() {
		return equation;
	}

	public String getIndexFieldName() {
		return indexFieldName;
	}

	public T getSecondComparisonValue() {
		return secondComparisonValue;
	}

	protected abstract String getType();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((comparisonValue == null) ? 0 : comparisonValue.hashCode());
		result = prime * result
				+ ((equation == null) ? 0 : equation.hashCode());
		result = prime * result
				+ ((indexFieldName == null) ? 0 : indexFieldName.hashCode());
		result = prime
				* result
				+ ((secondComparisonValue == null) ? 0 : secondComparisonValue
						.hashCode());
		return result;
	}

	@Override
	public boolean isValid() {
		if (equation == null) {
			return false;
		}
		if (MathEquation.RANGE.equals(equation)) {
			return comparisonValue != null && secondComparisonValue != null
					&& comparisonValue.compareTo(secondComparisonValue) == -1;
		} else {
			return comparisonValue != null;
		}
	}

	public void setComparison(T comparison) {
		this.comparisonValue = comparison;
	}

	public void setEquation(MathEquation equation) {
		this.equation = equation;
	}

	public void setIndexFieldName(String indexFieldName) {
		this.indexFieldName = indexFieldName;
	}

	public void setMaxRange(T maxRange) {
		this.secondComparisonValue = maxRange;
	}

	protected String toHTTPParam(T value) {
		String strValue;
		if (value == null) {
			strValue = null;
		} else {
			strValue = value.toString();
		}
		return strValue;
	}

	@Override
	public final SimpleParams toSimpleParams(boolean onlyType) {
		SimpleParams params = new SimpleParams();
		String prefix = getPrefix();
		params.add(prefix + DELIM + PARAM_TYPE, getType());
		params.add(prefix + DELIM + PARAM_INDEX_FIELD, this.indexFieldName);
		if (!onlyType) {
			if (equation != null) {
				params.add(prefix + DELIM + PARAM_EQUATION, equation.name());
			}
			if (comparisonValue != null) {
				params.add(prefix + DELIM + PARAM_VALUE_1, toHTTPParam(comparisonValue));
			}
			if (secondComparisonValue != null) {
				params.add(prefix + DELIM + PARAM_VALUE_2, toHTTPParam(secondComparisonValue));
			}
		}
		return params;
	}

	@Override
	public String toLuceneQuery() {
		String query = indexFieldName + ":";
		switch (equation) {
		case EQ:
			query +="[" + toMinLuceneParam(comparisonValue) + " TO " + toMaxLuceneParam(comparisonValue) + "]";
			break;
			
//		case NE:
			//TODO Implement neglation and enable it in MathEquation
//			break;
			
		case GT:
			query +="[" + toMaxLuceneParam(comparisonValue) + " TO * ]";
			break;
			
		case LT:
			query +="[ * TO " + toMinLuceneParam(comparisonValue) + "]";
			break;
			
		case RANGE:
			query +="[" + toMinLuceneParam(comparisonValue) + " TO " + toMaxLuceneParam(secondComparisonValue) + "]";
			break;

		default:
			break;
		}
		return query;
	}
	
	protected String toMinLuceneParam(T value) {
		return value.toString();
	}
	
	protected String toMaxLuceneParam(T value) {
		return value.toString();
	}

	@Override
	public String toString() {
		return toLuceneQuery();
	}

}
