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

/**
 * Java bean that describes a AEM dispatcher filter rule as defined in
 * https://docs.adobe.com/docs/en/dispatcher/disp-config.html#par_134_32_0009
 */
class FilterRule {

  /*
   * The /type indicates whether to allow or deny access for the requests that match the pattern.
   * The value can be either allow or deny.
   */
  private final FilterType type;

  /*
   * Include /method, /url, /query, or /protocol and a pattern for filtering requests according
   * to these specific parts of the request-line part of the HTTP request.
   * Filtering on elements of the request line (rather than on the entire request line)
   * is the preferred filter method.
   * => all strings are interpreted as regular expressions
   */
  private final String method;
  private final String url;
  private final String query;
  private final String protocol;

  /*
   * One of the enhancements introduced in dispatcher 4.2.0 is the ability to filter additional
   * elements of the request URL. The new elements introduced are:
   * => all strings are interpreted as regular expressions
   */
  private final String path;
  private final String selectors;
  private final String extension;
  private final String suffix;

  /*
   * The /glob property is used to match with the entire request-line of the HTTP request.
   * => all strings are interpreted as regular expressions
   */
  private final String glob;

  /**
   * Construct filter rule from map
   * @param map Map with filter definition
   */
  FilterRule(Map<String, Object> map) {
    this.type = getFilterType(map);
    this.method = getValue(map, "method");
    this.url = getValue(map, "url");
    this.query = getValue(map, "query");
    this.protocol = getValue(map, "protocol");
    this.path = getValue(map, "path");
    this.selectors = getValue(map, "selectors");
    this.extension = getValue(map, "extension");
    this.suffix = getValue(map, "suffix");
    this.glob = getValue(map, "glob");

    if (method == null && url == null && query == null && protocol == null
        && path == null && selectors == null && extension == null && suffix == null
        && glob == null) {
      throw new IllegalArgumentException("Require any definition of: method, url, query, protocol, path, selectors, extension, suffix, glob.");
    }
  }

  private String getValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof String) {
      String stringValue = (String)value;

      // compile value to regex to validate it
      try {
        Pattern.compile(stringValue);
      }
      catch (PatternSyntaxException ex) {
        throw new IllegalArgumentException("Invalid regex for '" + key + "': " + ex.getMessage());
      }

      return stringValue;
    }
    else {
      return null;
    }
  }

  private FilterType getFilterType(Map<String, Object> map) {
    String typeValue = getValue(map, "type");
    if (typeValue == null) {
      throw new IllegalArgumentException("Type expression missing.");
    }
    return FilterType.valueOf(StringUtils.upperCase(typeValue));
  }

  public FilterType getType() {
    return this.type;
  }

  public String getMethod() {
    return this.method;
  }

  public String getUrl() {
    return this.url;
  }

  public String getQuery() {
    return this.query;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public String getPath() {
    return this.path;
  }

  public String getSelectors() {
    return this.selectors;
  }

  public String getExtension() {
    return this.extension;
  }

  public String getSuffix() {
    return this.suffix;
  }

  public String getGlob() {
    return this.glob;
  }

}
