/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import io.wcm.devops.conga.generator.util.PluginManager;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import org.junit.Before;
import org.junit.Test;

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChainHelperTest {

  private HelperPlugin<Object> helper;
  private HelperContext pluginContext;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    PluginManager pluginManager = new PluginManagerImpl();
    helper = pluginManager.get(ChainHelper.NAME, HelperPlugin.class);
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
      .pluginManager(pluginManager);
    pluginContext = new HelperContext()
      .pluginContextOptions(pluginContextOptions);
  }

  @Test
  public void testNull() throws Exception {
    Object chainedResult = executeHelper(helper, null, new MockOptions(), pluginContext);
    assertNull(chainedResult);
  }

  @Test
  public void testEmptyChain() throws Exception {
    Object chainedResult = executeHelper(helper, "value", new MockOptions().withHash(ChainHelper.HASH_OPTION_HELPERS, ""), pluginContext);
    assertEquals("value", chainedResult);
  }

  @Test
  public void testNotExistingChain() throws Exception {
    Object chainedResult = executeHelper(helper, "value", new MockOptions(), pluginContext);
    assertEquals("value", chainedResult);
  }

  @Test(expected = GeneratorException.class)
  public void testNotExistingHelpers() throws Exception {
    MockOptions mockOptions = new MockOptions()
      .withHash(ChainHelper.HASH_OPTION_HELPERS, "helper1,helper2");
    Object chainedResult = executeHelper(helper, "local.host", mockOptions, pluginContext);

  }

  @Test
  public void testChaining() throws Exception {
    MockOptions mockOptions = new MockOptions()
      .withHash(HttpHostHelper.HASH_OPTION_PORT, 8081)
      .withHash(ChainHelper.HASH_OPTION_HELPERS, "httpHost,webconsolePasswordHash");
    Object chainedResult = executeHelper(helper, "local.host", mockOptions, pluginContext);
    // hashed representation of "local.host:8081"
    assertEquals("{sha-256}9iFcpJv7gpP9ooMXpwiM4dvKnItIZTivYfs8oiv4qFw=", chainedResult);
  }

  @Test
  public void testChainingWithSpaces() throws Exception {
    MockOptions mockOptions = new MockOptions()
      .withHash(HttpHostHelper.HASH_OPTION_PORT, 8081)
      .withHash(ChainHelper.HASH_OPTION_HELPERS, "  httpHost ,  webconsolePasswordHash ");
    Object chainedResult = executeHelper(helper, "local.host", mockOptions, pluginContext);
    // hashed representation of "local.host:8081"
    assertEquals("{sha-256}9iFcpJv7gpP9ooMXpwiM4dvKnItIZTivYfs8oiv4qFw=", chainedResult);
  }

}
