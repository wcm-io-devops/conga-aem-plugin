/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.devops.conga.plugins.aem.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.yaml.YamlConstructorPlugin;
import io.wcm.devops.conga.generator.spi.yaml.context.YamlConstructorContext;
import io.wcm.devops.conga.generator.util.PluginManager;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

final class YamlUtil {

  private YamlUtil() {
    // static methods only
  }

  private static final Logger log = LoggerFactory.getLogger(YamlUtil.class);

  public static Yaml createYaml() {
    // initialize CONGA plugin manager
    PluginManager pluginManager = new PluginManagerImpl();
    PluginContextOptions options = new PluginContextOptions()
        .pluginManager(pluginManager)
        .logger(log);

    // apply YAML plugins for modifying YAML constructor
    Constructor constructor = new Constructor();
    YamlConstructorContext context = new YamlConstructorContext()
        .pluginContextOptions(options)
        .yamlConstructor(constructor);
    for (YamlConstructorPlugin plugin : pluginManager.getAll(YamlConstructorPlugin.class)) {
      plugin.register(context);
    }

    return new Yaml(constructor);
  }

}
