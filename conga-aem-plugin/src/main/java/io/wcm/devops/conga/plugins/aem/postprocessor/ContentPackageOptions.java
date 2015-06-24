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

import java.util.Map;

/**
 * Option property names for content package post processors {@link ContentPackagePostProcessor} and
 * {@link ContentPackageOsgiConfigPostProcessor}.
 */
public final class ContentPackageOptions {

  private ContentPackageOptions() {
    // constants only
  }

  /**
   * Root path for content package
   */
  public static final String PROPERTY_PACKAGE_ROOT_PATH = "contentPackageRootPath";

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

}
