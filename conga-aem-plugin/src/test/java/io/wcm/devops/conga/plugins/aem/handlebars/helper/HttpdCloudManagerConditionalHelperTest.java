/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.MockOptions.FN_RETURN;
import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.assertHelper;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

class HttpdCloudManagerConditionalHelperTest {

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    helper = new PluginManagerImpl().get(HttpdCloudManagerConditionalHelper.NAME, HelperPlugin.class);
  }

  @Test
  void testApply() throws IOException {
    Map<String, Object> model = ImmutableMap.of("httpd", ImmutableMap.of("serverName", "host1"));
    MockOptions options = new MockOptions(model);

    assertHelper(FN_RETURN + "(" + model + ")", helper, null, options);
  }

  @Test
  void testApplySeparateModel() throws IOException {
    Map<String, Object> model = ImmutableMap.of("httpd", ImmutableMap.of("serverName", "host1"));
    MockOptions options = new MockOptions();

    assertHelper(FN_RETURN + "(" + model + ")", helper, model, options);
  }

  @Test
  void testApplyWithCloudManagerConditional() throws IOException {
    Map<String, Object> model = ImmutableMap.of("httpd",
        ImmutableMap.of("serverName", "host0",
            "cloudManagerConditional", ImmutableMap.of(
                "dev", ImmutableMap.of("serverName", "host1"),
                "stage", ImmutableMap.of("serverName", "host2"),
                "prod", ImmutableMap.of())));
    MockOptions options = new MockOptions(model);

    Map<String, Object> model_dev = ImmutableMap.of("httpd", ImmutableMap.of("serverName", "host1"));
    Map<String, Object> model_stage = ImmutableMap.of("httpd", ImmutableMap.of("serverName", "host2"));
    Map<String, Object> model_prod = ImmutableMap.of("httpd", ImmutableMap.of("serverName", "host0"));
    assertHelper("<IfDefine ENVIRONMENT_DEV>" + FN_RETURN + "(" + model_dev + ")</IfDefine>\n"
        + "<IfDefine ENVIRONMENT_STAGE>" + FN_RETURN + "(" + model_stage + ")</IfDefine>\n"
        + "<IfDefine ENVIRONMENT_PROD>" + FN_RETURN + "(" + model_prod + ")</IfDefine>\n",
        helper, null, options);
  }

}
