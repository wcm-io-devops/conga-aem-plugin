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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.jknack.handlebars.Helper;
import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

public class AemDispatcherFilterHelperTest {

  private Helper<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(AemDispatcherFilterHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testUrl() throws Exception {
    assertHelper("{ /type \"allow\" /url '/abc(/.*)?' }",
        helper, ImmutableMap.of("type", "allow", "url", "/abc(/.*)?"), new MockOptions());
  }

  @Test
  public void testAll() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("type", "deny");
    map.put("method", "method1");
    map.put("url", "url1");
    map.put("query", "query1");
    map.put("protocol", "protocol1");
    map.put("path", "path1");
    map.put("selectors", "selector1");
    map.put("extension", "extension1");
    map.put("suffix", "suffix1");
    map.put("glob", "glob1");
    assertHelper("{ /type \"deny\" /method 'method1' /url 'url1' /query 'query1' /protocol 'protocol1' "
        + "/path 'path1' /selectors 'selector1' /extension 'extension1' /suffix 'suffix1' "
        + "/glob 'glob1' }",
        helper, map, new MockOptions());
  }

}
