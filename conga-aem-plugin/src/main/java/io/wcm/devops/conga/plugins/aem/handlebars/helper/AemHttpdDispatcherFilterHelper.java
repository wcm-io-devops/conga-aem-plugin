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
 * Handlebars helper that generates HTTPd access rules from a given object structure as defined by
 * https://docs.adobe.com/docs/en/dispatcher/disp-config.html#par_134_32_0009
 */
public final class AemHttpdDispatcherFilterHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "aemHttpdDispatcherFilter";

  private final FilterRuleGenerator filterRuleGenerator = new FilterRuleGenerator();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object context, Options options) throws IOException {
    if (!(context instanceof Map)) {
      throw new IllegalArgumentException("Excpected map object for filter rule.");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>)context;
    FilterRule filter = new FilterRule(map);
    return filterRuleGenerator.generate(filter);
  }

}
