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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.ContextProperties;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.model.environment.Tenant;
import io.wcm.devops.conga.plugins.aem.handlebars.helper.AemTenantsCloudManagerConditionalHelper.CloudManagerConditional;

public class AemTenantsCloudManagerConditionalHelperTest {

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setUp() {
    helper = new PluginManagerImpl().get(AemTenantsCloudManagerConditionalHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testNull() throws Exception {
    Options options = new MockOptions();

    assertHelper(null, helper, "my-var", options);

    Map<String, List<Map<String, Object>>> expectedResult = ImmutableMap.of();
    assertEquals(expectedResult, options.context.get("my-var"));
  }

  @Test
  public void testEmpty() throws Exception {
    Options options = new MockOptions();

    List<Map<String, Object>> tenants = ImmutableList.of();
    options.context.data(ContextProperties.TENANTS, tenants);

    assertHelper(null, helper, "my-var", options);

    Map<String, List<Map<String, Object>>> expectedResult = ImmutableMap.of();
    assertEquals(expectedResult, options.context.get("my-var"));
  }

  @Test
  public void testTenants() throws Exception {
    Options options = new MockOptions();

    Map<String, Object> tenant1_dev = ImmutableMap.of("targetEnvironment", "dev", "serverName", "name1-dev");
    Map<String, Object> tenant1_stage = ImmutableMap.of("targetEnvironment", "stage", "serverName", "name1-stage");
    Map<String, Object> tenant2_dev = ImmutableMap.of("targetEnvironment", "dev", "serverName", "name2-dev");
    Map<String, Object> tenant2_null = ImmutableMap.of("serverName", "name2-null");

    Tenant tenant1 = new Tenant();
    tenant1.setTenant("tenant1");
    tenant1.setConfig(ImmutableMap.of("httpd", ImmutableMap.of("cloudManagerConditional", ImmutableList.of(tenant1_dev, tenant1_stage))));

    Tenant tenant2 = new Tenant();
    tenant2.setTenant("tenant2");
    tenant2.setConfig(ImmutableMap.of("httpd", ImmutableMap.of("cloudManagerConditional", ImmutableList.of(tenant2_dev, tenant2_null))));

    Tenant tenant3 = new Tenant();
    tenant3.setTenant("tenant3");
    tenant3.setConfig(ImmutableMap.of());

    Tenant tenant4 = new Tenant();
    tenant4.setTenant("tenant4");
    tenant4.setConfig(ImmutableMap.of("httpd", ImmutableMap.of("cloudManagerConditional", "invalid")));

    List<Tenant> tenants = ImmutableList.of(tenant1, tenant2, tenant3, tenant4);
    options.context.data(ContextProperties.TENANTS, tenants);

    assertHelper(null, helper, "my-var", options);

    Map<String, List<CloudManagerConditional>> expectedResult = ImmutableMap.of(
        "dev", ImmutableList.of(
            new CloudManagerConditional(tenant1_dev, tenant1),
            new CloudManagerConditional(tenant2_dev, tenant2)),
        "stage", ImmutableList.of(
            new CloudManagerConditional(tenant1_stage, tenant1)));
    assertEquals(expectedResult, options.context.get("my-var"));
  }

}
