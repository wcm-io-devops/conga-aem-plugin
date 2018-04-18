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

/**
 * Abstract base class for host helpers
 */
abstract class AbstractHostHelper {

  /**
   * Option for setting port
   */
  public static final String HASH_OPTION_PORT = "port";

  /**
   * Helper function to detect if the host already contains a port
   *
   * @param host The host
   * @return true when the host already contains a port
   */
  private Boolean hasPort(String host) {
    return host.matches("^.+:\\d+$");
  }

  /**
   * Adds a port to the incoming host when it is not the default one
   *
   * @param context The context
   * @param options Options
   * @param defaultPort The default port that should not be added
   * @return The hostname with port (when necessary)
   */
  Object addNonDefaultPort(Object context, Options options, Integer defaultPort) {
    StringBuilder sb = new StringBuilder();
    if (context == null) {
      return null;
    }
    String host = context.toString();
    // host has already a defined port
    if (this.hasPort(host)) {
      return context;
    }
    // retrieve the port, or take default one
    Integer port = options.hash(HASH_OPTION_PORT, defaultPort);

    // build result
    sb.append(host);
    if (! port.equals(defaultPort)) {
      // add port when it is not the default one
      sb.append(":").append(port);
    }
    return sb.toString();
  }

}
