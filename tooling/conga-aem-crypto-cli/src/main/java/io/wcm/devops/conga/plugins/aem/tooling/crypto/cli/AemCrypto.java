/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.devops.conga.plugins.aem.tooling.crypto.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import io.wcm.devops.conga.plugins.aem.crypto.CryptoSupport;
import io.wcm.devops.conga.plugins.aem.crypto.impl.AesCryptoSupport;

/**
 * Encrypts and decrypts values using AEM crypto support.
 */
public final class AemCrypto {

  private AemCrypto() {
    // static methods only
  }

  /**
   * Encrypts a string value using AEM crypto support.
   * @param value Value to encrypt
   * @param cryptoAesKey Path to 'master' key file
   * @return Encrypted value
   * @throws IOException I/O Exception
   * @throws GeneralSecurityException Security exception
   */
  public static String encryptString(String value, String cryptoAesKey)
      throws IOException, GeneralSecurityException {
    CryptoSupport crypto = new AesCryptoSupport();
    Key key = getCryptoKey(cryptoAesKey, crypto);
    return crypto.encrypt(value, key);
  }

  /**
   * Decrypts a string value using AEM crypto support.
   * @param value Value to decrypt
   * @param cryptoAesKey Path to 'master' key file
   * @return Decrypted value
   * @throws IOException I/O Exception
   * @throws GeneralSecurityException Security exception
   */
  public static String decryptString(String value, String cryptoAesKey)
      throws IOException, GeneralSecurityException {
    CryptoSupport crypto = new AesCryptoSupport();
    Key key = getCryptoKey(cryptoAesKey, crypto);
    return crypto.decrypt(value, key);
  }

  private static Key getCryptoKey(String cryptoAesKey, CryptoSupport crypto)
      throws IOException, GeneralSecurityException {
    if (StringUtils.isBlank(cryptoAesKey)) {
      throw new IllegalArgumentException("Please specify AEM crypto 'master' key via parameter '" + AemCryptoCli.CRYPTO_AES_KEY + "'.");
    }
    File file = new File(cryptoAesKey);
    if (!(file.exists() && file.isFile())) {
      throw new IllegalArgumentException("Unable to find AEM crypto 'master' key: " + file.getAbsolutePath());
    }
    try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
      byte[] data = IOUtils.toByteArray(is);
      return crypto.readKey(data);
    }
  }

}
