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
package com.doculibre.constellio.lang;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.doculibre.constellio.utils.ConstellioStringUtils;

public class LangDetectorUtil {
	
	private static final Logger LOGGER = Logger.getLogger(LangDetectorUtil.class);
	
	static {
		String packageName = StringUtils.substringBeforeLast(LangDetectorUtil.class.getCanonicalName(), ".");
		String profilesPackage = packageName + (packageName == null ? "" : ".") + "profiles";
		String profilesDirPath = "/" + StringUtils.replace(profilesPackage.toString(), ".", "/");
		URL profilesDirURL = LangDetectorUtil.class.getResource(profilesDirPath);
		File profilesDirectory;
		try {
			profilesDirectory = new File(profilesDirURL.toURI());
			DetectorFactory.loadProfile(profilesDirectory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private final static LangDetectorUtil INSTANCE = new LangDetectorUtil();

	private LangDetectorUtil() { }

	public static LangDetectorUtil getInstance() {
		return INSTANCE;
	}

	public String detect(String text) {
		String lang;
		if (!ConstellioStringUtils.isEmpty(text)) {
			try {
				Detector detector = DetectorFactory.create();
				detector.append(text);
				lang = detector.detect();
			} catch (Throwable t) {
				LOGGER.warn("Problem while trying to detect lang for " + text, t);
				lang = null;
			}
		} else {
			lang = null;
		} 
		return lang;
	}

	/**
	 * <p>Checks if the string contains only ASCII printable characters.</p>
	 * 
	 * <p><code>null</code> will return <code>false</code>.
	 * An empty String ("") will return <code>true</code>.</p>
	 * 
	 * <pre>
	 * StringUtils.isAsciiPrintable(null)     = false
	 * StringUtils.isAsciiPrintable("")       = true
	 * StringUtils.isAsciiPrintable(" ")      = true
	 * StringUtils.isAsciiPrintable("Ceki")   = true
	 * StringUtils.isAsciiPrintable("ab2c")   = true
	 * StringUtils.isAsciiPrintable("!ab-c~") = true
	 * StringUtils.isAsciiPrintable("\u0020") = true
	 * StringUtils.isAsciiPrintable("\u0021") = true
	 * StringUtils.isAsciiPrintable("\u007e") = true
	 * StringUtils.isAsciiPrintable("\u007f") = false
	 * StringUtils.isAsciiPrintable("Ceki G\u00fclc\u00fc") = false
	 * </pre>
	 *
	 * @param str the string to check, may be null
	 * @return <code>true</code> if every character is in the range
	 *  32 thru 126
	 * @since 2.1
	 */
	public static boolean isAsciiPrintable(String str) {
	    if (str == null) {
	        return false;
	    }
	    int sz = str.length();
	    for (int i = 0; i < sz; i++) {
	        if (isAsciiPrintable(str.charAt(i)) == false) {
	            return false;
	        }
	    }
	    return true;
	}
	  
	/**
	 * <p>Checks whether the character is ASCII 7 bit printable.</p>
	 *
	 * <pre>
	 *   CharUtils.isAsciiPrintable('a')  = true
	 *   CharUtils.isAsciiPrintable('A')  = true
	 *   CharUtils.isAsciiPrintable('3')  = true
	 *   CharUtils.isAsciiPrintable('-')  = true
	 *   CharUtils.isAsciiPrintable('\n') = false
	 *   CharUtils.isAsciiPrintable('&copy;') = false
	 * </pre>
	 * 
	 * @param ch  the character to check
	 * @return true if between 32 and 126 inclusive
	 */
	public static boolean isAsciiPrintable(char ch) {
	    return ch >= 32 && ch < 127;
	}

	public static void main(String[] args) {
		LangDetectorUtil autodetectLang = LangDetectorUtil.getInstance();
		long start = System.currentTimeMillis();
		System.out.println(autodetectLang.detect("Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. Etant donné la structure hiérarchique des packages et le nombre considérables de packages créés par des développeurs du monde entier, il est essentiel d'éviter de donner le même nom à des packages différents. Ainsi Java propose une dénomination standard des packages. Cette appellation standard consiste à donner un nom au package et de l'allonger par le nom de la société, ou du concepteur des classes qu'il contient. Ainsi, un package soundstuffs développé par CCM aurait pour dénomination net.commentcamarche.soundstuffs. "));
		System.out.println(autodetectLang.detect("The Canadian journal of economics and political science"));
		System.out.println(autodetectLang.detect("un texto un poco mas longo en espa�ol"));
		System.out.println(autodetectLang.detect("les longs camions"));
		System.out.println(autodetectLang.detect("les camions"));
		System.out.println(autodetectLang.detect("document"));
		System.out.println(autodetectLang.detect("nouveau logo doculibre"));
		System.out
				.println(autodetectLang
						.detect("Merci nicolas pour tes suggestions: Voici quelques idées que j'ai eu Open4search (o4s) Seachforge Searchforce Envoyé de mon iPhone"));
		System.out.println(autodetectLang.detect("Rencontre Patin lundi"));
		System.out.println(System.currentTimeMillis() - start);
	}
	
}
