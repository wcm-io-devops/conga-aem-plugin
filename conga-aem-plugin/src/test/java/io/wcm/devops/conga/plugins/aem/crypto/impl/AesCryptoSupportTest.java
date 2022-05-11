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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Key;

import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.plugins.aem.crypto.CryptoKeySupport;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoString;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoSupport;

class AesCryptoSupportTest {

  @Test
  void testKeySupport() throws Exception {
    CryptoKeySupport keysupport = new AesCryptoSupport();

    Key generatedKey = keysupport.generateKey();
    assertNotNull(generatedKey);

    byte[] keyData = generatedKey.getEncoded();
    assertNotNull(keyData);

    Key readKey = keysupport.readKey(keyData);
    assertNotNull(readKey);

    assertEquals(generatedKey, readKey);
  }

  @Test
  void testEncryptDecrypt() throws Exception {
    CryptoSupport crypto = new AesCryptoSupport();
    Key key = crypto.generateKey();

    String text = "My sample string";
    String encryptedText = crypto.encrypt(text, key);

    assertTrue(CryptoString.isCryptoString(encryptedText));

    String decryptedText = crypto.decrypt(encryptedText, key);

    assertEquals(text, decryptedText);
  }

}
