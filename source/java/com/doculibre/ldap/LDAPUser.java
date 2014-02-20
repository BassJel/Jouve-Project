package com.doculibre.ldap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LDAPUser {
	public static final String MEMBER_OF = "memberOf";
	public static final String USER_ACCOUNT_CONTROL = "userAccountControl";
	public static final String MAIL = "mail";
	public static final String NAME = "name";
	public static final String SAM_ACCOUNT_NAME = "sAMAccountName";
	public static final String GIVEN_NAME = "givenName";
	public static final String FAMILY_NAME = "sn";
	public static final String LAST_LOGON = "lastlogon";
	public static final String LIEU_TRAVAIL = "company"; // Peut aussi etre department
	//TODO ajouter dateDesactivation derniereAdresseIP
	
	public static final String[] FETCHED_ATTRIBUTES = { MEMBER_OF, USER_ACCOUNT_CONTROL, MAIL, NAME, SAM_ACCOUNT_NAME, GIVEN_NAME, FAMILY_NAME, LAST_LOGON, LIEU_TRAVAIL};
	
	private String id;
	private String account;
	private String name;
	private Boolean enabled;
	private String email;
	private String givenName;
	private String familyName;
	private Date lastLogon;
	private String lieuTravail;
	private List<LDAPGroup> userGroups = new ArrayList<LDAPGroup>();
	
	

	public void addGroup(LDAPGroup group){
		userGroups.add(group);
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<LDAPGroup> getUserGroups() {
		return userGroups;
	}
	public void setUserGroups(List<LDAPGroup> userGroups) {
		this.userGroups = userGroups;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public Date getLastLogon() {
		return lastLogon;
	}
	public void setLastLogon(Date lastLogon) {
		this.lastLogon = lastLogon;
	}
	public String getLieuTravail() {
		return lieuTravail;
	}
	public void setLieuTravail(String lieuTravail) {
		this.lieuTravail = lieuTravail;
	}

	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append("id :" + id + "\n");
		strb.append("account :" + account + "\n");
		strb.append("name :" + name + "\n");
		strb.append("email :" + email + "\n");
		strb.append("enabled :" + enabled + "\n");
		strb.append("lieuTravail :" + lieuTravail + "\n");
		strb.append("userGroups :\n" + StringUtils.join(userGroups.toArray(), "\n") );
		return strb.toString();
	}

}