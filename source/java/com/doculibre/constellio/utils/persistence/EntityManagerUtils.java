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
package com.doculibre.constellio.utils.persistence;

import javax.persistence.EntityManager;

import org.apache.wicket.Application;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.context.ManagedSessionContext;

import com.doculibre.constellio.utils.ConstellioAnnotationUtils;

public class EntityManagerUtils {

	private static SessionFactory sessionFactory;

	public static void setUp() {		
		AnnotationConfiguration config = new AnnotationConfiguration();
		config.setProperty("hibernate.current_session_context_class", "managed");
		config.setProperty("hibernate.c3p0.max_size", "20").setProperty(
				"hibernate.c3p0.timeout", "3000").setProperty(
				"hibernate.c3p0.idle_test_period", "300").setProperty("hibernate.hbm2ddl.auto", "update");

		ConstellioAnnotationUtils.addAnnotatedClasses(config);
		
		sessionFactory = config.buildSessionFactory();

		Application.set(new DataDummyWebApplication());
		
		org.hibernate.classic.Session hibernateSession = sessionFactory.openSession();
		hibernateSession.beginTransaction();
		ManagedSessionContext.bind(hibernateSession);
	}

	public static void tearDown() {
		try {
			if (ManagedSessionContext.hasBind(sessionFactory)) {
				EntityManager currentEntityManager = ConstellioPersistenceContext.getCurrentEntityManager();
				if (currentEntityManager.isOpen()) {
					try {
						if (currentEntityManager.getTransaction().isActive())
							currentEntityManager.getTransaction().rollback();
					} finally {
						currentEntityManager.close();
					}
				}	
				ManagedSessionContext.unbind(sessionFactory);
			}
		} finally {
			if (sessionFactory != null) {
				sessionFactory.close();
			}
		}
	}
	
	public static class DataDummyWebApplication extends DummyWebApplication {
		
	}

}
