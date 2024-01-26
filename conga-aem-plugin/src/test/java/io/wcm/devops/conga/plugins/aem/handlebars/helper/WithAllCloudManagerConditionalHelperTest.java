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

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

class WithAllCloudManagerConditionalHelperTest {

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    helper = new PluginManagerImpl().get(WithAllCloudManagerConditionalHelper.NAME, HelperPlugin.class);
  }

  @Test
  void testApply() throws IOException {
    Map<String, Object> model = Map.of("httpd", Map.of("serverName", "host1"));
    MockOptions options = new MockOptions(model);

    assertHelper(FN_RETURN + "(" + model + ")", helper, null, options);
  }

  @Test
  void testApplySeparateModel() throws IOException {
    Map<String, Object> model = Map.of("httpd", Map.of("serverName", "host1"));
    MockOptions options = new MockOptions();

    assertHelper(FN_RETURN + "(" + model + ")", helper, model, options);
  }

  @Test
  void testApplyWithCloudManagerConditional() throws IOException {
    Map<String, Object> model = Map.of("httpd",
        Map.of("serverName", "host0",
            "cloudManagerConditional", Map.of(
                "dev", Map.of("serverName", "host1"),
                "stage", Map.of("serverName", "host2"),
                "prod", Map.of())));
    MockOptions options = new MockOptions(model);

    Map<String, Object> model_dev = Map.of("httpd", Map.of("serverName", "host1"));
    Map<String, Object> model_stage = Map.of("httpd", Map.of("serverName", "host2"));
    Map<String, Object> model_prod = Map.of("httpd", Map.of("serverName", "host0"));
    assertHelper(FN_RETURN + "(" + model_dev + ")"
        + FN_RETURN + "(" + model_stage + ")"
        + FN_RETURN + "(" + model_prod + ")",
        helper, null, options);
  }

  @Test
  void testApplyWithCloudManagerConditionalSeparateModel() throws IOException {
    Map<String, Object> model = Map.of("httpd",
        Map.of("serverName", "host0",
            "cloudManagerConditional", Map.of(
                "dev", Map.of("serverName", "host1"),
                "stage", Map.of("serverName", "host2"))));
    MockOptions options = new MockOptions();

    Map<String, Object> model_dev = Map.of("httpd", Map.of("serverName", "host1"));
    Map<String, Object> model_stage = Map.of("httpd", Map.of("serverName", "host2"));
    Map<String, Object> model_prod = Map.of("httpd", Map.of("serverName", "host0"));
    assertHelper(FN_RETURN + "(" + model_dev + ")"
        + FN_RETURN + "(" + model_stage + ")"
        + FN_RETURN + "(" + model_prod + ")",
        helper, model, options);
  }

}
