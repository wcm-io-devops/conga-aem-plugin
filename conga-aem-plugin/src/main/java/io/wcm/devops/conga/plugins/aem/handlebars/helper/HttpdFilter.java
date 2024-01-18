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

import java.util.HashMap;
import java.util.Map;

/**
 * Java bean that describes a simple HTTPd filter rule.
 */
final class HttpdFilter extends AbstractFilter {

  private final HttpdFilterType type;

  // only on of this parameters may be set at a time
  private final String location;
  private final String locationMatch;

  /**
   * Construct filter rule from map
   * @param map Map with filter definition
   */
  HttpdFilter(Map<String, Object> map) {
    Map<String, Object> mapCopy = new HashMap<>(map);
    this.type = getFilterType(mapCopy, HttpdFilterType.class);
    this.location = getValue(mapCopy, "location");
    this.locationMatch = getRegexValue(mapCopy, "locationMatch");

    // validate
    ensureNoMoreParams(mapCopy);

    int paramCount = 0;
    if (location != null) {
      paramCount++;
    }
    if (locationMatch != null) {
      paramCount++;
    }
    if (paramCount != 1) {
      throw new IllegalArgumentException("For each filter one (and only one) of these properties required: location, locationMatch");
    }
  }

  public HttpdFilterType getType() {
    return this.type;
  }

  public String getLocation() {
    return this.location;
  }

  public String getLocationMatch() {
    return this.locationMatch;
  }

}
