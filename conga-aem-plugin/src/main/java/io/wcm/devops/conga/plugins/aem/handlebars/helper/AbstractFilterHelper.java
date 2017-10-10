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

import java.io.IOException;
import java.util.Map;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;

/**
 * Handlebars helper that generates a filter rule.
 */
abstract class AbstractFilterHelper implements HelperPlugin<Object> {

  @Override
  public final Object apply(Object context, Options options) throws IOException {
    if (!(context instanceof Map)) {
      throw new IllegalArgumentException("Excpected map object for filter rule.");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>)context;
    try {
      return generateFilter(map, options);
    }
    catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid filter rule: " + ex.getMessage() + "\n" + toYaml(map));
    }
  }

  private String toYaml(Map<String, Object> filterMap) {
    StringBuilder sb = new StringBuilder();
    filterMap.entrySet().forEach(entry -> {
      if (sb.length() == 0) {
        sb.append("- ");
      }
      else {
        sb.append("\n  ");
      }
      sb.append(entry.getKey()).append(": ").append(entry.getValue());
    });
    return sb.toString();
  }

  protected abstract String generateFilter(Map<String, Object> filterMap, Options options);

}
