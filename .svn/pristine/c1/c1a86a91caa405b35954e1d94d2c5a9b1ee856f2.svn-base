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

import com.doculibre.constellio.utils.aes.lslAESCrypto.LSLAESCrypto.LSLAESCryptoMode;
import com.doculibre.constellio.utils.aes.lslAESCrypto.LSLAESCrypto.LSLAESCryptoPad;
 
/** */
public class ExampleEncrypt {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final String myKey = "1234567890ABCDEF0123456789ABCDEF";
        final String myIV = "89ABCDEF0123456789ABCDEF01234567";
        final String myMsg = "Hello world! I am a lovely message waiting to be encrypted!";
 
        final LSLAESCrypto aes = new LSLAESCrypto(
            LSLAESCryptoMode.CFB,
            LSLAESCryptoPad.NONE,
            128,
            myKey,
            myIV);
        System.out.println(aes.encrypt(myMsg));
    }
}
