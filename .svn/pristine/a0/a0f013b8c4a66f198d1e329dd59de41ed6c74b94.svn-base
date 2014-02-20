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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.doculibre.constellio.entities.Cache;
import com.doculibre.constellio.services.util.ServicesTestUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

/**
 * This test ensure the test environment is correctly configured
 * 
 * @author francisbaril
 * 
 */
public class TestEnvironmentTest {

	EntityManager entityManager;

	@Before
	public void init() {
		ServicesTestUtils.init();
		entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
	}

	@After
	public void cleanup() {
		ServicesTestUtils.cleanup();
	}

	@Test
	public void testIsolation1() {
		String req = "from " + Cache.class.getSimpleName() + " o";

		Query queryBefore = entityManager.createQuery(req);
		// The cache created in testIsolation2 should not exist
		TestCase.assertEquals(0, queryBefore.getResultList().size());

		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		Cache c = new Cache();
		entityManager.persist(c);
		try {
			entityManager.getTransaction().commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);

		}

		Query queryAfter = entityManager.createQuery(req);
		TestCase.assertEquals(1, queryAfter.getResultList().size());
	}

	@Test
	public void testIsolation2() {
		String req = "from " + Cache.class.getSimpleName() + " o";

		Query queryBefore = entityManager.createQuery(req);
		// The cache created in testIsolation1 should not exist
		TestCase.assertEquals(0, queryBefore.getResultList().size());

		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
		Cache c = new Cache();
		entityManager.persist(c);
		try {
			entityManager.getTransaction().commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);

		}

		Query queryAfter = entityManager.createQuery(req);
		TestCase.assertEquals(1, queryAfter.getResultList().size());
	}
}
