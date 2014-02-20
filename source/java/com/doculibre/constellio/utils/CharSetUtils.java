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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.ibm.icu.charset.CharsetICU;
import com.ibm.icu.text.CharsetDetector;

public class CharSetUtils {

	// charSet attribute.
	public static final String UTF_8 = "UTF-8";

	public static final String ISO_8859_1 = "ISO-8859-1";

	public static String convert(String text, String fromCharSet, String toCharSet) {
		return convert(text, fromCharSet, toCharSet, false);
	}
	
	private static String convert(String text, String fromCharSet, String toCharSet, boolean tryingAfterCharacterCodingException) {
//		System.out.println(Charset.availableCharsets());
		Charset asciiCharset = CharsetICU.forName(fromCharSet);
		CharsetDecoder decoder = asciiCharset.newDecoder();
		ByteBuffer asciiBytes = ByteBuffer.wrap(text.getBytes());

		CharBuffer helpChars = null;
		try {
			helpChars = decoder.decode(asciiBytes);
		} catch (CharacterCodingException e) {
			if (!tryingAfterCharacterCodingException) {
				String textFrom;
				try {
					textFrom = new String(text.getBytes(fromCharSet));
				} catch (UnsupportedEncodingException e2) {
					throw new RuntimeException(e);
				}
				String tryAgain = convert(textFrom, fromCharSet, toCharSet, true);
				tryAgain = convert(tryAgain, fromCharSet, toCharSet, true);
				return tryAgain;
			} else {
				return text;
			}
		}
		
		Charset utfCharset = Charset.forName(toCharSet);
		CharsetEncoder encoder = utfCharset.newEncoder();
		ByteBuffer utfBytes = null;
		try {
			utfBytes = encoder.encode(helpChars);
		} catch (CharacterCodingException e) {
			return text;
		}
		return new String(utfBytes.array(), utfBytes.arrayOffset(), utfBytes.limit());
	} 
	
	public static String getEncoding(String text) {
		InputStream bis = new ByteArrayInputStream(text.getBytes());
		CharsetDetector detector = new CharsetDetector();
		try {
			detector.setText(bis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String encoding = detector.detect().getName();
		return encoding;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String text = "québec";
		String converted = convert(text, UTF_8, ISO_8859_1);
		System.out.println(converted);

		System.out.println("----------------");

		text = "qu�bec";
		converted = convert(text, ISO_8859_1, UTF_8);
		System.out.println(converted);
	}

    /**
     * Silent url encoding
     * 
     * @param text
     * @param charSet
     * @return
     */
    public static String urlEncode(String text, String charSet) {
        try {
            return URLEncoder.encode(text, charSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String urlDecode(String text, String charSet) {
        try {
            return URLDecoder.decode(text, charSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
