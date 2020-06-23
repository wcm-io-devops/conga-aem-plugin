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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.ContextProperties;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import io.wcm.devops.conga.model.environment.Tenant;
import io.wcm.devops.conga.model.util.MapExpander;

/**
 * Gets all tenants which have a "httpd.cloudManagerConditional" configuration set, grouped by targetEnvironment value.
 * The resulting map with environment name as key and list of conditional configuration maps as value is
 * set to a context variable with the given name.
 */
public final class AemTenantsCloudManagerConditionalHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemTenantsCloudManagerConditional";

  static final String CLOUD_MANAGER_CONDITIONAL_KEY = "httpd.cloudManagerConditional";
  static final String TARGET_ENVIRONMENT_KEY = "targetEnvironment";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    String variableName = null;
    if (context instanceof String) {
      variableName = (String)context;
    }

    // get tenants from context
    Object tenants = options.context.get(ContextProperties.TENANTS);

    // generate grouped result map
    Map<String, List<CloudManagerConditional>> result = getTenantConfigs(tenants).stream()
        .flatMap(tenantConfig -> tenantConfig.getCloudManagerConditionals().stream())
        .filter(item -> item.getTargetEnvironment() != null)
        .collect(Collectors.groupingBy(CloudManagerConditional::getTargetEnvironment, LinkedHashMap::new, Collectors.toList()));

    // set variable
    if (variableName != null) {
      options.context.data(variableName, result);
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private List<TenantConfig> getTenantConfigs(Object object) {
    List<TenantConfig> result = new ArrayList<>();
    if (object instanceof List) {
      List<Tenant> tenants = (List<Tenant>)object;
      for (Tenant tenant : tenants) {
        result.add(new TenantConfig(tenant));
      }
    }
    return result;
  }

  private static class TenantConfig {

    private final Tenant tenant;

    TenantConfig(Tenant tenant) {
      this.tenant = tenant;
    }

    @SuppressWarnings("unchecked")
    public List<CloudManagerConditional> getCloudManagerConditionals() {
      List<CloudManagerConditional> result = new ArrayList<>();
      Object object = MapExpander.getDeep(tenant.getConfig(), CLOUD_MANAGER_CONDITIONAL_KEY);
      if (object instanceof List) {
        List<Map<String, Object>> items = (List<Map<String, Object>>)object;
        for (Map<String, Object> item : items) {
          result.add(new CloudManagerConditional(item, tenant));
        }
      }
      return result;
    }

  }

  /**
   * Cloud manager conditional result per tenant and target environment.
   */
  public static class CloudManagerConditional {

    private final Map<String, Object> config;
    private final Tenant tenant;
    private final String targetEnvironment;

    CloudManagerConditional(Map<String, Object> config, Tenant tenant) {
      this.config = config;
      this.tenant = tenant;
      Object value = config.get(TARGET_ENVIRONMENT_KEY);
      if (value instanceof String) {
        this.targetEnvironment = (String)value;
      }
      else {
        this.targetEnvironment = null;
      }
    }

    public String getTargetEnvironment() {
      return targetEnvironment;
    }

    public Map<String, Object> getConfig() {
      return config;
    }

    public Tenant getTenant() {
      return this.tenant;
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder()
          .append(config)
          .append(tenant.getTenant())
          .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CloudManagerConditional) {
        CloudManagerConditional other = (CloudManagerConditional)obj;
        return new EqualsBuilder()
            .append(config, other.config)
            .append(tenant.getTenant(), other.tenant.getTenant())
            .isEquals();
      }
      return false;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
          .append("config", config)
          .append("tenant", tenant.getTenant())
          .toString();
    }

  }

}
