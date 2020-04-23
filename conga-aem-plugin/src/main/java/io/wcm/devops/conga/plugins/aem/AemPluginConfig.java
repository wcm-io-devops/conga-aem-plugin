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
package io.wcm.devops.conga.plugins.aem;

/**
 * AEM plugin parameters for generic CONGA plugin configuration.
 */
public final class AemPluginConfig {

  /**
   * Plugin name for generic plugin configuration.
   */
  public static final String PLUGIN_NAME = "aem-plugin";

  /**
   * AES AEM Crypto key URL (master key). Url prefixes are supported.
   */
  public static final String PARAMETER_CRYPTO_AES_KEY_URL = "cryptoAesKeyUrl";

  /**
   * Skip AES AEM crypto support, output strings that should be encrypted without encryption.
   */
  public static final String PARAMETER_CRYPTO_SKIP = "cryptoSkip";

  private AemPluginConfig() {
    // constants only
  }

}
