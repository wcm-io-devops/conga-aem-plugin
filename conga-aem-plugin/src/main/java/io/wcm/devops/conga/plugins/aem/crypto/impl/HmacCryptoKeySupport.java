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

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import io.wcm.devops.conga.plugins.aem.crypto.CryptoKeySupport;

/**
 * HMAC crypto key support implementation with the same algorithms as used by AEM 6.3 and up.
 */
public class HmacCryptoKeySupport implements CryptoKeySupport {

  private static final String HMAC_KEY_ALGORITHM = "HmacSHA256";
  private static final int HMAC_KEY_SIZE = 256;

  @Override
  public Key generateKey() throws GeneralSecurityException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_KEY_ALGORITHM);
    keyGenerator.init(HMAC_KEY_SIZE);
    return keyGenerator.generateKey();
  }

  @Override
  public Key readKey(byte[] keyData) {
    return new SecretKeySpec(keyData, HMAC_KEY_ALGORITHM);
  }

}
