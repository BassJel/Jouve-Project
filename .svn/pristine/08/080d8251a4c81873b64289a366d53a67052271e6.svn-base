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
package com.doculibre.constellio.izpack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.util.crypt.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;

import com.izforge.izpack.util.AbstractUIProcessHandler;

public class UsersToXmlFile {
    public static final String DEFAULT_USERS_FILE = "initUsersList.xml";// ClasspathUtils.getWebinfDir() +
                                                                        // File.separator +

    private static final String FIRST_NAME = "firstName";
    private static final String USER = "user";
    private static final String ROLES = "roles";
    private static final String LAST_NAME = "lastName";
    private static final String LOGIN = "login";
    private static final String PASSWORD_HASH = "password";
    private static final String LOCALE = "locale";
    private static final String ROLE = "role";
    private static final String VALUE = "value";

    private class ConstellioUser {

        private String passwordHash;

        private String username;

        private String firstName;

        private String lastName;

        private Locale locale;

        private String localeCode;

        private Set<String> roles = new HashSet<String>(1);

        public ConstellioUser(String username, String password, Locale locale) {
            super();
            this.username = username;
            if (password == null) {
                password = "";
            }
            this.passwordHash = getHash(password);
            if (locale != null) {
                setLocale(locale);
            }

            roles.add(Roles.USER);
        }

        @Column(unique = true, nullable = false)
        public String getUsername() {
            return username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Transient
        public Locale getLocale() {
            if (locale == null && localeCode != null) {
                locale = parseLocale(localeCode);
            }
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
            this.setLocaleCode(locale.getLanguage());
        }

        @Column(length = 5, nullable = false)
        public String getLocaleCode() {
            return this.localeCode;
        }

        public void setLocaleCode(String localeCode) {
            this.localeCode = localeCode;
            this.locale = parseLocale(localeCode);
        }

        private Locale parseLocale(String localeCode) {
            Locale locale;
            StringTokenizer st = new StringTokenizer(localeCode, "_");
            String language = st.nextToken();
            if (st.hasMoreTokens()) {
                locale = new Locale(language, st.nextToken());
            } else {
                locale = new Locale(language);
            }
            return locale;
        }

        @Column(length = 28, nullable = false)
        public String getPasswordHash() {
            return passwordHash;
        }

        /**
         * Performs hash on given password and compares it to the correct hash.
         * 
         * @return true if hashed password is correct
         */

        public String getHash(String password) {
            MessageDigest md = getMessageDigest();
            md.update(getSalt());
            byte[] hash = md.digest(password.getBytes());
            // using a Base64 string for the hash because putting a
            // byte[] into a blob isn't working consistently.
            return new String(Base64.encodeBase64(hash));
        }

        private byte[] getSalt() {
            return "constellio".getBytes();
        }

        protected MessageDigest getMessageDigest() {
            try {
                return MessageDigest.getInstance("SHA");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA Hash algorithm not found.", e);
            }
        }

        @ElementCollection
        @CollectionTable(name = "ConstellioUser_Roles", joinColumns = @JoinColumn(name = "constellioUser_id"))
        @Column(name = "role")
        public Set<String> getRoles() {
            return roles;
        }

    }

    // empty file including its DTD

    public static final String[] emptyFileLines = { "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "<!DOCTYPE users [", "<!ELEMENT users (user*)>", "<!ELEMENT " + USER + " (" + ROLES + "?)>",
        "\t<!ATTLIST " + USER + " " + FIRST_NAME + " CDATA #REQUIRED>",
        "\t<!ATTLIST " + USER + " " + LAST_NAME + " CDATA #REQUIRED>",
        "\t<!ATTLIST " + USER + " " + LOGIN + " CDATA #REQUIRED>",
        "\t<!ATTLIST " + USER + " " + PASSWORD_HASH + " CDATA #REQUIRED>",
        "\t<!ATTLIST " + USER + " " + LOCALE + " CDATA #IMPLIED>",
        "<!ELEMENT " + ROLES + " (" + ROLE + "+)>", "<!ELEMENT " + ROLE + "  EMPTY>",
        "\t<!ATTLIST " + ROLE + " " + VALUE + " CDATA #REQUIRED>", "]>", "<users>", "</users>" };

    public static void run(AbstractUIProcessHandler handler, String[] args) {
        if (args.length != 3) {
            System.out.println("file login password");
            return;
        }

        String target = args[0];

        File xmlFile = new File(target);

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(xmlFile));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            for (String line : Arrays.asList(emptyFileLines)) {
                writer.write(line + System.getProperty("line.separator"));
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String login = args[1];

        String passwd = args[2];

        UsersToXmlFile elem = new UsersToXmlFile();

        ConstellioUser dataUser = elem.new ConstellioUser(login, passwd, null);
        dataUser.setFirstName("System");
        dataUser.setLastName("Administrator");
        dataUser.getRoles().add(Roles.ADMIN);

        Document xmlDocument;
        try {
            xmlDocument = new SAXReader().read(target);
            Element root = xmlDocument.getRootElement();

            BaseElement user = new BaseElement(USER);
            user.addAttribute(FIRST_NAME, dataUser.getFirstName());
            user.addAttribute(LAST_NAME, dataUser.getLastName());
            user.addAttribute(LOGIN, dataUser.getUsername());
            user.addAttribute(PASSWORD_HASH, dataUser.getPasswordHash());

            if (dataUser.getLocale() != null) {
                user.addAttribute(LOCALE, dataUser.getLocaleCode());
            }

            Set<String> constellioRoles = dataUser.getRoles();
            if (!constellioRoles.isEmpty()) {
                Element roles = user.addElement(ROLES);
                for (String constellioRole : constellioRoles) {
                    Element role = roles.addElement(ROLE);
                    role.addAttribute(VALUE, constellioRole);
                }
            }

            root.add(user);

            OutputFormat format = OutputFormat.createPrettyPrint();

            xmlFile = new File(target);
            // FIXME réecrire la DTD
            // xmlDocument.addDocType(arg0, arg1, arg2)
            XMLWriter writer2 = new XMLWriter(new FileOutputStream(xmlFile), format);

            writer2.write(xmlDocument);
            writer2.close();
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

}
