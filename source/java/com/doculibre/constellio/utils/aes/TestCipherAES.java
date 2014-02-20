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
package com.doculibre.constellio.utils.aes;

import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Demonstrate use of CipherOutputStream and CipherInputStream to encipher and decipher a message.
 * <p/>
 * This particular version uses AES/CBC/PKCS5Padding
 * but it fairly easy to convert it to use other algorithms.
 * Requires a shared secret key.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.0 2008-06-17
 * @since 2008-06-17
 */
public class TestCipherAES
    {
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * configure with encryption algorithm to use. Avoid insecure DES. Changes to algorithm may require additional ivParms.
     */
    private static final String ALGORITHM = "AES";

    /**
     * configure with block mode to use. Avoid insecure ECB.
     */
    private static final String BLOCK_MODE = "CBC";

    /**
     * configure with padding method to use
     */
    private static final String PADDING = "PKCS5Padding";

    /**
     * the encoding to use when converting bytes <--> String
     */
    private static final Charset CHARSET = Charset.forName( "UTF-8" );

    /**
     * 128 bits worth of some random, not particularly secret, but stable bytes to salt AES-CBC with
     */
    private static final IvParameterSpec CBC_SALT = new IvParameterSpec(
            new byte[] { 7, 34, 56, 78, 90, 87, 65, 43,
                    12, 34, 56, 78, -123, 87, 65, 43 } );

    // -------------------------- STATIC METHODS --------------------------

    /**
     * generate a random AES style Key
     *
     * @return the AES key generated.
     * @throws java.security.NoSuchAlgorithmException
     *          if AES is not supported.
     */
    private static SecretKeySpec generateKey()
            throws NoSuchAlgorithmException
        {
        final KeyGenerator kg = KeyGenerator.getInstance( ALGORITHM );
        kg.init( 128 );// specify key size in bits
        final SecretKey secretKey = kg.generateKey();
        final byte[] keyAsBytes = secretKey.getEncoded();
        return new SecretKeySpec( keyAsBytes, ALGORITHM );
        }

    /**
     * read an enciphered file and retrieve its plaintext message.
     *
     * @param cipher method used to encrypt the file
     * @param key    secret key used to encrypt the file
     * @param file   file where the message was written.
     *
     * @return the reconstituted decrypted message.
     * @throws InvalidKeyException if something wrong with the key.
     * @throws IOException         if problems reading the file.
     */
    @SuppressWarnings( { "JavaDoc" } )
    private static String readCiphered( Cipher cipher, SecretKeySpec key, File file )
            throws InvalidKeyException, IOException, InvalidAlgorithmParameterException
        {
        cipher.init( Cipher.DECRYPT_MODE, key, CBC_SALT );

        final CipherInputStream cin = new CipherInputStream( new FileInputStream( file ), cipher );

        // read big endian short length, msb then lsb
        final int messageLengthInBytes = ( cin.read() << 8 ) | cin.read();
        out.println( file.length() + " enciphered bytes in file" );
        out.println( messageLengthInBytes + " reconstituted bytes" );

        final byte[] reconstitutedBytes = new byte[messageLengthInBytes];

        // we can't trust CipherInputStream to give us all the data in one shot
        int bytesReadSoFar = 0;

        int bytesRemaining = messageLengthInBytes;
        while ( bytesRemaining > 0 )
            {
            final int bytesThisChunk = cin.read( reconstitutedBytes, bytesReadSoFar, bytesRemaining );
            if ( bytesThisChunk == 0 )
                {
                throw new IOException( file.toString() + " corrupted." );
                }
            bytesReadSoFar += bytesThisChunk;
            bytesRemaining -= bytesThisChunk;
            }
        cin.close();
        return new String( reconstitutedBytes, CHARSET );
        }

    /**
     * write a plaintext message to a file enciphered.
     *
     * @param cipher    the method to use to encrypt the file.
     * @param key       the secret key to use to encrypt the file.
     * @param file      the file to write the encrypted message to.
     * @param plainText the plaintext of the message to write.
     *
     * @throws InvalidKeyException if something is wrong with they key
     * @throws IOException         if there are problems writing the file.
     * @throws InvalidAlgorithmParameterException
     *                             if problems with CBC_SALT.
     */
    private static void writeCiphered( Cipher cipher, SecretKeySpec key, File file, String plainText )
            throws InvalidKeyException, IOException, InvalidAlgorithmParameterException
        {
        cipher.init( Cipher.ENCRYPT_MODE, key, CBC_SALT );
        final CipherOutputStream cout = new CipherOutputStream( new FileOutputStream( file ), cipher );
        final byte[] plainTextBytes = plainText.getBytes( CHARSET );
        out.println( plainTextBytes.length + " plaintext bytes written" );
        // prepend with big-endian short message length, will be encrypted too.
        cout.write( plainTextBytes.length >>> 8 );// msb
        cout.write( plainTextBytes.length & 0xff );// lsb
        cout.write( plainTextBytes );
        cout.close();
        }

    // --------------------------- main() method ---------------------------

    /**
     * Demonstrate use of CipherOutputStream and CipherInputStream to encipher and decipher a message.
     *
     * @param args not used
     *
     * @throws NoSuchAlgorithmException if AES is not supported
     * @throws NoSuchPaddingException   if PKCS5 padding is not supported.
     * @throws InvalidKeyException      if there is something wrong with the key.
     * @throws IOException              if there are problems reading or writing the file.
     * @throws java.security.InvalidAlgorithmParameterException
     *                                  if problems with CBC_SALT.
     */
    public static void main( String[] args ) throws InvalidAlgorithmParameterException,
            InvalidKeyException,
            IOException,
            NoSuchAlgorithmException,
            NoSuchPaddingException
        {
        // The secret message we want to send to our secret agent in London.
        final String plainText = "Q.E. to throw cream pies at Cheney and Bush tomorrow at 19:05.";

        // use a random process to generate a enciphering key
        SecretKeySpec key = generateKey();

        final Cipher cipher = Cipher.getInstance( ALGORITHM + "/" + BLOCK_MODE + "/" + PADDING );

        // write out the ciphered message
        writeCiphered( cipher, key, new File( "transport.bin" ), plainText );

        // now try reading message back in deciphering it.
        final String reconstitutedText = readCiphered( cipher, key, new File( "transport.bin" ) );

        out.println( "original: " + plainText );
        out.println( "reconstituted: " + reconstitutedText );

        // output is:
        // 62 plaintext bytes written
        // 80 enciphered bytes in file
        // 62 reconstituted bytes
        // original: Q.E. to throw cream pies at Cheney and Bush tomorrow at 19:05.
        // reconstituted: Q.E. to throw cream pies at Cheney and Bush tomorrow at 19:05.
        }
    }
