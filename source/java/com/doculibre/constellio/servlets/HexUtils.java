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
package com.doculibre.constellio.servlets;

public class HexUtils {
	  private static final char[] kDigits = {
	    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    'a', 'b', 'c', 'd', 'e', 'f'
	  };
	  
	  public static char[] bytesToHex(byte[] raw) {
	    int length = raw.length;
	    char[] hex = new char[length * 2];
	    for (int i = 0; i < length; i++) {
	      int value = (raw[i] + 256) % 256;
	      int highIndex = value >> 4;
	      int lowIndex = value & 0x0f;
	      hex[i * 2 + 0] = kDigits[highIndex];
	      hex[i * 2 + 1] = kDigits[lowIndex];
	    }
	    return hex;
	  }
	  
	  public static byte[] hexToBytes(char[] hex) {
	    int length = hex.length / 2;
	    byte[] raw = new byte[length];
	    for (int i = 0; i < length; i++) {
	      int high = Character.digit(hex[i * 2], 16);
	      int low = Character.digit(hex[i * 2 + 1], 16);
	      int value = (high << 4) | low;
	      if (value > 127) value -= 256;
	      raw[i] = (byte)value;
	    }
	    return raw;
	  }
	  
	  public static byte[] hexToBytes(String hex) {
	    return hexToBytes(hex.toCharArray());
	  }
	}
