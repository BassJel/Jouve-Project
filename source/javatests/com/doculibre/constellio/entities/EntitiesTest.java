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
package com.doculibre.constellio.entities;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.doculibre.constellio.utils.persistence.EntityManagerUtils;

public class EntitiesTest {

	@Before
	public void setUp() throws Exception {
		EntityManagerUtils.setUp();
	}

	@After
	public void tearDown() throws Exception {
		EntityManagerUtils.tearDown();
	}

	@Test
	public void test() {
		
	}

}
