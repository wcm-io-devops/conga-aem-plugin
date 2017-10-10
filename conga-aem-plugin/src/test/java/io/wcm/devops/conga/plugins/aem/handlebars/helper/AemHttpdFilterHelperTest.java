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

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.assertHelper;

import org.junit.Before;
import org.junit.Test;

import com.github.jknack.handlebars.Helper;
import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

public class AemHttpdFilterHelperTest {

  private Helper<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(AemHttpdFilterHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testLocation() throws Exception {
    assertHelper("<Location \"/abc\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Allow from all\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all granted\n" +
        "  </IfVersion>\n" +
        "</Location>",
        helper, ImmutableMap.of("type", "allow", "location", "/abc"), new MockOptions());
  }

  @Test
  public void testLocationMatch() throws Exception {
    assertHelper("<LocationMatch \"/abc(/.*)?\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Order Deny,Allow\n" +
        "    Deny from all\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all denied\n" +
        "  </IfVersion>\n" +
        "</LocationMatch>",
        helper, ImmutableMap.of("type", "deny", "locationMatch", "/abc(/.*)?"), new MockOptions());
  }

}
