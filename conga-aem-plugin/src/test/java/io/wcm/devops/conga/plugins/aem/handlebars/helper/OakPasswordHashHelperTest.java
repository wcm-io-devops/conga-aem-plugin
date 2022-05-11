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

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

class OakPasswordHashHelperTest {

  private static final String PASSWORD_PLAIN = "mypassword";
  private static final String PASSWORD_HASH = "{SHA-256}c274ffe336bfc0dd-1000-f7673915a3cf354742c70e5aa11744589c0e22d66861ce4f65e84592ea67f7f1";

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    helper = new PluginManagerImpl().get(OakPasswordHashHelper.NAME, HelperPlugin.class);
  }

  @Test
  void testNull() throws Exception {
    Object passwordHash = executeHelper(helper, null, new MockOptions());
    assertNull(passwordHash);
  }

  @Test
  void testHash() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions());
    assertTrue(passwordHash instanceof String);
    assertTrue(PasswordUtil.isSame(passwordHash.toString(), PASSWORD_PLAIN));
  }

  @Test
  void testAlreadyHashed() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_HASH, new MockOptions());
    assertEquals(PASSWORD_HASH, passwordHash);
  }

}
