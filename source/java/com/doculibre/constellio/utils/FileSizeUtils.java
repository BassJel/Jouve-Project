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
package com.doculibre.constellio.utils;

import java.io.File;
import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;

/**
 * Adapted from http://www.java2s.com/Tutorial/Java/0180__File/FormatSize.htm
 */
public class FileSizeUtils {
    
    public static String formatSize(File file, int decimalPos) {
        long fileSize;
        if (file.isDirectory()) {
            fileSize = FileUtils.sizeOfDirectory(file);
        } else {
            fileSize = file.length();
        }
        return formatSize(fileSize, decimalPos);
    }
    
    public static String formatSize(long fileSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
           fmt.setMaximumFractionDigits(decimalPos);
        }
        final double size = fileSize;
        double val = size / (1024 * 1024);
        if (val > 1) {
           return fmt.format(val).concat(" MB");
        }
        val = size / 1024;
        if (val > 10) {
           return fmt.format(val).concat(" KB");
        }
        return fmt.format(val).concat(" bytes");
    }

}
