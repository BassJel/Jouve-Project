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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ConstellioPersistenceUtils {

    private static final Logger LOG = Logger.getLogger(ConstellioPersistenceUtils.class.getName());

    public static void finishTransaction(boolean close) {
        EntityManager entityManager = null;
        EntityTransaction entityTransaction = null;
        try {
            entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            entityTransaction = entityManager.getTransaction();
            if (entityTransaction.isActive()) {
                if (!entityTransaction.getRollbackOnly()) {
                    entityTransaction.commit();
                } else {
                    entityTransaction.rollback();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Exception while trying to commit transaction", e);
            if (entityTransaction != null && entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
        } finally {
            if (close && entityManager != null) {
                entityManager.close();
            }
        }
    }

    public static void beginTransaction() {
        try {
            EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            if (!entityTransaction.isActive()) {
                entityTransaction.begin();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
