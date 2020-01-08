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
        .append("{ ")
        .append("/type \"").append(filter.getType()).append("\" ");

    if (StringUtils.isNotEmpty(filter.getMethod())) {
      sb.append("/method '").append(filter.getMethod()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getUrl())) {
      sb.append("/url '").append(filter.getUrl()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getQuery())) {
      sb.append("/query '").append(filter.getQuery()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getProtocol())) {
      sb.append("/protocol '").append(filter.getProtocol()).append("' ");
    }

    if (StringUtils.isNotEmpty(filter.getPath())) {
      sb.append("/path '").append(filter.getPath()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getSelectors())) {
      sb.append("/selectors '").append(filter.getSelectors()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getExtension())) {
      sb.append("/extension '").append(filter.getExtension()).append("' ");
    }
    if (StringUtils.isNotEmpty(filter.getSuffix())) {
      sb.append("/suffix '").append(filter.getSuffix()).append("' ");
    }

    if (StringUtils.isNotEmpty(filter.getGlob())) {
      sb.append("/glob '").append(filter.getGlob()).append("' ");
    }

    sb.append("}");
    return sb.toString();
  }

}
