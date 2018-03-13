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
  private static final String HASH_ALGO = "SHA-256";

  /**
   * encoding
   */
  private static final String ENCODING = "UTF-8";

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

    return hashPassword(password.getBytes("UTF-8") );
  }

  /**
   * Hashes the given password and Base64 encodes the result
   *
   * @param password The password to be hashed
   *
   * @return The hashed password
   */
  private String hashPassword( final byte[] password )
  {
    byte[] hashedPassword;
    String hashedPasswordBase64;

    // create password hash
    try {
      final MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
      hashedPassword = md.digest(password);
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Cannot hash the password: " + e);
    }

    // encode hashed password to utf8
    try {
      hashedPasswordBase64 = new String(Base64.getEncoder().encode(hashedPassword),ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Invalid Encoding: "+e);
    }

    return String.format("{%s}%s",HASH_ALGO.toLowerCase(),hashedPasswordBase64);
  }
}
