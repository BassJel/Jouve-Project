package com.doculibre.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;


public class LDAPGroup {
	public static final String COMMON_NAME = "cn";
	public static final String MEMBER = "member";
	public static final String[] FETCHED_ATTRIBUTES = { COMMON_NAME, MEMBER};
	
	private String distinguishedname;
	
	private List<String> ldapUsers = new ArrayList<String>();

	
	public LDAPGroup(String name) {
		super();
		this.distinguishedname = name;
	}

	public String getDistinguishedname() {
		return distinguishedname;
	}

	public void setDistinguishedname(String distinguishedname) {
		this.distinguishedname = distinguishedname;
	}
	
	public void addUser(String userId){
		if(!this.ldapUsers.contains(userId)){
			this.ldapUsers.add(userId);
		}
	}
	
	public List<String> getLdapUsers() {
		return Collections.unmodifiableList(this.ldapUsers);
	}

	@Override
	public String toString() {
		return "\t" +distinguishedname + "\n\tUsers :\n\t" + StringUtils.join(ldapUsers.toArray(), "\n\t");
	}
	
}