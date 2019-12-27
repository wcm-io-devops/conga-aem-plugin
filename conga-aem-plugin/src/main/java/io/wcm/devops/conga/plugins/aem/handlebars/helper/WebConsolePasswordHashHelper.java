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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;

/**
 * Handlebars helper that builds a password hash for a given password string for the Apache Felix Webconsole (felix.webconsole.password).
 */
public class WebConsolePasswordHashHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "webconsolePasswordHash";

  /**
   * hash algorithm
   */
  private static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

  /**
   * encoding
   */
  private static final String DEFAULT_ENCODING = "UTF-8";

  /**
   * Key for setting algorithm from external
   */
  public static final String HASH_OPTION_ALGORITHM = "digest";

  /**
   * Key for setting encoding from external
   */
  public static final String HASH_OPTION_ENCODING = "encoding";


  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    if (context == null) {
      return null;
    }

    String password = context.toString();

    // if password is already encrypted skip further processing
    if (!PasswordUtil.isPlainTextPassword(password)) {
      return password;
    }

    String encoding = options.hash(HASH_OPTION_ENCODING, DEFAULT_ENCODING);
    String algorithm = options.hash(HASH_OPTION_ALGORITHM, DEFAULT_HASH_ALGORITHM);

    return hashPassword(password, algorithm, encoding);
  }

  /**
   * Hashes a password for the Apache Felix Webconsole
   *
   * @param password The password to hash
   * @param hashAlgorithm The hash algorithm to use
   * @param encoding The encoding to use
   * @return The hashed password (hashed + encoded as Base64)
   */
  private String hashPassword(String password, final String hashAlgorithm, final String encoding) throws IOException {
    byte[] bytePassword;
    byte[] hashedPassword;
    String hashedPasswordBase64;

    try {
      bytePassword = password.getBytes(encoding);
    }
    catch (UnsupportedEncodingException e) {
      throw new IOException("Cannot hash the password: " + e);
    }

    // create password hash
    try {
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      hashedPassword = md.digest(bytePassword);
    }
    catch (NoSuchAlgorithmException e) {
      throw new IOException("Cannot hash the password: " + e);
    }

    // encode hashed password to utf8
    try {
      hashedPasswordBase64 = new String(Base64.getEncoder().encode(hashedPassword), encoding);
    }
    catch (UnsupportedEncodingException e) {
      throw new IOException("Invalid Encoding: " + e);
    }

    return String.format("{%s}%s", hashAlgorithm.toLowerCase(), hashedPasswordBase64);
  }

}
