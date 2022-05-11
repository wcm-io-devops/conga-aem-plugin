/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.devops.conga.plugins.aem.postprocessor;

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_DESCRIPTION;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_GROUP;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_VERSION;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.assertXpathEvaluatesTo;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.getDataFromZip;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.getXmlFromZip;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.felix.cm.file.ConfigurationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.plugins.sling.postprocessor.ProvisioningOsgiConfigPostProcessor;

class ContentPackageOsgiConfigPostProcessorTest {

  private PostProcessorPlugin underTest;

  private static final Map<String, Object> PACKAGE_OPTIONS = ImmutableMap.<String, Object>of(
      PROPERTY_PACKAGE_GROUP, "myGroup",
      PROPERTY_PACKAGE_NAME, "myName",
      PROPERTY_PACKAGE_DESCRIPTION, "myDesc",
      PROPERTY_PACKAGE_VERSION, "1.5",
      PROPERTY_PACKAGE_ROOT_PATH, "/apps/test/config");

  @BeforeEach
  void setUp() {
    underTest = new PluginManagerImpl().get(ContentPackageOsgiConfigPostProcessor.NAME, PostProcessorPlugin.class);
  }

  @Test
  void testPostProcess() throws Exception {
    // prepare provisioning file
    File target = new File("target/" + ContentPackageOsgiConfigPostProcessor.NAME + "-test");
    if (target.exists()) {
      FileUtils.deleteDirectory(target);
    }
    File contentPackageFile = new File(target, "test.txt");
    FileUtils.copyFile(new File(getClass().getResource("/provisioning/provisioning.txt").toURI()), contentPackageFile);

    // post-process
    FileContext fileContext = new FileContext()
        .file(contentPackageFile)
        .charset(StandardCharsets.UTF_8);
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(new PluginManagerImpl())
        .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));
    PostProcessorContext context = new PostProcessorContext()
        .pluginContextOptions(pluginContextOptions)
        .options(PACKAGE_OPTIONS);

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);

    // validate
    assertFalse(contentPackageFile.exists());

    File zipFile = new File(target, "test.zip");
    assertTrue(zipFile.exists());

    try (InputStream is = new ByteArrayInputStream(getDataFromZip(zipFile, "jcr_root/apps/test/config/my.pid.config"))) {

      // check for initial comment line
      is.mark(256);
      final int firstChar = is.read();
      if (firstChar == '#') {
        int b;
        while ((b = is.read()) != '\n') {
          if (b == -1) {
            throw new IOException("Unable to read configuration.");
          }
        }
      }
      else {
        is.reset();
      }

      Dictionary<?, ?> config = ConfigurationHandler.read(is);
      assertEquals("value1", config.get("stringProperty"));
      assertArrayEquals(new String[] {
          "v1", "v2", "v3"
      }, (String[])config.get("stringArrayProperty"));
      assertEquals(true, config.get("booleanProperty"));
      assertEquals(999999999999L, config.get("longProperty"));
    }

    assertTrue(ZipUtil.containsEntry(zipFile, "jcr_root/apps/test/config/my.factory-my.pid.config"));
    assertTrue(ZipUtil.containsEntry(zipFile, "jcr_root/apps/test/config.mode1/my.factory-my.pid2.config"));
    assertTrue(ZipUtil.containsEntry(zipFile, "jcr_root/apps/test/config.mode2/my.pid2.config"));

    Document filterXml = getXmlFromZip(zipFile, "META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("/apps/test/config", "/workspaceFilter/filter[1]/@root", filterXml);

    Document propsXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");
    assertXpathEvaluatesTo("myGroup", "/properties/entry[@key='group']", propsXml);
    assertXpathEvaluatesTo("myName", "/properties/entry[@key='name']", propsXml);
    assertXpathEvaluatesTo("myDesc\n---\nSample comment in provisioning.txt", "/properties/entry[@key='description']", propsXml);
    assertXpathEvaluatesTo("1.5", "/properties/entry[@key='version']", propsXml);
    assertXpathEvaluatesTo("container", "/properties/entry[@key='packageType']", propsXml);

    assertFalse(ZipUtil.containsEntry(zipFile, "jcr_root/apps/test/config/.content.xml"));
  }

  @Test
  void testPostProcess_EmptyProvisioning() throws Exception {
    // prepare provisioning file
    File target = new File("target/" + ContentPackageOsgiConfigPostProcessor.NAME + "-test-empty");
    if (target.exists()) {
      FileUtils.deleteDirectory(target);
    }
    File contentPackageFile = new File(target, "test.txt");
    FileUtils.copyFile(new File(getClass().getResource("/provisioning/provisioning_empty.txt").toURI()), contentPackageFile);

    // post-process
    FileContext fileContext = new FileContext()
        .file(contentPackageFile)
        .charset(StandardCharsets.UTF_8);
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(new PluginManagerImpl())
        .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));
    PostProcessorContext context = new PostProcessorContext()
        .pluginContextOptions(pluginContextOptions)
        .options(PACKAGE_OPTIONS);

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);

    // validate
    assertFalse(contentPackageFile.exists());

    File zipFile = new File(target, "test.zip");
    assertTrue(zipFile.exists());

    Document propsXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");
    assertXpathEvaluatesTo("application", "/properties/entry[@key='packageType']", propsXml);

    assertTrue(ZipUtil.containsEntry(zipFile, "jcr_root/apps/test/config/.content.xml"));
  }

}
