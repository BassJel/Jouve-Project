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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstellioNameUtils {

    //  Checks if the String contains only unicode letters, digits or space (' ').
    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z_A-Z][a-zA-Z0-9_-]*", Pattern.CASE_INSENSITIVE);
    
    public static boolean isValidName(String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);
//        if (!matcher.matches()) {
//            throw new IllegalArgumentException(
//                    "Name does not validate against regular expression : \"[a-z_][a-z0-9_-]*\" :" + name);
//        }
        return matcher.matches();
    }

}
