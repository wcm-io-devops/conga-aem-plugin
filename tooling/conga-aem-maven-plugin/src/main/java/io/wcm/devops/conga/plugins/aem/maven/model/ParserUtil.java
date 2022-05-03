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
package io.wcm.devops.conga.plugins.aem.maven.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper methods for {@link ModelParser}.
 */
final class ParserUtil {

  private static final String PROP_VARIANTS = "variants";

  private ParserUtil() {
    // static methods only
  }

  @SuppressWarnings("unchecked")
  static List<Map<String, Object>> children(Map<String, Object> data, String propertyName) {
    List<Map<String, Object>> children = (List<Map<String, Object>>)data.get(propertyName);
    if (children == null) {
      return Collections.emptyList();
    }
    else {
      return children;
    }
  }

  @SuppressWarnings("unchecked")
  static Set<String> toStringSet(Object value) {
    Set<String> result = new LinkedHashSet<>();
    if (value instanceof String) {
      String target = (String)value;
      if (StringUtils.isNotBlank(target)) {
        result.add(target);
      }
    }
    else if (value instanceof List) {
      result.addAll(((List<String>)value).stream()
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toList()));
    }
    else {
      throw new IllegalArgumentException("Value is neither string nor string list: " + value);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  static List<String> getVariants(Map<String, Object> roleData) {
    List<String> variants = (List<String>)roleData.get(PROP_VARIANTS);
    if (variants != null) {
      return variants;
    }
    else {
      return Collections.emptyList();
    }
  }

}
