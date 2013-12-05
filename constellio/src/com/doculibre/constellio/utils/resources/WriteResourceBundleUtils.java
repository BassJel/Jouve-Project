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
package com.doculibre.constellio.utils.resources;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.doculibre.constellio.utils.ClasspathUtils;

/**
 * Utility class that will generate empty ResourceBundle files for the default language.
 * 
 * @author Vincent Dussault
 */
public class WriteResourceBundleUtils {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        File binDir = ClasspathUtils.getClassesDir();
        File projectDir = binDir.getParentFile();
        File sourceDir = new File(projectDir, "source");

        String defaultLanguage;
        String otherLanguage;
        if (args.length > 0) {
            defaultLanguage = args[0];
            otherLanguage = args[1];
        } else {
            defaultLanguage = Locale.ENGLISH.getLanguage();
            otherLanguage = Locale.FRENCH.getLanguage();
        }

        List<File> propertiesFiles = (List<File>) FileUtils.listFiles(sourceDir,
            new String[] { "properties" }, true);
        for (File propertiesFile : propertiesFiles) {
            File propertiesDir = propertiesFile.getParentFile();

            String propertiesNameWoutSuffix = StringUtils.substringBefore(propertiesFile.getName(), "_");
            propertiesNameWoutSuffix = StringUtils.substringBefore(propertiesNameWoutSuffix, ".properties");

            String noLanguageFileName = propertiesNameWoutSuffix + ".properties";
            String defaultLanguageFileName = propertiesNameWoutSuffix + "_" + defaultLanguage + ".properties";
            String otherLanguageFileName = propertiesNameWoutSuffix + "_" + otherLanguage + ".properties";

            File noLanguageFile = new File(propertiesDir, noLanguageFileName);
            File defaultLanguageFile = new File(propertiesDir, defaultLanguageFileName);
            File otherLanguageFile = new File(propertiesDir, otherLanguageFileName);

            if (defaultLanguageFile.exists() && otherLanguageFile.exists() && !noLanguageFile.exists()) {
                System.out.println(defaultLanguageFile.getPath() + " > " + noLanguageFileName);
                System.out.println(defaultLanguageFile.getPath() + " > empty file");

                 defaultLanguageFile.renameTo(noLanguageFile);
                 FileWriter defaultLanguageEmptyFileWriter = new FileWriter(defaultLanguageFile);
                 defaultLanguageEmptyFileWriter.write("");
                 IOUtils.closeQuietly(defaultLanguageEmptyFileWriter);
            }
        }
    }
}
