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
package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;

/**
 * Handlebars helper that builds a password hash for a given password string of an Oak JCR user.
 */
public final class OakPasswordHashHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "oakPasswordHash";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object context, Options options) throws IOException {
    if (context == null) {
      return null;
    }

    String password = context.toString();

    // if password is already encrypted skip further processing
    if (!PasswordUtil.isPlainTextPassword(password)) {
      return password;
    }

    ConfigurationParameters config = ConfigurationParameters.of(options.hash);
    try {
      return PasswordUtil.buildPasswordHash(password, config);
    }
    catch (NoSuchAlgorithmException ex) {
      throw new IOException("Unable build password hash.", ex);
    }
  }

}
