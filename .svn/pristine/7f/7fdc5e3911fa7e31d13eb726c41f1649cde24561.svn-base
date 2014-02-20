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
 * Cette classe sera utilisée par le ProcessPanel (izPack) pour changer le port du serveur tomcat:
 * le port sera remplacé par celui donnée en paramètre dans le fichier donné en entrée
 * 
 * @author bnouha1
 */
public class TomcatUtil {

    @SuppressWarnings("unchecked")
    public static void run(AbstractUIProcessHandler handler, String[] args) {
        if (args.length != 2) {
            System.out.println("serverPath port");
            return;
        }

        String serverPath = args[0];
        String port = args[1];
        if (port.equals("8080")) {
            // C'est celui par defaut => ne rien faire
            return;
        }

        Document xmlDocument;
        try {
            xmlDocument = new SAXReader().read(serverPath);
            Element root = xmlDocument.getRootElement();

            Iterator<Element> it = root.elementIterator("Service");
            if (!it.hasNext()) {
                System.out.println("Corrupt persistence file :" + serverPath);
                return;
            }
            Element connectors = it.next();
            for (it = connectors.elementIterator("Connector"); it.hasNext();) {
                Element connector = it.next();
                String id = connector.attributeValue("protocol");
                if (id.startsWith("HTTP")) {
                    Attribute att = connector.attribute("port");
                    att.setText(port);
                    break;
                }
            }

            OutputFormat format = OutputFormat.createPrettyPrint();

            File xmlFile = new File(serverPath);
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

    /*
     * public static void main(String[] args){
     * run(null, args);
     * }
     */

}
