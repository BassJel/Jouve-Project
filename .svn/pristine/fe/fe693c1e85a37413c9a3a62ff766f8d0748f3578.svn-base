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
package com.doculibre.constellio.utils.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.doculibre.constellio.utils.ClasspathUtils;

public class ApplyLicenseUtils {

    @SuppressWarnings("unchecked")
    private static List<String> readLines(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        List<String> lines = IOUtils.readLines(is);
        IOUtils.closeQuietly(is);
        return lines;
    }
    
    private static boolean isValidPackage(File javaFile) {
        String javaFileDotPath = javaFile.getPath().replace(File.separator, ".");
        return javaFileDotPath.indexOf("com.doculibre.constellio") != -1;
    }

    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        URL licenceHeaderURL = ApplyLicenseUtils.class.getResource("LICENSE_HEADER");
        File binDir = ClasspathUtils.getClassesDir();
        File projectDir = binDir.getParentFile();
//        File dryrunDir = new File(projectDir, "dryrun");
        File licenceFile = new File(licenceHeaderURL.toURI());
        List<String> licenceLines = readLines(licenceFile);
//        for (int i = 0; i < licenceLines.size(); i++) {
//            String licenceLine = licenceLines.get(i);
//            licenceLines.set(i, " * " + licenceLine);
//        }
//        licenceLines.add(0, "/**");
//        licenceLines.add(" */");

        List<File> javaFiles = (List<File>) org.apache.commons.io.FileUtils.listFiles(projectDir,
            new String[] { "java" }, true);
        for (File javaFile : javaFiles) {
            if (isValidPackage(javaFile)) {
                List<String> javaFileLines = readLines(javaFile);
                if (!javaFileLines.isEmpty()) {
                    boolean modified = false;
                    String firstLineTrim = javaFileLines.get(0).trim();
                    if (firstLineTrim.startsWith("package")) {
                        modified = true;
                        javaFileLines.addAll(0, licenceLines);
                    } else if (firstLineTrim.startsWith("/**")) {
                        int indexOfEndCommentLine = -1;
                        loop2: for (int i = 0; i < javaFileLines.size(); i++) {
                            String javaFileLine = javaFileLines.get(i);
                            if (javaFileLine.indexOf("*/") != -1) {
                                indexOfEndCommentLine = i;
                                break loop2;
                            }
                        }
                        if (indexOfEndCommentLine != -1) {
                            modified = true;
                            int i = 0;
                            loop3 : for (Iterator<String> it = javaFileLines.iterator(); it.hasNext();) {
                                it.next();
                                if (i <= indexOfEndCommentLine) {
                                    it.remove();
                                } else {
                                    break loop3;
                                }
                                i++;
                            }
                            javaFileLines.addAll(0, licenceLines);
                        } else {
                            throw new RuntimeException("Missing end comment for file "
                                + javaFile.getAbsolutePath());
                        }
                    }
                    
                    if (modified) {
//                        String outputFilePath = javaFile.getPath().substring(projectDir.getPath().length());
//                        File outputFile = new File(dryrunDir, outputFilePath);
//                        outputFile.getParentFile().mkdirs();
//                        System.out.println(outputFile.getPath());
//                        FileOutputStream fos = new FileOutputStream(outputFile);
                        System.out.println(javaFile.getPath());
                        FileOutputStream fos = new FileOutputStream(javaFile);
                        IOUtils.writeLines(javaFileLines, "\n", fos);
                        IOUtils.closeQuietly(fos);
                    }
                }
            }
        }
    }

}
