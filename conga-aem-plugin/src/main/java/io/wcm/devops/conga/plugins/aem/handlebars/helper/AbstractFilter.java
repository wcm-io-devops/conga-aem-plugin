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

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

abstract class AbstractFilter {

  /**
   * Gets and removes value from map.
   * @param map Map
   * @param key Key
   * @return Value or null if not set
   */
  protected final String getValue(Map<String, Object> map, String key) {
    // get and remove value from map
    Object value = map.remove(key);
    if (value instanceof String stringValue) {
      return stringValue;
    }
    else {
      return null;
    }
  }

  /**
   * Gets and removes value from map and validtes as regex.
   * @param map Map
   * @param key Key
   * @return Value or null if not set
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  protected final String getRegexValue(Map<String, Object> map, String key) {
    String value = getValue(map, key);

    // compile value to regex to validate it
    if (value != null) {
      try {
        Pattern.compile(value);
      }
      catch (PatternSyntaxException ex) {
        throw new IllegalArgumentException("Invalid regex for '" + key + "': " + ex.getMessage());
      }
    }

    return value;
  }

  /**
   * Gets and removes type and casts it to enum.
   * @param map Map
   * @param enumType Type class
   * @return Type value - never null
   */
  protected final <T extends Enum<T>> T getFilterType(Map<String, Object> map, Class<T> enumType) {
    String typeValue = getValue(map, "type");
    if (typeValue == null) {
      throw new IllegalArgumentException("Type expression missing.");
    }
    return Enum.valueOf(enumType, StringUtils.upperCase(typeValue));
  }

  /**
   * Validates that map is empty and does not contain more parameters.
   * @param map Map
   */
  protected final void ensureNoMoreParams(Map<String, Object> map) {
    if (!map.isEmpty()) {
      throw new IllegalArgumentException("Unexpected properties for filter rule: " + StringUtils.join(map.keySet(), ", "));
    }
  }

  @Override
  public final String toString() {
    return ToStringBuilder.reflectionToString(this, NoClassNameOmitNullsStyle.TOSTRING_STYLE);
  }

}
