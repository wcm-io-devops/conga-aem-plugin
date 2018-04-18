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

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import org.junit.Before;
import org.junit.Test;

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpHostSslHelperTest {

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(HttpHostHelperSsl.NAME, HelperPlugin.class);
  }

  @Test
  public void testNull() throws Exception {
    Object httpHost = executeHelper(helper, null, new MockOptions());
    assertNull(httpHost);
  }

  @Test
  public void testContextWithPort() throws Exception {
    Object httpHost = executeHelper(helper, "localhost:8443", new MockOptions());
    assertTrue(httpHost instanceof String);
    assertEquals("localhost:8443", httpHost);
  }

  @Test
  public void testContextWithoutPort() throws Exception {
    Object httpHost = executeHelper(helper, "localhost", new MockOptions());
    assertTrue(httpHost instanceof String);
    assertEquals("localhost", httpHost);
  }

  @Test
  public void testWithCustomPort() throws Exception {
    Object httpHost = executeHelper(helper, "localhost", new MockOptions().withHash(HttpHostHelperSsl.HASH_OPTION_PORT, 9443));
    assertTrue(httpHost instanceof String);
    assertEquals("localhost:9443", httpHost);
  }

  @Test
  public void testWithDefaultPort() throws Exception {
    Object httpHost = executeHelper(helper, "localhost", new MockOptions().withHash(HttpHostHelperSsl.HASH_OPTION_PORT, HttpHostHelperSsl.DEFAULT_PORT));
    assertTrue(httpHost instanceof String);
    assertEquals("localhost", httpHost);
  }

}
