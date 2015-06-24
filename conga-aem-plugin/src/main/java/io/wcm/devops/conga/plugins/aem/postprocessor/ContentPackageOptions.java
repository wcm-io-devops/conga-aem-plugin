/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.devops.conga.plugins.aem.postprocessor;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Option property names for content package post processors {@link ContentPackagePostProcessor} and
 * {@link ContentPackageOsgiConfigPostProcessor}.
 */
public final class ContentPackageOptions {

  private ContentPackageOptions() {
    // constants only
  }

  /**
   * Root path for content package (simplified version for setting just one filter)
   */
  public static final String PROPERTY_PACKAGE_ROOT_PATH = "contentPackageRootPath";

  /**
   * Contains list with filter definitions, optionally with include/exclude rules
   */
  public static final String PROPERTY_PACKAGE_FILTERS = "contentPackageFilters";

  /**
   * Group name for content package
   */
  public static final String PROPERTY_PACKAGE_GROUP = "contentPackageGroup";

  /**
   * Package name for content package
   */
  public static final String PROPERTY_PACKAGE_NAME = "contentPackageName";

  /**
   * Get property from options and throw exception if it is not set.
   * @param options Options
   * @param key Key
   * @return Option value
   */
  static String getMandatoryProp(Map<String, Object> options, String key) {
    Object value = options.get(key);
    if (value instanceof String) {
      return (String)value;
    }
    throw new GeneratorException("Missing post processor option '" + key + "'.");
  }

  /**
   * Get property from options and throw exception if it is not set.
   * @param options Options
   * @param key Key
   * @return Option value
   */
  @SuppressWarnings("unchecked")
  static List<Map<String, Object>> getOptionalPropMapList(Map<String, Object> options, String key) {
    Object value = options.get(key);
    if (value instanceof List || value == null) {
      return (List<Map<String, Object>>)value;
    }
    throw new GeneratorException("Missing post processor option '" + key + "'.");
  }

  /**
   * Get filter definitions either from contentPackageRootPath or from contentPackageFilters property.
   * @param options Options
   * @return Filters list
   */
  static List<PackageFilter> getFilters(Map<String, Object> options) {
    List<PackageFilter> filters = new ArrayList<>();

    String rootPath = getMandatoryProp(options, PROPERTY_PACKAGE_ROOT_PATH);
    List<Map<String, Object>> filterDefinitions = getOptionalPropMapList(options, PROPERTY_PACKAGE_FILTERS);

    if (filterDefinitions != null) {
      for (Map<String, Object> filterDefinition : filterDefinitions) {
        String filterRoot = getMandatoryProp(filterDefinition, "filter");
        PackageFilter filter = new PackageFilter(filterRoot);
        filters.add(filter);

        List<Map<String, Object>> ruleDefinitions = getOptionalPropMapList(filterDefinition, "rules");
        if (ruleDefinitions != null) {
          for (Map<String, Object> ruleDefinition : ruleDefinitions) {
            String rule = getMandatoryProp(ruleDefinition, "rule");
            String pattern = getMandatoryProp(ruleDefinition, "pattern");
            if (StringUtils.equals(rule, "include")) {
              filter.addIncludeRule(pattern);
            }
            else if (StringUtils.equals(rule, "exclude")) {
              filter.addExcludeRule(pattern);
            }
            else {
              throw new GeneratorException("Invalude rule '" + rule + "' in post processor options.");
            }
          }
        }
      }
    }
    else if (rootPath != null) {
      filters.add(new PackageFilter(rootPath));
    }

    if (filters.isEmpty()) {
      throw new GeneratorException("No content package filters defines in post processor options.");
    }

    return filters;
  }

}
