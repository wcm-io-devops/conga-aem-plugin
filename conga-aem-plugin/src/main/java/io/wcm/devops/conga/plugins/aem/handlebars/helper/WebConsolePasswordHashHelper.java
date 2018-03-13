package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import com.github.jknack.handlebars.Options;
import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;
import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
   * @throws IOException
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
