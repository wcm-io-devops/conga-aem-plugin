/*-
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 - 2018 wcm.io DevOps
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

import com.github.jknack.handlebars.Options;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChainHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "chain";

  /**
   * Plugin/Helper name
   */
  public static final String HASH_OPTION_HELPERS = "helpers";

  @Override public Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    if (context == null) {
      return null;
    }
    String helpers = options.hash(HASH_OPTION_HELPERS, null);
    List<HelperPlugin> helperPlugins = this.getHelpers(helpers, pluginContext);
    for (HelperPlugin helperPlugin : helperPlugins) {
      context = helperPlugin.apply(context, options, pluginContext);
    }

    return context;
  }

  private List<HelperPlugin> getHelpers(String helperStr, HelperContext pluginContext) {
    List<HelperPlugin> helperPlugins = new ArrayList<HelperPlugin>();
    if (helperStr == null) {
      return helperPlugins;
    }

    String[] helperNames = helperStr.split(",");
    for (String helperName : helperNames) {
      helperName = helperName.trim();
      if (helperName != "") {
        HelperPlugin helperPlugin = pluginContext.getPluginManager().get(helperName, HelperPlugin.class);
        helperPlugins.add(helperPlugin);
      }
    }

    return helperPlugins;
  }

  @Override public String getName() {
    return NAME;
  }
}
