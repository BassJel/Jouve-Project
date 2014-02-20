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
package com.doculibre.constellio.wicket.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.search.SearchedFacet;
import com.doculibre.constellio.entities.search.SimpleSearch;
import com.doculibre.constellio.services.AuthenticationServices;
import com.doculibre.constellio.services.UserServices;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceContext;
import com.doculibre.constellio.wicket.models.ReloadableEntityModel;

@SuppressWarnings("serial")
public class ConstellioSession extends AuthenticatedWebSession {

	private ReloadableEntityModel<ConstellioUser> userModel;

	private String password;
	private ListOrderedMap searchHistory = new ListOrderedMap();
	private List<SimpleSearch> combinedSearchHistory = new ArrayList<SimpleSearch>();

	private boolean portletMode;
	
	public ConstellioSession(Request request) {
		super(request);
	}

	@SuppressWarnings("unchecked")
	public List<SimpleSearch> getSearchHistory(String solrServerName) {
		List<SimpleSearch> searchHistorySolrServer = (List<SimpleSearch>) searchHistory.get(solrServerName);
		if (searchHistorySolrServer == null) {
			searchHistorySolrServer = new ArrayList<SimpleSearch>();
			searchHistory.put(solrServerName, searchHistorySolrServer);
		}
		return searchHistorySolrServer;
	}

	public List<SimpleSearch> getCombinedSearchHistory() {
		return combinedSearchHistory;
	}

	public void addSearchHistory(SimpleSearch simpleSearch) {
		boolean modification = false;

		List<SimpleSearch> searchHistorySolrServer = getSearchHistory(simpleSearch.getCollectionName());
		if (!searchHistorySolrServer.isEmpty()) {
			SimpleSearch lastSearch = searchHistorySolrServer.get(searchHistorySolrServer.size() - 1);
			if (lastSearch.getLuceneQuery().equals(simpleSearch.getLuceneQuery())) {
				SearchedFacet clusterLastSearch = lastSearch.getCluster();
				SearchedFacet clusterSimpleSearch = simpleSearch.getCluster();
				if ((clusterLastSearch == null && clusterSimpleSearch == null) || (clusterLastSearch != null && clusterLastSearch.equals(clusterSimpleSearch))) {
					modification = true;
				}
			}
		}
		if (!modification) {
			searchHistorySolrServer.add(simpleSearch);
		}

		// Combined :
		if (!combinedSearchHistory.isEmpty()) {
			SimpleSearch lastSearch = combinedSearchHistory.get(combinedSearchHistory.size() - 1);
			if (lastSearch.getLuceneQuery().equals(simpleSearch.getLuceneQuery()) && lastSearch.getCollectionName().equals(simpleSearch.getCollectionName())) {
				SearchedFacet clusterLastSearch = lastSearch.getCluster();
				SearchedFacet clusterSimpleSearch = simpleSearch.getCluster();
				if ((clusterLastSearch == null && clusterSimpleSearch == null) || (clusterLastSearch != null && clusterLastSearch.equals(clusterSimpleSearch))) {
					modification = true;
				}
			}
		}
		if (!modification) {
			combinedSearchHistory.add(simpleSearch);
		}
	}

	public void removeSearchHistory(String solrServerName, int index) {
		List<SimpleSearch> searchHistorySolrServer = getSearchHistory(solrServerName);
		if (searchHistorySolrServer.size() > index) {
			searchHistorySolrServer.remove(index);
		}
	}

	public void removeSearchHistory(int index) {
		if (combinedSearchHistory.size() > index) {
			combinedSearchHistory.remove(index);
		}
	}

	public static ConstellioSession get() {
		return (ConstellioSession) Session.get();
	}

	@Override
	public Locale getLocale() {
		Locale locale = null;

		List<Locale> supportedLocales = ConstellioSpringUtils.getSupportedLocales();
		if (supportedLocales.size() == 1) {
			locale = supportedLocales.get(0);
		} else {
			ConstellioUser user = (ConstellioUser) getUser();
			if (user == null) {
				locale = new Locale(super.getLocale().getLanguage());
			} else {
				locale = user.getLocale();
			}
		}

		return locale;
	}

	/**
	 * Can't override Session#setLocale(Locale) classe Session.
	 */
	public void changeLocale(Locale locale) {
		ConstellioUser user = (ConstellioUser) getUser();
		if (user == null) {
			setLocale(locale);
		} else {
			EntityManager entityManager = ConstellioPersistenceContext.getCurrentEntityManager();
			if (!entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().begin();
			}
			user.setLocale(locale);
			entityManager.persist(user);
			entityManager.getTransaction().commit();
		}
	}

	public ConstellioUser getUser() {
		if (!isSessionInvalidated() && userModel != null) {
			userModel.detach();
		}
		return userModel != null ? (ConstellioUser) userModel.getObject() : null;
	}

	@Override
	protected void detach() {
		if (userModel != null) {
			userModel.detach();
		}
		super.detach();
	}

	@Override
	public Roles getRoles() {
		if (isSignedIn()) {
			Set<String> roles = getUser().getRoles();
			return new Roles(roles.toArray(new String[] {}));
		}
		return null;
	}

	@Override
	public boolean authenticate(String username, String password) {
		AuthenticationServices authenticationServices = ConstellioSpringUtils.getAuthenticationServices();
		boolean authenticated = authenticationServices.authenticate(username, password);
		if (authenticated) {
			UserServices userServices = ConstellioSpringUtils.getUserServices();
			ConstellioUser user = userServices.get(username);
			userModel = new ReloadableEntityModel<ConstellioUser>(user);
			this.password = password;
		} else {
			if (userModel != null) {
				userModel.detach();
				userModel = null;
			}
		}
		// Check username and password
		return authenticated;
	}

	@Override
	public void signOut() {
		super.signOut();
		if (userModel != null) {
			userModel.detach();
		}
		userModel = null;
	}

	public String getPassword() {
		return password;
	}
	
	public boolean isPortletMode() {
		return portletMode;
	}
	
	public void setPortletMode(boolean portletMode) {
		this.portletMode = portletMode;
	}
}
