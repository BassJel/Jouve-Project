package com.doculibre.ldap;

/**
 * ldapfastbind.java
 * 
 * Sample JNDI application to use Active Directory LDAP_SERVER_FAST_BIND connection control
 * Based on https://forums.oracle.com/forums/thread.jspa?threadID=1155584
 */

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;

public class LDAPFastBind {

	@SuppressWarnings("rawtypes")
	public Hashtable env = null;
	public LdapContext ctx = null;
	public Control[] connCtls = null;

	private String type;
	private String searchBase;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LDAPFastBind(String ldapurl, String type, String searchBase, String adminDn, String adminPassword) {
		env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.PROVIDER_URL, ldapurl);
		env.put(Context.REFERRAL, "follow");
		
		this.type = type;
		this.searchBase = searchBase;
		if ("edirectory".equals(type)) {
			if (!StringUtils.contains(adminDn.toLowerCase(), "cn=")) {
				throw new IllegalArgumentException("Invalid adminDn:" + adminDn);
			}
			//Prepare authentication for dn retrieval
			this.authenticate(adminDn, adminPassword);
		}
	}

	public boolean authenticate(String username, String password) {
		// See
		// http://stackoverflow.com/questions/12359831/java-ldap-make-it-not-ignore-blank-passwords
		if (StringUtils.isEmpty(password)) {
			return false;
		}
		String domain = StringUtils.substringAfter(username, "@");
		boolean activeDirectory;
		if ("edirectory".equals(type)) {
			if (!StringUtils.contains(username.toLowerCase(), "cn=")) {
				String usernameBeforeDomain = StringUtils.substringBefore(username, "@");
				String dn = dnForUser(usernameBeforeDomain);
				if (dn != null) {
					username = dn;
				}
			}
			activeDirectory = false;
		} else if (StringUtils.contains(domain.toLowerCase(), "cn=") || StringUtils.contains(domain.toLowerCase(), "ou=") || StringUtils.contains(domain.toLowerCase(), "c=") || StringUtils.contains(domain.toLowerCase(), "o=")) {
			activeDirectory = false;
		} else {
			activeDirectory = true;
		}

		if (ctx == null) {
			if (activeDirectory) {
				connCtls = new Control[] { new FastBindConnectionControl() };
			} else {
				connCtls = new Control[] {};
			}

			// first time we initialize the context, no credentials are supplied
			// therefore it is an anonymous bind.

			try {
				ctx = new InitialLdapContext(env, connCtls);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
		}

		String[] securityPrincipals;
		if ("edirectory".equals(type)) {
			securityPrincipals = new String[] { username };
		} else if (activeDirectory) {
			securityPrincipals = new String[] { username };
		} else {
			String[] prefixes = new String[] { "uid=", "cn=" };
			securityPrincipals = new String[prefixes.length];
			for (int i = 0; i < prefixes.length; i++) {
				String prefix = prefixes[i];
				String usernameBeforeDomain = StringUtils.substringBefore(username, "@");
				StringBuffer securityPrincipalSB = new StringBuffer();
				securityPrincipalSB.append(prefix);
				securityPrincipalSB.append(usernameBeforeDomain);
				securityPrincipalSB.append(",");
				securityPrincipalSB.append(domain);

				securityPrincipals[i] = securityPrincipalSB.toString();
			}
		}

		try {
			for (String securityPrincipal : securityPrincipals) {
				ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, securityPrincipal);
				ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
				try {
					ctx.reconnect(connCtls);
					// System.out.println(username + " is authenticated");
					return true;
				} catch (Exception e) {
					//Silent
				}
			}
			return false;
		} catch (AuthenticationException e) {
			// System.out.println(username + " is not authenticated");
			return false;
		} catch (NamingException e) {
			// System.out.println(username + " is not authenticated");
			return false;
		}
	}

	public void close() {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				throw new RuntimeException("Context close failure ", e);
			}
		}
	}

	public LdapContext getDirContext() {
		return this.ctx;
	}

	private String dnForUser(String cnORuid) {
		try {
			DirContext dirContext = this.getDirContext();

			String[] returnAttribute = { "dn" };
			SearchControls srchControls = new SearchControls();
			srchControls.setReturningAttributes(returnAttribute);
			srchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String searchFilter = "(&(objectClass=inetOrgPerson)&(|(uid=" + cnORuid + ")(cn=" + cnORuid + ")))";

			NamingEnumeration<SearchResult> srchResponse = dirContext.search(this.searchBase, searchFilter, srchControls);
			if (srchResponse.hasMore()) {
				return srchResponse.next().getNameInNamespace();
			}
		} catch (NamingException namEx) {
			namEx.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		LDAPFastBind ldapFastBind = new LDAPFastBind("ldap://localhost:389", "edirectory", "o=MINISTERE", "cn=adminLDAP-samba,o=ministere", "areyouanengland");
		System.out.println(ldapFastBind.authenticate("adminLDAP-samba", "areyouanengland"));

		System.out.println(ldapFastBind.dnForUser("adminLDAP-samba"));

		// LDAPFastBind ldapFastBind = new LDAPFastBind("ldap://localhost:389");
		// System.out.println(ldapFastBind.authenticate("user@domain",
		// "password"));
		// System.out.println(ldapFastBind.authenticate("CN=User,OU=Users,DC=domain,DC=local",
		// "password"));
		// System.out.println(ldapFastBind.authenticate("user@cptaq.local",
		// "e"));
		// System.out.println(ldapFastBind.authenticate("sadasdaf", ""));

	}

	@SuppressWarnings("serial")
	class FastBindConnectionControl implements Control {

		public byte[] getEncodedValue() {
			return null;
		}

		public String getID() {
			return "1.2.840.113556.1.4.1781";
		}

		public boolean isCritical() {
			return true;
		}
	}
}