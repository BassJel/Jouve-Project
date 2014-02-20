package com.doculibre.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.springframework.util.Assert;

public class LDAPUtils {
//	private static final long WINDOWS_FILE_TIME_FORMAT_BASE_DATE = 11644473600000L;
//
//	private LDAPUtils() {
//	}
//
//	public static List<LDAPUser> getAllUsers(LDAPFastBind ldapFastBind, String contextName) {
//		LdapContext ctx = ldapFastBind.getDirContext();
//		if (contextName == null || contextName.isEmpty()) {
//			String baseCtx;
//			try {
//				baseCtx = getDefaultNamingContext(ctx);
//				return browseUsersFromBaseContext(ctx, baseCtx);
//			} catch (NamingException e) {
//				throw new RuntimeException(e);
//			}
//		} else {
//			try {
//				return searchUsersFromContext(ctx, contextName);
//			} catch (NamingException e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}
//
//	public static List<LDAPUser> getAllUsers(String ldapurl, String user, String password, List<String> baseContextList) {
//		LDAPFastBind ldapFastBind = new LDAPFastBind(ldapurl);
//		boolean authenticated = ldapFastBind.authenticate(user, password);
//		if (!authenticated) {
//			throw new RuntimeException("Could not connect with given configuration");
//		}
//		List<LDAPUser> returnList;
//		if (baseContextList == null || baseContextList.isEmpty()) {
//			returnList = getAllUsers(ldapFastBind, null);
//		} else {
//			returnList = new ArrayList<LDAPUser>();
//			for (String baseContext : baseContextList) {
//				Collection<? extends LDAPUser> currentfetchedUsers = getAllUsers(ldapFastBind, baseContext);
//				returnList.addAll(currentfetchedUsers);
//			}
//		}
//		ldapFastBind.close();
//		return returnList;
//	}
//
//	public static LDAPFastBind connectToLDAP(List<String> domains, String ldapurl, String user, String password) {
//		LDAPFastBind ldapFastBind = new LDAPFastBind(ldapurl);
//		boolean authenticated = false;
//		for (String domain : domains) {
//			String nomUtilisateurDomaine = user + "@" + domain;
//			authenticated = ldapFastBind.authenticate(nomUtilisateurDomaine, password);
//			if (authenticated) {
//				break;
//			}
//		}
//		authenticated = ldapFastBind.authenticate(user, password);
//		if (!authenticated) {
//			throw new RuntimeException("Could not connect with given configuration");
//		}
//		return ldapFastBind;
//	}
//
//	public static List<LDAPGroup> getAllGroups(LDAPFastBind ldapFastBind, List<String> baseContextList) {
//		List<LDAPGroup> returnList;
//		if (baseContextList == null || baseContextList.isEmpty()) {
//			throw new RuntimeException("At least one context requiered");
//		} else {
//			returnList = new ArrayList<LDAPGroup>();
//			LdapContext ctx = ldapFastBind.getDirContext();
//			for (String baseContext : baseContextList) {
//				Collection<? extends LDAPGroup> currentfetchedGroups;
//				try {
//					currentfetchedGroups = searchGroupsFromContext(ctx, baseContext);
//				} catch (NamingException e) {
//					throw new RuntimeException(e);
//				}
//				returnList.addAll(currentfetchedGroups);
//			}
//		}
//		return returnList;
//	}
//
//	@SuppressWarnings("unchecked")
//	public static String getDefaultNamingContext(DirContext dirCtxt) throws NamingException {
//		Attributes attributes = dirCtxt.getAttributes(dirCtxt.getNameInNamespace());
//
//		Attribute attribute = attributes.get("defaultNamingContext");
//		NamingEnumeration<String> all = (NamingEnumeration<String>) attribute.getAll();
//		if (all.hasMoreElements()) {
//			return all.next();
//		} else {
//			throw new RuntimeException("No Default Naming Context!");
//		}
//	}
//
//	// Util lorsqu'on n'a pas de context name
//	public static List<LDAPUser> browseUsersFromBaseContext(DirContext ctx, String baseContextName) throws NamingException {
//		List<LDAPUser> users = new ArrayList<LDAPUser>();
//		NamingEnumeration<?> contentsEnum = ctx.list(baseContextName);
//		try {
//			while (contentsEnum.hasMore()) {
//				NameClassPair ncp = (NameClassPair) contentsEnum.next();
//				String contentName = ncp.getName();
//				String subContentName = contentName + "," + baseContextName;
//				Attributes attr1 = ctx.getAttributes(subContentName, new String[] { "objectcategory" });
//				if (attr1.get("objectcategory").toString().indexOf("CN=Person") == -1) {
//					// subContexts
//					List<LDAPUser> subUsers = browseUsersFromBaseContext(ctx, subContentName);
//					users.addAll(subUsers);
//				} else {
//					try {
//						Attributes attrs = ctx.getAttributes(subContentName, LDAPUser.FETCHED_ATTRIBUTES);//
//						LDAPUser user = buildLDAPUser(subContentName, attrs);
//						users.add(user);
//					} catch (NamingException ne) {
//						ne.printStackTrace();
//					}
//				}
//			}
//		} catch (PartialResultException e) {
//			System.out.println("Retour a la racine :" + e.getMessage());
//			return users;
//		}
//		return users;
//	}
//
//	public static List<LDAPGroup> searchGroupsFromContext(DirContext ctx, String groupsContainer) throws NamingException {
//		List<LDAPGroup> groups = new ArrayList<LDAPGroup>();
//		SearchControls ctls = new SearchControls();
//		ctls.setReturningAttributes(LDAPGroup.FETCHED_ATTRIBUTES);
//		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
//		NamingEnumeration<?> answer = ctx.search(groupsContainer, "(objectclass=group)", ctls);
//		while (answer.hasMore()) {
//			SearchResult rslt = (SearchResult) answer.next();
//			Attributes attrs = rslt.getAttributes();
//			LDAPGroup group = buildLDAPGroup(attrs);
//			groups.add(group);
//		}
//		return groups;
//	}
//
//	public static List<LDAPUser> searchUsersFromContext(DirContext ctx, String usersContainer) throws NamingException {
//		List<LDAPUser> users = new ArrayList<LDAPUser>();
//		SearchControls ctls = new SearchControls();
//		ctls.setReturningAttributes(LDAPUser.FETCHED_ATTRIBUTES);
//		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
//		NamingEnumeration<?> answer = ctx.search(usersContainer, "(objectclass=person)", ctls);
//		while (answer.hasMore()) {
//			SearchResult rslt = (SearchResult) answer.next();
//			Attributes attrs = rslt.getAttributes();
//			LDAPUser user = buildLDAPUser("", attrs);
//			users.add(user);
//		}
//		return users;
//	}
//
//	private static LDAPGroup buildLDAPGroup(Attributes attrs) throws NamingException {
//		Attribute groupNameAttribute = attrs.get(LDAPGroup.COMMON_NAME);
//		if (groupNameAttribute != null && groupNameAttribute.size() > 0) {
//			String groupName = (String) groupNameAttribute.get(0);
//			LDAPGroup returnGroup = new LDAPGroup(groupName);
//			Attribute members = attrs.get(LDAPGroup.MEMBER);
//			if (members != null) {
//				for (int i = 0; i < members.size(); i++) {
//					String userId = (String) members.get(i);
//					returnGroup.addUser(userId);
//				}
//			}
//			return returnGroup;
//		}
//		return null;
//	}
//
//	public static List<LDAPUser> buildUsersOfgroup(LDAPGroup group, LDAPFastBind ldapFastBind) {
//		List<LDAPUser> returnUsers = new ArrayList<LDAPUser>();
//		Assert.notNull(group);
//		for (String userId : group.getLdapUsers()) {
//			LDAPUser currentUser = getUser(userId, ldapFastBind);
//			returnUsers.add(currentUser);
//		}
//		return returnUsers;
//	}
//
//	public static LDAPUser getUser(String userId, LDAPFastBind ldapFastBind) {
//		LdapContext ctx = ldapFastBind.getDirContext();
//		Attributes attrs;
//		try {
//			attrs = ctx.getAttributes(userId, LDAPUser.FETCHED_ATTRIBUTES);
//			LDAPUser user = buildLDAPUser(userId, attrs);
//			return user;
//		} catch (NamingException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public static LDAPUser getUserByUserName(String userName, LDAPFastBind ldapFastBind) {
//		LdapContext ctx = ldapFastBind.getDirContext();
//		try {
//
//			SearchControls sc = new SearchControls();
//			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
//			String filter = "(name=" + userName + ")";
//			String baseCtx = getDefaultNamingContext(ctx);
//
//			NamingEnumeration<SearchResult> results = ctx.search(baseCtx, filter, sc);
//
//			if (results.hasMoreElements()) {
//				SearchResult next = results.next();
//				return buildLDAPUser(next.getName(), next.getAttributes());
//			}
//		} catch (NamingException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public static LDAPUser buildLDAPUser(String userId, Attributes attrs) throws NamingException {
//		LDAPUser returnUser = new LDAPUser();
//		Attribute groupsAttribute = attrs.get(LDAPUser.MEMBER_OF);
//		Attribute bitsAttribute = attrs.get(LDAPUser.USER_ACCOUNT_CONTROL);
//		Attribute mailAttribute = attrs.get(LDAPUser.MAIL);
//		Attribute nameAttribute = attrs.get(LDAPUser.NAME);
//		Attribute accountAttribute = attrs.get(LDAPUser.SAM_ACCOUNT_NAME);
//		Attribute givenNameAttribute = attrs.get(LDAPUser.GIVEN_NAME);
//		Attribute familyNameAttribute = attrs.get(LDAPUser.FAMILY_NAME);
//		Attribute lieuTravailAttribute = attrs.get(LDAPUser.LIEU_TRAVAIL);
//		Attribute lastLogonAttribute = attrs.get(LDAPUser.LAST_LOGON);
//		if (accountAttribute != null) {
//			for (int i = 0; i < accountAttribute.size(); i++) {
//				String account = (String) accountAttribute.get(i);
//				returnUser.setAccount(account);
//			}
//		}
//		if (groupsAttribute != null) {
//			for (int i = 0; i < groupsAttribute.size(); i++) {
//				String group = (String) groupsAttribute.get(i);
//				returnUser.addGroup(new LDAPGroup(group));
//			}
//		}
//		if (bitsAttribute != null) {
//			long lng = Long.parseLong(bitsAttribute.get(0).toString());
//			long secondBit = lng & 2; // get bit 2
//			if (secondBit == 0) {
//				boolean enabled = true;
//				returnUser.setEnabled(enabled);
//			}
//		}
//		if (mailAttribute != null) {
//			for (int i = 0; i < mailAttribute.size(); i++) {
//				String email = (String) mailAttribute.get(i);
//				returnUser.setEmail(email);
//			}
//		}
//		if (nameAttribute != null) {
//			for (int i = 0; i < nameAttribute.size(); i++) {
//				String name = (String) nameAttribute.get(i);
//				returnUser.setName(name);
//			}
//		}
//		if (givenNameAttribute != null) {
//			for (int i = 0; i < givenNameAttribute.size(); i++) {
//				String givenName = (String) givenNameAttribute.get(i);
//				returnUser.setGivenName(givenName);
//			}
//		}
//		if (familyNameAttribute != null) {
//			for (int i = 0; i < familyNameAttribute.size(); i++) {
//				String familyName = (String) familyNameAttribute.get(i);
//				returnUser.setFamilyName(familyName);
//			}
//		}
//		if (lieuTravailAttribute != null) {
//			for (int i = 0; i < lieuTravailAttribute.size(); i++) {
//				String lieuTravail = (String) lieuTravailAttribute.get(i);
//				returnUser.setLieuTravail(lieuTravail);
//			}
//		}
//		if (lastLogonAttribute != null) {
//			for (int i = 0; i < lastLogonAttribute.size(); i++) {
//				long date = Long.parseLong((String) lastLogonAttribute.get(i));
//				if (date != 0L) {
//					Date lastLogon = new Date(date / 10000 - WINDOWS_FILE_TIME_FORMAT_BASE_DATE);
//					returnUser.setLastLogon(lastLogon);
//				}
//			}
//		}
//
//		returnUser.setId(userId);
//		return returnUser;
//	}
//
//	public static void main(String[] args) throws Exception {
//		LDAPFastBind ldapFastBind = new LDAPFastBind("ldap://10.151.1.1:389");
//		boolean authentifie = ldapFastBind.authenticate("srvAlfresco", "qwerty");//
//		System.out.println(authentifie);
//		// List<LDAPUser> returnList = getAllUsers(ldapFastBind);
//		// ldapFastBind.getDirContext(),
//		// "OU=Comptes de service,OU=Administration,DC=FADQ,DC=QC"
//		// List<String> baseContextList =
//		// Arrays.asList("OU=Comptes de service,OU=Administration,DC=FADQ,DC=QC");
//		// List<LDAPUser> returnList = getAllUsers("ldap://10.151.1.1:389",
//		// "srvAlfresco", "qwerty",
//		// Arrays.asList("OU=Comptes de service,OU=Administration,DC=FADQ,DC=QC"));//"CN=Users,DC=test,DC=doculibre,DC=ca"
//		//
//		// for(LDAPUser user : returnList){
//		// System.out.println(user);
//		// // LDAPUser fetchedUser = getUser(user.getId(), ldapFastBind);
//		// // System.out.println(fetchedUser);
//		// // fetchedUser = getUserByUserName(user.getName(), ldapFastBind);
//		// // System.out.println(fetchedUser);
//		// }
//		// System.out.println(LDAPUtils.connectToLDAP(Arrays.asList(""),
//		// "ldap://10.151.1.1:389", "srvAlfresco", "qwerty"));
//		// OU=Administration,DC=FADQ,DC=QC
//		List<LDAPGroup> ldapGroups = LDAPUtils.getAllGroups(ldapFastBind, Arrays.asList("OU=Groupes,DC=FADQ,DC=QC"));// GALF_test,
//		// List<LDAPGroup> groups =
//		// searchGroupsFromContext(ldapFastBind.getDirContext(),
//		// "OU=Groupes,DC=FADQ,DC=QC");//"OU=ALF_GRP,OU=securite,OU=Groupes,DC=test,DC=doculibre,DC=ca");
//		for (LDAPGroup group : ldapGroups) {
//			System.out.println(group);
//		}
//
//		// LDAPUser fetchedUser = getUserByUserName("lol", ldapFastBind);
//		// System.out.println(fetchedUser);
//		// fetchedUser = getUserByUserName("user1Org1", ldapFastBind);
//		// System.out.println(fetchedUser);
//		System.out.println("fin");
//		ldapFastBind.close();
//	}

}