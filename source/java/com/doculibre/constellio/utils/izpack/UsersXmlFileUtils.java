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
package com.doculibre.constellio.utils.izpack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;

import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.utils.ClasspathUtils;
import com.doculibre.constellio.utils.ConstellioSpringUtils;

public class UsersXmlFileUtils {
	public static final String DEFAULT_USERS_FILE = ClasspathUtils.getWebinfDir() + File.separator + "initUsersList.xml";

	private static final String FIRST_NAME = "firstName";
	private static final String USER = "user";
	private static final String ROLES = "roles";
	private static final String LAST_NAME = "lastName";
	private static final String LOGIN = "login";
	private static final String PASSWORD_HASH = "password";
	private static final String LOCALE = "locale";
	private static final String ROLE = "role";
	private static final String VALUE = "value";
	
	//empty file including its DTD
	public static final String[] emptyFileLines =  {
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", 
		"<!DOCTYPE users [",
		"<!ELEMENT users (user*)>",
		"<!ELEMENT " + USER + " (" + ROLES + "?)>",
			"\t<!ATTLIST " + USER + " " + FIRST_NAME + " CDATA #REQUIRED>",
			"\t<!ATTLIST " + USER + " " + LAST_NAME + " CDATA #REQUIRED>",
			"\t<!ATTLIST " + USER + " " + LOGIN + " CDATA #REQUIRED>",
			"\t<!ATTLIST " + USER + " " + PASSWORD_HASH + " CDATA #REQUIRED>",
			"\t<!ATTLIST " + USER + " " + LOCALE + " CDATA #IMPLIED>",
		"<!ELEMENT " + ROLES + " (" + ROLE + "+)>",
		"<!ELEMENT " + ROLE + "  EMPTY>",
			"\t<!ATTLIST " + ROLE  + " " + VALUE  + " CDATA #REQUIRED>",
		"]>",
		"<users>",
		"</users>"
	};


	
	//Dans le fichier XML les mots de passes sont encodés
	
	private UsersXmlFileUtils(){
		
	}
	
	@SuppressWarnings("unchecked")
	public static List<ConstellioUser> readUsers(String fileName){
		List<ConstellioUser> returnList = new ArrayList<ConstellioUser>();
		
		File xmlFile = new File(fileName);
		
		Document xmlDocument;
		if (!xmlFile.exists()){
			return returnList;
		}
		try {
			xmlDocument = new SAXReader().read(xmlFile);
		} catch (DocumentException e) {
			e.printStackTrace();
			return returnList;
		}

        Element root = xmlDocument.getRootElement();
        for (Iterator<Element> it = root.elementIterator(USER); it.hasNext();) {
        	Element currentUser = it.next();
        	returnList.add(toConstellioUser(currentUser));
        	
        }
		return returnList;
	}
	
	public static List<ConstellioUser> readUsers(){
		return readUsers(DEFAULT_USERS_FILE );
	}
	
	public static void createEmptyUsersFile(){
		createEmptyUsersFile(DEFAULT_USERS_FILE);
	}

	public static void createEmptyUsersFile(String fileName) {
		File xmlFile = new File(fileName);
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(xmlFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			IOUtils.writeLines(Arrays.asList(emptyFileLines), System.getProperty("line.separator"), writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(writer);
		}
		
			
	}
	
	public static void addUserTo(ConstellioUser constellioUser, String fileName){
		Document xmlDocument;
		try {
			xmlDocument = new SAXReader().read(fileName);
			Element root = xmlDocument.getRootElement();
	        
			Element user = toXmlElement(constellioUser);
			root.add(user);
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			
			File xmlFile = new File(fileName);
			//FIXME réecrire la DTD
			//xmlDocument.addDocType(arg0, arg1, arg2)
	        XMLWriter writer = new XMLWriter(new FileOutputStream(xmlFile), format);
	        
	        writer.write(xmlDocument);
	        writer.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        
	}
	
	public static void addUserTo(ConstellioUser user){
		addUserTo(user, DEFAULT_USERS_FILE);
	}
	
	private static Element toXmlElement(ConstellioUser constellioUser){
        BaseElement user = new BaseElement(USER);
        user.addAttribute(FIRST_NAME, constellioUser.getFirstName());
        user.addAttribute(LAST_NAME, constellioUser.getLastName());
        user.addAttribute(LOGIN, constellioUser.getUsername());
        user.addAttribute(PASSWORD_HASH, constellioUser.getPasswordHash());

        if (constellioUser.getLocale() != null){
        	user.addAttribute(LOCALE, constellioUser.getLocaleCode());
        }

        Set<String> constellioRoles = constellioUser.getRoles();
        if (! constellioRoles.isEmpty()){
        	Element roles = user.addElement(ROLES);
        	for(String constellioRole : constellioRoles){
        		Element role = roles.addElement(ROLE);
        		role.addAttribute(VALUE, constellioRole);
        	}
        }
        
		return user;
	}
	
	@SuppressWarnings("unchecked")
	private static ConstellioUser toConstellioUser(Element element){
		ConstellioUser constellioUser = new ConstellioUser();
		
		constellioUser.setFirstName(element.attributeValue(FIRST_NAME));
		constellioUser.setLastName(element.attributeValue(LAST_NAME));
		constellioUser.setUsername(element.attributeValue(LOGIN));
		constellioUser.setPasswordHash(element.attributeValue(PASSWORD_HASH));
		
		Attribute locale = element.attribute(LOCALE);
		
		if(locale != null){
			constellioUser.setLocaleCode(locale.getValue());
		}else{
			constellioUser.setLocale(ConstellioSpringUtils.getDefaultLocale());
		}
		
		Iterator<Element> rolesIt = element.elementIterator(ROLES);
		if (rolesIt != null){
			Element roles = rolesIt.next();
			for (Iterator<Element> it = roles.elementIterator(ROLE); it.hasNext();) {
	            Element currentRole = it.next();
	            constellioUser.addRole(currentRole.attributeValue(VALUE));
			}
		}
		

		return constellioUser;
	}
	
	public static void main(String[] argv){
		createEmptyUsersFile();
		
    	ConstellioUser dataUser = new ConstellioUser("admin", "lol", ConstellioSpringUtils.getDefaultLocale());
        dataUser.setFirstName("System");
        dataUser.setLastName("Administrator");
        dataUser.getRoles().add(Roles.ADMIN);
        
        addUserTo(dataUser);
        
        List<ConstellioUser> users = readUsers();
        
        Assert.assertEquals(1, users.size());
        
        ConstellioUser user = users.get(0);
        
        Assert.assertTrue(user.checkPassword("lol"));
        
//        Assert.assertEquals(1, user.getRoles());
        
        Assert.assertTrue(user.isAdmin());
        System.out.println("Succes!" + DEFAULT_USERS_FILE);
		
	}
}
