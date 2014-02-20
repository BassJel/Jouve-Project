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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.services.util.ServicesTestUtils;
import com.doculibre.constellio.solr.context.SolrCoreContext;
import com.doculibre.constellio.solr.context.SolrLogContext;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

/**
 * The prupose of this class is to simplify services testing. It is required to
 * implement these methods :
 * <ul>
 * <li>{@link #getServices()}</li>
 * <li>{@link #constructSomeCompleteEntities(List)}</li>
 * </ul>
 * Also, this is greatly recommended to understand these methods :
 * <ul>
 * <li>{@link #constructSomeIncompleteEntities(List)}</li>
 * <li>{@link #makeUnremoveable(int, ConstellioEntity)}</li>
 * <li>{@link #loadAllTestBaseData()}</li>
 * <li>{@link #loadGenericTestBaseData()}</li>
 * <li>{@link #getInitialEntityCount()}</li>
 * </ul>
 * And for specific test case, it can be usefull to understand these methods :
 * <ul>
 * <li>{@link #getCompleteEntities()}</li>
 * <li>{@link #loadAllTestBaseData()}</li>
 * </ul>
 * 
 * 
 * @author francisbaril
 * 
 * @param <T>
 *            The entity
 */
public abstract class BaseCRUDServicesTest<T extends ConstellioEntity> {

	protected BaseCRUDServices<T> services;
	protected EntityManager entityManager;
	protected List<T> completeEntities;
	protected ConnectorManager connectorManager;

	public BaseCRUDServicesTest() {
		super();
	}

	@BeforeClass
	public static void initTestClass() {
		// The test use a different persistence file, the web application BD is
		// untouched
		System.setProperty("base-persistence-file", "persistence_tests.xml");
		System.setProperty("test-bd-name", "constellio_derby");
	}

	@Before
	public void init() {
		ServicesTestUtils.init();
		entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		services = getServices();
		completeEntities = null;
		doTransaction(new Runnable() {
			@Override
			public void run() {
				loadAllTestBaseData();
			}
		});
	}

	@After
	public void cleanup() {
		SolrCoreContext.shutdown();
		SolrLogContext.shutdown();
		ServicesTestUtils.cleanup();
		connectorManager = null;
		completeEntities = null;
		entityManager = null;
	}

	/**
	 * 
	 * @return The number of entities in the BD before
	 *         {@link #testGenericServices()} begins
	 */
	protected int getInitialEntityCount() {
		return 0;
	}

	/**
	 * Test nearly all crud service methods using the
	 * {@link #constructSomeCompleteEntities(List)} and
	 * {@link #constructSomeIncompleteEntities(List)} which needs to be
	 * implemented
	 */
	@Test
	public final void testGenericServices() {

		int initialEntityCount = getInitialEntityCount();

		loadGenericTestBaseData();
		List<T> incompleteEntities = new ArrayList<T>();
		constructSomeIncompleteEntities(incompleteEntities);

		for (int i = 0; i < incompleteEntities.size(); i++) {
			try {
				beginTransaction();
				services.makePersistent(incompleteEntities.get(i));
				commitTransaction();
				fail("Should not be able to persist entity " + (i + 1) + " : "
						+ incompleteEntities.get(i).toString());
			} catch (Exception e) {
				// Expected
				rollBackTransaction();
			} finally {
				// Some entities are detached when an exception occur
				cleanup();
				init();
				loadGenericTestBaseData();
				incompleteEntities = new ArrayList<T>();
				constructSomeIncompleteEntities(incompleteEntities);
			}
		}

		assertEquals(initialEntityCount + 0, services.list().size());
		completeEntities = persistCompleteEntities();
		if (completeEntities.size() < 2) {
			fail("Need at least 2 complete entity to execute this test");
		}

		assertEquals(initialEntityCount + completeEntities.size(), services
				.list().size());
		assertEquals(1, services.list(1).size());
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("id", completeEntities.get(0).getId());
		assertEquals(1, services.list(properties).size());
		assertEquals(completeEntities.get(0), services.list(properties).get(0));
		assertEquals(completeEntities.get(0), services.get(properties));

		List<Long> ids = new ArrayList<Long>();
		for (T entity : completeEntities) {
			ids.add(entity.getId());
		}
		Collections.sort(ids);

		if (initialEntityCount == 0) {
			List<T> idASC = services.list("id", true);
			List<T> idDSC = services.list("id", false);
			assertEquals(idASC.size(), idDSC.size());
			for (int i = 0; i < idASC.size(); i++) {
				assertEquals(ids.get(i), idASC.get(i).getId());
				assertEquals(ids.get(idDSC.size() - i - 1), idDSC.get(i)
						.getId());
			}
		}

		assertEquals(initialEntityCount + completeEntities.size(), services
				.list().size());
		for (final T entity : completeEntities) {
			doTransaction(new Runnable() {
				@Override
				public void run() {
					assertTrue(services.isRemoveable(entity));
					services.makeTransient(entity);
				}
			});
		}

		assertEquals(initialEntityCount, services.list().size());
	}

	private final List<T> persistCompleteEntities() {
		List<T> completeEntities = new ArrayList<T>();
		constructSomeCompleteEntities(completeEntities);
		// Let's add some entities
		for (int i = 0; i < completeEntities.size(); i++) {
			try {
				final T entity = completeEntities.get(i);
				doTransaction(new Runnable() {
					@Override
					public void run() {
						services.makePersistent(entity);
					}
				});

			} catch (Exception e) {
				rollBackTransaction();
				throw new RuntimeException("Should be able to persist entity "
						+ (i + 1) + " : " + completeEntities.get(i).toString(),
						e);
			}
		}
		return completeEntities;
	}

