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
import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.jknack.handlebars.Helper;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

public class OakAuthorizableUuidHelperTest {

  private static final String AUTHORIZABLE_ID = "myuser";
  private static final String AUHORZIZABLE_UUID = "5d5a582e-5adf-396e-96e1-474c700b481a";

  private Helper<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(OakAuthorizableUuidHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testNull() throws Exception {
    Object passwordHash = executeHelper(helper, null, new MockOptions());
    assertNull(passwordHash);
  }

  @Test
  public void testHash() throws Exception {
    assertHelper(AUHORZIZABLE_UUID, helper, AUTHORIZABLE_ID, new MockOptions());
  }

}
