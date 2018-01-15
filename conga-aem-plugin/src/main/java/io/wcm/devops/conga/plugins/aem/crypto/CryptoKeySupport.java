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

import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * Supports generating and reading crypto keys.
 */
public interface CryptoKeySupport {

  /**
   * Generate new crypto key.
   * @return Crypto key
   * @throws GeneralSecurityException Security exception
   */
  Key generateKey() throws GeneralSecurityException;

  /**
   * Read crypto key from given byte array.
   * @param keyData Crypto key data.
   * @return Crypto key
   * @throws GeneralSecurityException Security exception
   */
  Key readKey(byte[] keyData) throws GeneralSecurityException;

}
