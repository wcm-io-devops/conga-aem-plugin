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

import java.security.Key;

import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.plugins.aem.crypto.CryptoKeySupport;

class HmacCryptoKeySupportTest {

  @Test
  void testKeySupport() throws Exception {
    CryptoKeySupport keysupport = new HmacCryptoKeySupport();

    Key generatedKey = keysupport.generateKey();
    assertNotNull(generatedKey);

    byte[] keyData = generatedKey.getEncoded();
    assertNotNull(keyData);

    Key readKey = keysupport.readKey(keyData);
    assertNotNull(readKey);

    assertEquals(generatedKey, readKey);
  }

}
