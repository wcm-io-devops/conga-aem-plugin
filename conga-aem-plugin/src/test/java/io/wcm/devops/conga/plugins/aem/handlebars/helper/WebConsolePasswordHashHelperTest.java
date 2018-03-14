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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

public class WebConsolePasswordHashHelperTest {

  private static final String PASSWORD_PLAIN = "password";
  private static final String PASSWORD_HASH_SHA256 = "{sha-256}XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=";
  private static final String PASSWORD_HASH_SHA512 = "{sha-512}sQnzu7wkTrgkQZF+0G1hi5AI3Qmzvv0bXgc5THBqi7mAsdd4Xll27ASbRt9fEyavWi6m0QP9B8lThf+rDKy8hg==";

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(WebConsolePasswordHashHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testNull() throws Exception {
    Object passwordHash = executeHelper(helper, null, new MockOptions());
    assertNull(passwordHash);
  }

  @Test
  public void testHash() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions());
    assertTrue(passwordHash instanceof String);
    assertEquals(PASSWORD_HASH_SHA256, passwordHash);
  }

  @Test
  public void testAlreadyHashed() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_HASH_SHA256, new MockOptions());
    assertEquals(PASSWORD_HASH_SHA256, passwordHash);
  }

  @Test
  public void testCustomAlgorithm() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions().withHash(WebConsolePasswordHashHelper.HASH_OPTION_ALGORITHM, "SHA-512"));
    assertEquals(PASSWORD_HASH_SHA512, passwordHash);
  }

  @Test(expected = IOException.class)
  public void testInvalidHashAlgorithm() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions().withHash(WebConsolePasswordHashHelper.HASH_OPTION_ALGORITHM, "invalid"));
    assertEquals(PASSWORD_HASH_SHA256, passwordHash);
  }

  @Test(expected = IOException.class)
  public void testInvalidEncoding() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions().withHash(WebConsolePasswordHashHelper.HASH_OPTION_ENCODING, "invalid"));
    assertEquals(PASSWORD_HASH_SHA256, passwordHash);
  }

}
