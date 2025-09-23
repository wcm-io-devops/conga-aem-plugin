/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Options.Buffer;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import io.wcm.devops.conga.model.util.MapMerger;

/**
 * Checks if there is a conditional statement 'httpd.cloudManagerConditional' in the current context.
 * If yes iterates over all environments and renders the body for each with a merged model.
 * If no the body is rendered once with the default model.
 */
abstract class AbstractCloudManagerConditionalHelper implements HelperPlugin<Object> {

  static final String HTTPD_KEY = "httpd";
  static final String CLOUD_MANAGER_CONDITIONAL_KEY = "cloudManagerConditional";
  static final List<String> ENVIRONMENTS = List.of("dev", "stage", "prod");

  @Override
  @SuppressWarnings("unchecked")
  public final Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    Buffer buffer = options.buffer();

    Context currentContext = options.context;
    if (context != null) {
      currentContext = Context.copy(options.context, context);
    }

    // get tenants from context
    Object cloudManagerConditional = currentContext.get(HTTPD_KEY + "." + CLOUD_MANAGER_CONDITIONAL_KEY);

    if (!(cloudManagerConditional instanceof Map)) {
      // no conditional statement - just render the body
      buffer.append(options.fn(currentContext));
    }
    else {
      // render body for each environment in conditional block with a merged context
      List<CloudManagerConditional> items = getCloudManagerConditional((Map<String, Object>)cloudManagerConditional);
      for (CloudManagerConditional item : items) {

        // config inside httpd.cloudManagerConditional is considered to be prefixed with "httpd.", so wrap it around here for merging
        Map<String, Object> httpdWrapper = Map.of(HTTPD_KEY, item.getConfig());
        Map<String, Object> mergedModel = MapMerger.merge(httpdWrapper, (Map<String, Object>)currentContext.model());

        // remove cloudManagerConditional after merging the models
        Map<String, Object> mergedHttpd = (Map<String, Object>)mergedModel.get(HTTPD_KEY);
        if (mergedHttpd != null) {
          mergedHttpd.remove(CLOUD_MANAGER_CONDITIONAL_KEY);
        }

        Context mergedContext = Context.copy(currentContext, mergedModel);
        CharSequence bodyContent = options.fn(mergedContext);

        if (StringUtils.isNotBlank(bodyContent)) {
          renderBodyContent(buffer, bodyContent, item.getTargetEnvironment());
        }
      }
    }

    return buffer;
  }

  protected abstract void renderBodyContent(Buffer buffer, CharSequence bodyContent,
      String targetEnvironment) throws IOException;

  private List<CloudManagerConditional> getCloudManagerConditional(Map<String, Object> cloudManagerConditional) {
    return ENVIRONMENTS.stream()
        .map(env -> new CloudManagerConditional(env, cloudManagerConditional.getOrDefault(env, Map.of())))
        .toList();
  }

  /**
   * Cloud manager conditional result per tenant and target environment.
   */
  private static class CloudManagerConditional {

    private final Map<String, Object> config;
    private final String targetEnvironment;

    @SuppressWarnings("unchecked")
    CloudManagerConditional(String targetEnvironment, Object value) {
      this.targetEnvironment = targetEnvironment;
      if (value instanceof Map) {
        this.config = (Map<String, Object>)value;
      }
      else {
        this.config = Map.of();
      }
    }

    public String getTargetEnvironment() {
      return targetEnvironment;
    }

    public Map<String, Object> getConfig() {
      return config;
    }

  }

}
