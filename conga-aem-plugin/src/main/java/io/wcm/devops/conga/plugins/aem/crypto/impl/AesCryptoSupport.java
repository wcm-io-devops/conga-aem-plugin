/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.plugins.aem.crypto.impl;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoSupport;

/**
 * AES crypto support implementation with the same algorithms as used by AEM 6.3 and up.
 */
@SuppressWarnings("java:S5542") // cannot use a more secure cipher mode, we have to be compatible with AEM
public class AesCryptoSupport extends CryptoSupport {

  private static final String AES_KEY_ALGORITHM = "AES";
  private static final int AES_KEY_SIZE = 128;

  private static final String AES_CYPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final int IV_SIZE = 16;

  private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

  private static final Random RANDOM = initRandom();

  private static Random initRandom() {
    try {
      return SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
    }
    catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public Key generateKey() throws GeneralSecurityException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_KEY_ALGORITHM);
    keyGenerator.init(AES_KEY_SIZE);
    return keyGenerator.generateKey();
  }

  @Override
  public Key readKey(byte[] keyData) {
    return new SecretKeySpec(keyData, AES_KEY_ALGORITHM);
  }

  @Override
  public byte[] encrypt(byte[] data, Key key) throws GeneralSecurityException {
    byte[] iv = generateIV();
    byte[] byteEncyrpted = getCipher(Cipher.ENCRYPT_MODE, iv, key).doFinal(data);
    ByteBuffer buffer = ByteBuffer.allocate(iv.length + byteEncyrpted.length);
    buffer.put(iv).put(byteEncyrpted);
    return buffer.array();
  }

  @Override
  public byte[] decrypt(byte[] data, Key key) throws GeneralSecurityException {
    byte[] iv = new byte[IV_SIZE];
    byte[] byteEncrypted = new byte[data.length - IV_SIZE];
    ByteBuffer buffer = ByteBuffer.wrap(data);
    buffer.get(iv).get(byteEncrypted);
    return getCipher(Cipher.DECRYPT_MODE, iv, key).doFinal(byteEncrypted);
  }

  private Cipher getCipher(int cipherMode, byte[] iv, Key key) throws GeneralSecurityException {
    IvParameterSpec spec = new IvParameterSpec(iv);
    Cipher cipher = Cipher.getInstance(AES_CYPHER_ALGORITHM);
    cipher.init(cipherMode, key, spec);
    return cipher;
  }

  @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
  private byte[] generateIV() {
    byte[] iv = new byte[IV_SIZE];
    RANDOM.nextBytes(iv);
    return iv;
  }

}
