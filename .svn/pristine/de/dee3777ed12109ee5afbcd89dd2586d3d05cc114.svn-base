package com.doculibre.constellio.services;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.utils.ConstellioSpringUtils;
import com.doculibre.constellio.utils.persistence.ConstellioPersistenceUtils;
import com.doculibre.ldap.LDAPFastBind;
import com.doculibre.ldap.LDAPUser;
import com.doculibre.ldap.LDAPUtils;

public class AuthenticationServicesLDAP extends AuthenticationServicesImpl implements AuthenticationServices {

	private List<String> domains;
	private String url;
	private String type;
	private String searchBase;
	private String adminDN;
	private String adminPassword;

	@Override
	public boolean authenticate(String username, String password) {
		if (username.equals("admin")) {
			return super.authenticate(username, password);
		}
		for (String domain : domains) {
			LDAPFastBind ctx = new LDAPFastBind(this.url, this.type, this.searchBase, this.adminDN, this.adminPassword);
			try {
				String usernameDomain = username + "@" + domain;
				boolean authenticated = ctx.authenticate(usernameDomain, password);
				if (authenticated) {
					mergeUser(username, domain, ctx);
					return true;
				}
			} finally {
				ctx.close();
			}
		}
		return false;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private void mergeUser(String username, String domain, LDAPFastBind ctx) {
		try {
			ConstellioPersistenceUtils.beginTransaction();
			UserServices userServices = ConstellioSpringUtils.getUserServices();
			ConstellioUser user = new ConstellioUser(username, UUID.randomUUID().toString(), Locale.getDefault());
			user.setDomain(domain);
			if (userServices.get(username) == null) {
				//FIXME Need to query using an authorized user
				//LDAPUser ldapUser = LDAPUtils.getUserByUserName(username, ctx);
				user.setFirstName(username);
				user.setLastName("");
				//Adding new user
				userServices.makePersistent(user);
			}
		} finally {
			ConstellioPersistenceUtils.finishTransaction(true);
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSearchBase() {
		return searchBase;
	}

	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

	public String getAdminDN() {
		return adminDN;
	}

	public void setAdminDN(String adminDN) {
		this.adminDN = adminDN;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
}
