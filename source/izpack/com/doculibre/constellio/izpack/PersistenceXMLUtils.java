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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 * Cette classe sera utilisée par le ProcessPanel (izPack) pour spécifier:
 * l'adresse du serveur de la BD ainsi que le login et le passeword associés
 * Elle remplace tout simplement ces paramètres par ceux qui se trouve dans le fichier de configuration donné
 * en paramètre et le copie au fichier
 * de persistence utilisé par défaut
 * 
 * @author bnouha1
 */
public class PersistenceXMLUtils {
    
    public static final String SERVER_ELEMENT_ID = "hibernate.connection.url";
    public static final String LOGIN_ELEMENT_ID = "hibernate.connection.username";
    public static final String PASSWORD_ELEMENT_ID = "hibernate.connection.password";
    public static final String BEFORE_SERVER_NAME = "jdbc:mysql://";
    public static final String AFTER_SERVER_NAME = "/constellio?autoReconnect=true";

    @SuppressWarnings("unchecked")
    public static void run(AbstractUIProcessHandler handler, String[] args) {
        if (args.length != 5) {
            System.out.println("persistence_mysqlPath defaultPersistencePath server login password");
            return;
        }

        String persistence_mysqlPath = args[0];
        String defaultPersistencePath = args[1];
        String server = args[2];
        String login = args[3];
        String password = args[4];

        Document xmlDocument;
        try {
            xmlDocument = new SAXReader().read(persistence_mysqlPath);
            Element root = xmlDocument.getRootElement();
            Iterator<Element> it = root.elementIterator("persistence-unit");
            if (!it.hasNext()) {
                System.out.println("Corrupt persistence file :" + persistence_mysqlPath);
                return;
            }
            it = it.next().elementIterator("properties");
            if (!it.hasNext()) {
                System.out.println("Corrupt persistence file :" + persistence_mysqlPath);
                return;
            }
            Element properties = it.next();
            for (it = properties.elementIterator("property"); it.hasNext();) {
                Element property = it.next();
                String id = property.attributeValue("name");
                if (id.equals(SERVER_ELEMENT_ID)) {
                    Attribute att = property.attribute("value");
                    att.setText(BEFORE_SERVER_NAME + server + AFTER_SERVER_NAME);
                } else {
                    if (id.equals(LOGIN_ELEMENT_ID)) {
                        Attribute att = property.attribute("value");
                        att.setText(login);
                    } else {
                        if (id.equals(PASSWORD_ELEMENT_ID)) {
                            Attribute att = property.attribute("value");
                            att.setText(password);
                        }
                    }
                }

            }

            OutputFormat format = OutputFormat.createPrettyPrint();

            File xmlFile = new File(persistence_mysqlPath);
            XMLWriter writer2 = new XMLWriter(new FileOutputStream(xmlFile), format);

            writer2.write(xmlDocument);
            writer2.close();
            // copier au fichier de persistence par défaut:
            xmlFile = new File(defaultPersistencePath);
            writer2 = new XMLWriter(new FileOutputStream(xmlFile), format);

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

    /*
     * public static void main(String[] args){
     * run(null, args);
     * }
     */

}