	/**
	 * Test the isRemoveable service using the
	 * {@link #makeUnremoveable(int, ConstellioEntity)}, which needs to be
	 * implemented
	 */
	@Test
	public final void testIsRemoveable() {

		int i = 0;
		boolean end = false;
		while (!end) {
			try {

				final int caseNumber = i;
				TestCase.assertTrue(services.isRemoveable(getCompleteEntities()
						.get(0)));
				doTransaction(new Runnable() {
					@Override
					public void run() {
						makeUnremoveable(caseNumber,
								getCompleteEntities().get(0));
					}
				});
				TestCase.assertFalse(services
						.isRemoveable(getCompleteEntities().get(0)));

				cleanup();
				init();
				loadGenericTestBaseData();

				i++;
			} catch (Throwable t) {
				if (t.getMessage().contains("END-TEST")) {
					end = true;
				} else {
					throw new RuntimeException(
							"Failure occured for case #" + i, t);
				}
			}
		}

	}

	/**
	 * Used by {@link #testIsRemoveable()} to make a removable entity
	 * unremovable. The scenario number is incremented from 0 to N-1 (N is the
	 * number of different scenarios to make the entity unremovable). Each of
	 * those scenarios will be executed on the same removable entity without
	 * conflicts. <br>
	 * <br>
	 * The method should be implemented like this :
	 * 
	 * <pre>
	 * {@code}
	 *  switch (scenarioNumber) {
	 *      case 0:
	 *          entity.makeItUnremoveable();
	 *          break;
	 * 
	 *      case 1:
	 *          entity.makeItUnremoveable2();
	 *          break;
	 * 
	 *      case 2:
	 *          entity.makeItUnremoveable3();
	 *          break;
	 *          
	 *      default:
	 *          //All scenarios have been tested, the test is complete!
	 *          super.makeUnremoveable(caseNumber, entity);
	 *          break;
	 *  }
	 * </pre>
	 * 
	 * @param caseScenario
	 * @param entity
	 */
	protected void makeUnremoveable(int caseScenario, T entity) {
		throw new RuntimeException("END-TEST");
	}

	/**
	 * Construct some incomplete entities, {@link #testGenericServices()} will
	 * then ensure that they cannot be persisted.
	 * 
	 * @param entities
	 *            incomplete entities
	 */
	public abstract void constructSomeIncompleteEntities(List<T> entities);

	/**
	 * Construct some entities (at least 2), {@link #testGenericServices()} will
	 * then ensure that they can be persisted. <br>
	 * Generic service methods will also be tested with these entities
	 * 
	 * @param entities
	 *            complete entities
	 */
	public abstract void constructSomeCompleteEntities(List<T> entities);

	/**
	 * Load all prerequired data for {@link #testGenericServices()}
	 */
	public void loadGenericTestBaseData() {
	}

	/**
	 * Load all prerequired data for all tests (including custom ones)
	 */
	public void loadAllTestBaseData() {
		ConstellioSpringUtils
				.setApplicationContext(new ClassPathXmlApplicationContext(
						"constellio.xml") {
					@Override
					public Object getBean(String name) throws BeansException {
						if (name.equals("connectorManagerServices")) {
							return new ConnectorManagerServicesImpl(
									entityManager) {
								@Override
								public void synchronizeWithDatabase(
										ConnectorManager connectorManager) {
								}
							};

						} else {
							return super.getBean(name);
						}
					}
				});

		ConstellioSpringUtils.getConstellioInitServices().init();
		connectorManager = ConstellioSpringUtils.getConnectorManagerServices()
				.getDefaultConnectorManager();
	}

	/**
	 * 
	 * @return The service, it should always be
	 *         ConstellioSpringUtils.getMyServices();
	 */
	protected abstract BaseCRUDServices<T> getServices();

	/**
	 * Begin a new transaction if there's no active one
	 */
	protected final void beginTransaction() {
		if (!entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().begin();
		}
	}

	/**
	 * Commit the current transaction
	 */
	protected final void commitTransaction() {
		entityManager.getTransaction().commit();
	}

	/**
	 * Rollback the current transaction
	 */
	protected final void rollBackTransaction() {
		try {
			entityManager.getTransaction().rollback();
		} catch (Exception e) {
		}
	}

	/**
	 * Execute this runnable in a transaction and ensure it is successfull
	 * 
	 * @param operations
	 */
	protected final void doTransaction(Runnable operations) {
		doTransaction(true, operations);
	}

	/**
	 * Execute this runnable in a transaction and ensure it is successfull
	 * Dependtly of successExpected attribute
	 * 
	 * @param successExpected
	 * @param operations
	 */
	protected final void doTransaction(boolean successExpected,
			Runnable operations) {
		beginTransaction();
		try {
			operations.run();
			commitTransaction();
			assertTrue(successExpected);

		} catch (Exception e) {
			rollBackTransaction();
			if (successExpected) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @return the persisted entities constructed in
	 *         {@link #constructSomeCompleteEntities(List)} <br>
	 *         This method can be used with custom test case for simplified
	 *         entity construction
	 */
	protected final List<T> getCompleteEntities() {
		if (completeEntities == null) {
			completeEntities = persistCompleteEntities();
		}
		return completeEntities;
	}

}
