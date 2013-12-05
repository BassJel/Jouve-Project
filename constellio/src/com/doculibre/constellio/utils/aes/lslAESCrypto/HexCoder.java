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
package com.doculibre.constellio.utils.aes.lslAESCrypto;
/**
 * The following is a simple set of static methods for converting from hex to
 * bytes and vice-versa
 * 
 * @author Haravikk Mistral
 * @date Sep 15, 2008, 3:26:42 PM
 * @version 1.0
 */
public class HexCoder {
    /**
     * Quick converts bytes to hex-characters
     * 
     * @param bytes
     *            the byte-array to convert
     * @return the hex-representation
     */
    public static String bytesToHex(final byte[] bytes) {
        final StringBuffer s = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
            s.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            s.append(Character.forDigit(bytes[i] & 0xF, 16));
        }
        return s.toString();
    }
 
    /**
     * Quickly converts hex-characters to bytes
     * 
     * @param s
     *            the hex-string
     * @return the bytes represented
     */
    public static byte[] hexToBytes(final String s) {
        final byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; ++i)
            bytes[i] = (byte) Integer.parseInt(
                s.substring(2 * i, (2 * i) + 2),
                16);
        return bytes;
    }
}
