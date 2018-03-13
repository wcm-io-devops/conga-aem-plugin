package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import org.apache.jackrabbit.oak.spi.security.user.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import static io.wcm.devops.conga.plugins.aem.handlebars.helper.TestUtils.executeHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WebConsolePasswordHashHelperTest {

  private static final String PASSWORD_PLAIN = "password";
  private static final String PASSWORD_HASH = "{sha-256}XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=";

  private HelperPlugin<Object> helper;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    helper = new PluginManagerImpl().get(WebConsolePasswordHashHelper.NAME, HelperPlugin.class);
  }

  @Test
  public void testNull() throws Exception {
    Object passwordHash = executeHelper(helper, null, new MockOptions());
    assertNull(passwordHash);
  }

  @Test
  public void testHash() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_PLAIN, new MockOptions());
    assertTrue(passwordHash instanceof String);
    assertEquals(PASSWORD_HASH, passwordHash);
  }

  @Test
  public void testAlreadyHashed() throws Exception {
    Object passwordHash = executeHelper(helper, PASSWORD_HASH, new MockOptions());
    assertEquals(PASSWORD_HASH, passwordHash);
  }
}
