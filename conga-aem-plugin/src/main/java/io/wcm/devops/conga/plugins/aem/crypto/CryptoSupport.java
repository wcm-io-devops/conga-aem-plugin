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
package io.wcm.devops.conga.plugins.aem.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * Support for symmetric encryption and decryption of data.
 */
public abstract class CryptoSupport implements CryptoKeySupport {

  /**
   * Encrypt binary data with the given key.
   * @param data Data to be encrypted
   * @param key Crypto key
   * @return Encrypted data
   * @throws GeneralSecurityException Security exception
   */
  public abstract byte[] encrypt(byte[] data, Key key) throws GeneralSecurityException;

  /**
   * Decrypt binary data with the given eky.
   * @param data Data to be decrypted
   * @param key Crypto key
   * @return Decrypted data
   * @throws GeneralSecurityException Security exception
   */
  public abstract byte[] decrypt(byte[] data, Key key) throws GeneralSecurityException;

  /**
   * Encrypt plain text with the given key.
   * @param plainText Text to be encrypted
   * @param key Crypto key
   * @return Encrypted data as crypto string
   * @throws GeneralSecurityException Security exception
   */
  public final String encrypt(String plainText, Key key) throws GeneralSecurityException {
    byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedData = encrypt(data, key);
    return CryptoString.toString(encryptedData);
  }

  /**
   * Decrypt binary data with the given eky.
   * @param cryptoString Encrypted data as crypto string
   * @param key Crypto key
   * @return Decrypted text
   * @throws GeneralSecurityException Security exception
   */
  public final String decrypt(String cryptoString, Key key) throws GeneralSecurityException {
    byte[] encryptedData = CryptoString.toByteArray(cryptoString);
    byte[] data = decrypt(encryptedData, key);
    return new String(data, StandardCharsets.UTF_8);
  }

}
