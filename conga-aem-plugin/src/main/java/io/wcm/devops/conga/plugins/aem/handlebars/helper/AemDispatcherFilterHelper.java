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

import org.apache.commons.lang3.StringUtils;

import com.github.jknack.handlebars.Options;

/**
 * Handlebars helper that generates AEM dispatcher ANY access rules from a given object structure as defined by
 * https://docs.adobe.com/docs/en/dispatcher/disp-config.html#par_134_32_0009
 */
public final class AemDispatcherFilterHelper extends AbstractFilterHelper {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemDispatcherFilter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected String generateFilter(Map<String, Object> filterMap, Options options) {
    DispatcherFilter filter = new DispatcherFilter(filterMap);

    StringBuilder sb = new StringBuilder()
        .append("{ ");

    applySimpleValue(sb, "type", filter.getType().toString());

    applyRegexValue(sb, "method", filter.getMethod());
    applyRegexValue(sb, "url", filter.getUrl());
    applyRegexValue(sb, "query", filter.getQuery());
    applyRegexValue(sb, "protocol", filter.getProtocol());

    applyRegexValue(sb, "path", filter.getPath());
    applyRegexValueEmptyStringAllowed(sb, "selectors", filter.getSelectors());
    applyRegexValueEmptyStringAllowed(sb, "extension", filter.getExtension());
    applyRegexValueEmptyStringAllowed(sb, "suffix", filter.getSuffix());

    applyRegexValue(sb, "glob", filter.getGlob());

    sb.append("}");
    return sb.toString();
  }

  /**
   * Append filter parameter as simple fixed string (enclosed in "").
   */
  private void applySimpleValue(StringBuilder sb, String key, String value) {
    if (StringUtils.isNotEmpty(value)) {
      sb.append("/").append(key).append(" \"").append(value).append("\" ");
    }
  }

  /**
   * Append filter parameter as string that may be a regex (enclosed in ''). Empty string are ignored.
   */
  private void applyRegexValue(StringBuilder sb, String key, String value) {
    if (StringUtils.isNotEmpty(value)) {
      sb.append("/").append(key).append(" '").append(value).append("' ");
    }
  }

  /**
   * Append filter parameter as string that may be a regex (enclosed in ''). Empty strings are left as-is.
   */
  private void applyRegexValueEmptyStringAllowed(StringBuilder sb, String key, String value) {
    if (value != null) {
      sb.append("/").append(key).append(" '").append(value).append("' ");
    }
  }

}
