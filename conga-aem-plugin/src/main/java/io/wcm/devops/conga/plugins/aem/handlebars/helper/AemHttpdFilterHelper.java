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

/**
 * Handlebars helper that generates HTTPd access rules.
 */
public final class AemHttpdFilterHelper extends AbstractFilterHelper {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemHttpdFilter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected String generateFilter(Map<String, Object> filterMap) {
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
    return generateRule(ruleType, ruleExpression, filter.getType());
  }

  private String generateRule(String ruleType, String ruleExpression, HttpdFilterType filterType) {
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
      // TODO: special handling for DENY_ALLOW_ADMIN
      sb.append("  <IfVersion < 2.4>\n")
          .append("    Order Deny,Allow\n")
          .append("    Deny from all\n")
          .append("  </IfVersion>\n")
          .append("  <IfVersion >= 2.4>\n")
          .append("    Require all denied\n")
          .append("  </IfVersion>\n");
    }

    sb.append("</").append(ruleType).append(">");

    return sb.toString();
  }

}
