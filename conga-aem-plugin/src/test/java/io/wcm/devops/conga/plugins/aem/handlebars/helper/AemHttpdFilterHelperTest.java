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

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

class AemHttpdFilterHelperTest {

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    helper = new PluginManagerImpl().get(AemHttpdFilterHelper.NAME, HelperPlugin.class);
  }

  @Test
  void testLocation() throws Exception {
    assertHelper("<Location \"/abc\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Allow from all\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all granted\n" +
        "  </IfVersion>\n" +
        "</Location>",
        helper, Map.of("type", "allow", "location", "/abc"), new MockOptions());
  }

  @Test
  void testLocationMatch() throws Exception {
    assertHelper("<LocationMatch \"/abc(/.*)?\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Order Deny,Allow\n" +
        "    Deny from all\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all denied\n" +
        "  </IfVersion>\n" +
        "</LocationMatch>",
        helper, Map.of("type", "deny", "locationMatch", "/abc(/.*)?"), new MockOptions());
  }

  @Test
  void testLocationDenyAllowAdmin_NoHash() throws Exception {
    assertHelper("<Location \"/abc\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Order Deny,Allow\n" +
        "    Deny from all\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all denied\n" +
        "  </IfVersion>\n" +
        "</Location>",
        helper, Map.of("type", "deny_allow_admin", "location", "/abc"), new MockOptions());
  }

  @Test
  void testLocationDenyAllowAdmin() throws Exception {
    assertHelper("<Location \"/abc\">\n" +
        "  <IfVersion < 2.4>\n" +
        "    Order Deny,Allow\n" +
        "    Deny from all\n" +
        "    Allow from 1.2.3.4\n" +
        "    Allow from myhost\n" +
        "  </IfVersion>\n" +
        "  <IfVersion >= 2.4>\n" +
        "    Require all denied\n" +
        "    Require ip 1.2.3.4\n" +
        "    Require host myhost\n" +
        "  </IfVersion>\n" +
        "</Location>",
        helper, Map.of("type", "deny_allow_admin", "location", "/abc"), new MockOptions()
            .withHash(AemHttpdFilterHelper.HASH_ALLOW_FROM_KEY, "allowFrom")
            .withHash(AemHttpdFilterHelper.HASH_ALLOW_FROM_HOST_KEY, "allowFromHost")
            .withProperty("allowFrom", "1.2.3.4")
            .withProperty("allowFromHost", "myhost"));
  }

}
