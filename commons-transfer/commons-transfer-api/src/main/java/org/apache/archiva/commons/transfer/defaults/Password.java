package org.apache.archiva.commons.transfer.defaults;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Perform basic password encryption / decryption.
 * 
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 */
public class Password {
    private static final String ENCODING = "UTF8";

    // DESede (Triple DES) Crypto Scheme
    private static final String SCHEME = "DESede";

    private KeySpec keySpec;

    private SecretKeyFactory keyFactory;

    private Cipher cipher;

    public Password() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] key = "GeoCaching: Cache In, Trash Out".getBytes(ENCODING);
        keySpec = new DESedeKeySpec(key);
        keyFactory = SecretKeyFactory.getInstance(SCHEME);
        cipher = Cipher.getInstance(SCHEME);
    }

    /**
     * Decrypt a Base64 encoded string into a password char array.
     * 
     * @param encryptedString the encrypted string to decrypt.
     * @return the decrypted password.
     */
    public char[] decrypt(String encryptedString) throws GeneralSecurityException, IOException {
        SecretKey key = keyFactory.generateSecret(keySpec);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] cleartext = Base64.decodeBase64(encryptedString.getBytes(ENCODING));
        byte[] ciphertext = cipher.doFinal(cleartext);

        return toCharArray(ciphertext);
    }

    /**
     * Encrypt the password and return a Base64 encrypted password string.
     * 
     * @param password the password to encrypt.
     * @return the Base64 encoded password.
     * @throws GeneralSecurityException if there was a problem encrypting the password.
     * @throws IOException 
     */
    public String encrypt(char password[]) throws GeneralSecurityException, IOException {
        SecretKey key = keyFactory.generateSecret(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cleartext = new String(password).getBytes(ENCODING);
        byte[] ciphertext = cipher.doFinal(cleartext);

        return new String(Base64.encodeBase64(ciphertext));
    }

    private char[] toCharArray(byte buf[]) {
        char ret[] = new char[buf.length];
        for (int i = 0; i < buf.length; i++) {
            ret[i] = (char) buf[i];
        }

        return ret;
    }
}
