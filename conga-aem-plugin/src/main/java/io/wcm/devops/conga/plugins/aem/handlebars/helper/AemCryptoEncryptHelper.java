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
package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import static io.wcm.devops.conga.plugins.aem.AemPluginConfig.PARAMETER_CRYPTO_AES_KEY_URL;
import static io.wcm.devops.conga.plugins.aem.AemPluginConfig.PARAMETER_CRYPTO_SKIP;
import static io.wcm.devops.conga.plugins.aem.AemPluginConfig.PLUGIN_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoString;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoSupport;
import io.wcm.devops.conga.plugins.aem.crypto.impl.AesCryptoSupport;

/**
 * Handlebars helper that encrypts a given string with AEM crypto key.
 */
public final class AemCryptoEncryptHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemCryptoEncrypt";

  /**
   * If set to true, missing crypto key is ignored and the input string is inserted unencrypted.
   */
  public static final String HASH_IGNORE_MISSING_KEY = "ignoreMissingKey";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  @SuppressWarnings({ "java:S3776", "java:S5411", "PMD.ExceptionAsFlowControl" }) // ignore complexity
  public Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    if (context == null) {
      return null;
    }

    // read crypto config parameter
    String cryptoAesKeyUrl = null;
    boolean cryptoSkip = false;
    Map<String, Object> aemPluginConfig = pluginContext.getGenericPluginConfig().get(PLUGIN_NAME);
    if (aemPluginConfig != null) {
      cryptoAesKeyUrl = Objects.toString(aemPluginConfig.get(PARAMETER_CRYPTO_AES_KEY_URL), null);
      Object cryptoSkipObject = aemPluginConfig.get(PARAMETER_CRYPTO_SKIP);
      if (cryptoSkipObject != null) {
        if (cryptoSkipObject instanceof Boolean) {
          cryptoSkip = (Boolean)cryptoSkipObject;
        }
        else {
          cryptoSkip = BooleanUtils.toBoolean(cryptoSkipObject.toString());
        }
      }
    }

    // Skip encryption if configured, or if value is already encrypted
    String input = context.toString();
    if (cryptoSkip || CryptoString.isCryptoString(input)) {
      return input;
    }

    byte[] cryptoKeyData;
    try {
      // get urls to crypto keys
      if (StringUtils.isBlank(cryptoAesKeyUrl)) {
        throw new IOException("Unable to encrypto string with AEM crypto keys: "
            + "Please add plugin configuration: '" + PLUGIN_NAME + ";" + PARAMETER_CRYPTO_AES_KEY_URL + "=/path/to/master");
      }

      // get crypto key
      try (InputStream is = pluginContext.getUrlFileManager().getFile(cryptoAesKeyUrl)) {
        cryptoKeyData = IOUtils.toByteArray(is);
      }
    }
    /*CHECKSTYLE:OFF*/
    catch (Exception ex) {
      /*CHECKSTYLE:ON*/
      if ((Boolean)options.hash(HASH_IGNORE_MISSING_KEY, false)) {
        return input;
      }
      else {
        throw ex;
      }
    }

    // encrypt input string
    try {
      CryptoSupport crypto = new AesCryptoSupport();
      Key cryptoKey = crypto.readKey(cryptoKeyData);
      return crypto.encrypt(input, cryptoKey);
    }
    catch (GeneralSecurityException ex) {
      throw new IOException("Unable to encrypt input string.", ex);
    }
  }

}
