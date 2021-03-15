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
 * Handlebars helper that generates HTTPd access rules.
 */
public final class AemHttpdFilterHelper extends AbstractFilterHelper {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemHttpdFilter";

  static final String HASH_ALLOW_FROM_KEY = "allowFromKey";
  static final String HASH_ALLOW_FROM_HOST_KEY = "allowFromHostKey";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected String generateFilter(Map<String, Object> filterMap, Options options) {
    HttpdFilter filter = new HttpdFilter(filterMap);

    String ruleType;
    String ruleExpression;
    if (StringUtils.isNotBlank(filter.getLocationMatch())) {
      ruleType = "LocationMatch";
      ruleExpression = filter.getLocationMatch();
    }
    else {
      ruleType = "Location";
      ruleExpression = filter.getLocation();
    }

    String allowFromKey = options.hash(HASH_ALLOW_FROM_KEY, null);
    String allowFromHostKey = options.hash(HASH_ALLOW_FROM_HOST_KEY, null);
    String allowFrom = null;
    String allowFromHost = null;
    if (StringUtils.isNotBlank(allowFromKey)) {
      allowFrom = options.get(allowFromKey);
    }
    if (StringUtils.isNotBlank(allowFromHostKey)) {
      allowFromHost = options.get(allowFromHostKey);
    }

    return generateRule(ruleType, ruleExpression, filter.getType(),
        allowFrom, allowFromHost);
  }

  private String generateRule(String ruleType, String ruleExpression, HttpdFilterType filterType,
      String allowFrom, String allowFromHost) {
    StringBuilder sb = new StringBuilder();

    sb.append("<").append(ruleType).append(" \"").append(ruleExpression).append("\">\n");

    if (filterType == HttpdFilterType.ALLOW) {
      sb.append("  <IfVersion < 2.4>\n")
          .append("    Allow from all\n")
          .append("  </IfVersion>\n")
          .append("  <IfVersion >= 2.4>\n")
          .append("    Require all granted\n")
          .append("  </IfVersion>\n");
    }
    else {
      sb.append("  <IfVersion < 2.4>\n")
          .append("    Order Deny,Allow\n")
          .append("    Deny from all\n");
      if (filterType == HttpdFilterType.DENY_ALLOW_ADMIN) {
        if (StringUtils.isNotBlank(allowFrom)) {
          sb.append("    Allow from ").append(allowFrom).append("\n");
        }
        if (StringUtils.isNotBlank(allowFromHost)) {
          sb.append("    Allow from ").append(allowFromHost).append("\n");
        }
      }
      sb.append("  </IfVersion>\n")
          .append("  <IfVersion >= 2.4>\n")
          .append("    Require all denied\n");
      if (filterType == HttpdFilterType.DENY_ALLOW_ADMIN) {
        if (StringUtils.isNotBlank(allowFrom)) {
          sb.append("    Require ip ").append(allowFrom).append("\n");
        }
        if (StringUtils.isNotBlank(allowFromHost)) {
          sb.append("    Require host ").append(allowFromHost).append("\n");
        }
      }
      sb.append("  </IfVersion>\n");
    }

    sb.append("</").append(ruleType).append(">");

    return sb.toString();
  }

}
