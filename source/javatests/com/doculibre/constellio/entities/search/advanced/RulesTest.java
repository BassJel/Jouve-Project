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
package com.doculibre.constellio.entities.search.advanced;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import com.doculibre.constellio.entities.search.advanced.enums.BooleanEquation;
import com.doculibre.constellio.entities.search.advanced.enums.MathEquation;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.AbstractNumericSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.BooleanSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DateSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.DoubleSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.FloatSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.IntegerSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.LongSearchRule;
import com.doculibre.constellio.entities.search.advanced.indexFieldRules.TextSearchRule;
import com.doculibre.constellio.utils.SimpleParams;


public class RulesTest {

	@Test
	public void test() {

		SearchRulesGroup g = new SearchRulesGroup();
		g.setEquation(BooleanEquation.AND);

		g.addNestedSearchRule(new SearchRulesGroup());
		g.addNestedSearchRule(new TextSearchRule());
		g.addNestedSearchRule(new DateSearchRule());
		g.addNestedSearchRule(new IntegerSearchRule());
		g.addNestedSearchRule(new LongSearchRule());
		g.addNestedSearchRule(new FloatSearchRule());
		g.addNestedSearchRule(new DoubleSearchRule());
		g.addNestedSearchRule(new BooleanSearchRule());

		TextSearchRule r1 = new TextSearchRule();
		r1.setIndexFieldName("A");
		r1.setTextValue("val");
		AbstractNumericSearchRule<Date> r2 = new DateSearchRule();
		r2.setIndexFieldName("B");
		r2.setComparison(new Date());
		r2.setMaxRange(new Date());
		r2.setEquation(MathEquation.RANGE);
		AbstractNumericSearchRule<Integer> r3 = new IntegerSearchRule();
		r3.setIndexFieldName("C");
		r3.setComparison(1);
		r3.setMaxRange(4);
		r3.setEquation(MathEquation.RANGE);
		AbstractNumericSearchRule<Long> r4 = new LongSearchRule();
		r4.setIndexFieldName("D");
		r4.setComparison(8l);
		r4.setEquation(MathEquation.GT);
		AbstractNumericSearchRule<Float> r5 = new FloatSearchRule();
		r5.setIndexFieldName("E");
		r5.setComparison(8.6f);
		r5.setEquation(MathEquation.LT);
		AbstractNumericSearchRule<Double> r6 = new DoubleSearchRule();
		r6.setIndexFieldName("F");
		r6.setComparison(8.3);
		r6.setEquation(MathEquation.EQ);
		
		BooleanSearchRule r7 = new BooleanSearchRule();
		r7.setIndexFieldName("G");
		r7.setValue(true);
		
		g.addNestedSearchRule(r1);
		g.addNestedSearchRule(r2);
		g.addNestedSearchRule(r3);
		g.addNestedSearchRule(r4);
		g.addNestedSearchRule(r5);
		g.addNestedSearchRule(r6);
		g.addNestedSearchRule(r7);
		
		SearchRule gClone = g.cloneRule();
		SimpleParams params = gClone.toSimpleParams(false);
		TestCase.assertEquals(g, gClone);
		SearchRule g2 = SearchRulesFactory.constructSearchRule(params, null, AbstractSearchRule.ROOT_PREFIX);
		TestCase.assertEquals(g, g2);
		SearchRule g2Clone = g2.cloneRule();
		TestCase.assertEquals(g, g2Clone);
		TestCase.assertTrue(g.isValid());
	}
	
	@Test
	public void test2() {
		SimpleParams params = new SimpleParams();
		params.add("advf_type", "group");
		params.add("advf_eq", "AND");
		
		params.add("advf_1_type", "group");
		params.add("advf_1_eq", "AND");
		
		params.add("advf_2_type", "group");
		params.add("advf_2_eq", "AND");
		
		SearchRulesGroup g = (SearchRulesGroup) SearchRulesFactory.constructSearchRule(params, null, AbstractSearchRule.ROOT_PREFIX);
		TestCase.assertEquals(2, g.getNestedRules().size());
	}

}
