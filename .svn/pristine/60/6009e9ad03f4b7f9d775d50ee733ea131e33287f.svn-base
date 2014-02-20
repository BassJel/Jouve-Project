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
package com.doculibre.constellio.services;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.doculibre.constellio.entities.FreeTextTag;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class FreeTextTagServicesTest extends
		BaseCRUDServicesTest<FreeTextTag> {

	@Test
	public void testGetString() {
		FreeTextTag newTag = new FreeTextTag();
		newTag.setFreeText("tag1");

		beginTransaction();
		services.makePersistent(newTag);
		commitTransaction();

		FreeTextTag tag = ((FreeTextTagServices)services).get("tag1");
		Assert.assertEquals(newTag.getId(), tag.getId());
	}

	@Test
	public void testSearch() {
		FreeTextTag newTag1 = new FreeTextTag();
		newTag1.setFreeText("tag1");

		FreeTextTag newTag2 = new FreeTextTag();
		newTag2.setFreeText("tag2");

		FreeTextTag newTag3 = new FreeTextTag();
		newTag3.setFreeText("lolo");

		beginTransaction();
		services.makePersistent(newTag1);
		services.makePersistent(newTag2);
		services.makePersistent(newTag3);
		commitTransaction();

		Set<FreeTextTag> res = ((FreeTextTagServices)services).search("tag1");
		Assert.assertEquals(1, res.size());

		res = ((FreeTextTagServices)services).search("tag*");
		Assert.assertEquals(2, res.size());

		res = ((FreeTextTagServices)services).search("*");
		Assert.assertEquals(3, res.size());

	}

	@Override
	protected FreeTextTagServices getServices() {
		return ConstellioSpringUtils.getFreeTextTagServices();
	}

	@Override
	public void constructSomeIncompleteEntities(List<FreeTextTag> entities) {
		//No free text
		FreeTextTag newTag1 = new FreeTextTag();
		entities.add(newTag1);
	}

	@Override
	public void constructSomeCompleteEntities(List<FreeTextTag> entities) {
		FreeTextTag tag = new FreeTextTag();
		tag.setFreeText("tag1");
		entities.add(tag);
		
		tag = new FreeTextTag();
		tag.setFreeText("tag2");
		entities.add(tag);
		
		tag = new FreeTextTag();
		tag.setFreeText("tag3");
		entities.add(tag);
	}

}
