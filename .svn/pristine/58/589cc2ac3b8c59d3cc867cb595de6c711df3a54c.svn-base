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
package com.doculibre.constellio.wicket.application;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.wicket.Page;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;

import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;

public class PersistenceAwareWebRequestCycle extends WebRequestCycle {

	public PersistenceAwareWebRequestCycle(WebApplication application, WebRequest request, Response response) {
		super(application, request, response);
	}

	@Override
	protected void onEndRequest() {
		EntityManager currentEntityManager = ConstellioPersistenceContext.getCurrentEntityManager();
		if (currentEntityManager.isOpen()) {
			try {
				EntityTransaction entityTransaction = currentEntityManager.getTransaction();
				if (entityTransaction.isActive()) {
					try {
                        entityTransaction.rollback();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				currentEntityManager.close();
			}
		}
	}

    @Override
    public Page onRuntimeException(Page page, RuntimeException e) {
//        if (!(page instanceof SearchExceptionHandlingPage)) {
//            return new SearchExceptionHandlingPage();
//        } else {
            return super.onRuntimeException(page, e);
//        }
    }
    
}
