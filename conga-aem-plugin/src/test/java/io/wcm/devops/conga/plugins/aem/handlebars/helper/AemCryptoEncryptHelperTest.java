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
import static io.wcm.devops.conga.plugins.aem.handlebars.helper.AemCryptoEncryptHelper.HASH_IGNORE_MISSING_KEY;
import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.assertHelper;
import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.generator.UrlFileManager;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.context.UrlFilePluginContext;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import io.wcm.devops.conga.generator.util.PluginManager;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.plugins.aem.crypto.CryptoString;

class AemCryptoEncryptHelperTest {

  private static final String INPUT = "mytext";
  private static final String ENCRYPTED = "{bb71ce8b28ac304c269d7f9a4697f30d7f335a3f279c7834e8f72389d86539ce}";

  private HelperPlugin<Object> helper;
  private HelperContext pluginContext;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    PluginManager pluginManager = new PluginManagerImpl();
    helper = pluginManager.get(AemCryptoEncryptHelper.NAME, HelperPlugin.class);

    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(pluginManager)
        .urlFileManager(new UrlFileManager(pluginManager, new UrlFilePluginContext()));
    pluginContext = new HelperContext()
        .pluginContextOptions(pluginContextOptions);
  }

  @Test
  void testNull() throws Exception {
    Object passwordHash = executeHelper(helper, null, new MockOptions());
    assertNull(passwordHash);
  }

  @Test
  void testWithoutKey() throws Exception {
    assertThrows(IOException.class, () -> {
      executeHelper(helper, INPUT, new MockOptions(), pluginContext);
    });
  }

  @Test
  void testWithoutKey_Skip() throws Exception {
    pluginContext.getGenericPluginConfig().put(PLUGIN_NAME,
        Map.of(PARAMETER_CRYPTO_SKIP, true));

    assertHelper(INPUT, helper, INPUT, new MockOptions(), pluginContext);
  }

  @Test
  void testEncrypt() throws Exception {
    pluginContext.getGenericPluginConfig().put(PLUGIN_NAME,
        Map.of(PARAMETER_CRYPTO_AES_KEY_URL, "classpath:/crypto/master"));

    String encrypted = (String)executeHelper(helper, INPUT, new MockOptions(), pluginContext);
    assertTrue(CryptoString.isCryptoString(encrypted));
  }

  @Test
  void testEncrypt_Skip() throws Exception {
    pluginContext.getGenericPluginConfig().put(PLUGIN_NAME,
        Map.of(PARAMETER_CRYPTO_AES_KEY_URL, "classpath:/crypto/master",
            PARAMETER_CRYPTO_SKIP, "true"));

    assertHelper(INPUT, helper, INPUT, new MockOptions(), pluginContext);
  }

  @Test
  void testAlreadyEncrypted() throws Exception {
    assertHelper(ENCRYPTED, helper, ENCRYPTED, new MockOptions(), pluginContext);
  }

  @Test
  void testFallbackWithoutKey() throws Exception {
    assertHelper(INPUT, helper, INPUT, new MockOptions().withHash(HASH_IGNORE_MISSING_KEY, true), pluginContext);
  }

}
