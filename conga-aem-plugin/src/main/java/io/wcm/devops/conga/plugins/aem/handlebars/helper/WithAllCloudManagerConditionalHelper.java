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

import com.github.jknack.handlebars.Options.Buffer;

/**
 * Checks if there is a conditional statement 'httpd.cloudManagerConditional' in the current context.
 * If yes iterates over all environments and renders the body for each with a merged model.
 * If no the body is rendered once with the default model.
 */
public final class WithAllCloudManagerConditionalHelper extends AbstractCloudManagerConditionalHelper {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "withAllCloudManagerConditional";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected void renderBodyContent(Buffer buffer, CharSequence bodyContent,
      String targetEnvironment) throws IOException {
    buffer.append(bodyContent);
  }

}
