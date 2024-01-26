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

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_AC_HANDLING;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_FILES;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_FILTERS;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_GROUP;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_PACKAGE_TYPE;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_PROPERTIES;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_THUMBNAIL_IMAGE;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.assertXpathEvaluatesTo;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.getDataFromZip;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageTestUtil.getXmlFromZip;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.wcm.devops.conga.generator.UrlFileManager;
import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.spi.context.UrlFilePluginContext;
import io.wcm.devops.conga.generator.util.PluginManager;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.plugins.sling.postprocessor.ProvisioningOsgiConfigPostProcessor;

class ContentPackagePostProcessorTest {

  private PostProcessorPlugin underTest;

  @BeforeEach
  void setUp() {
    underTest = new PluginManagerImpl().get(ContentPackagePostProcessor.NAME, PostProcessorPlugin.class);
  }

  @Test
  void testPostProcess() throws Exception {
    Map<String, Object> options = new HashMap<>();
    options.put(PROPERTY_PACKAGE_GROUP, "myGroup");
    options.put(PROPERTY_PACKAGE_NAME, "myName");
    options.put(PROPERTY_PACKAGE_ROOT_PATH, "/content/test");
    options.put(PROPERTY_PACKAGE_AC_HANDLING, "ignore");
    options.put(PROPERTY_PACKAGE_PACKAGE_TYPE, "content");
    options.put(PROPERTY_PACKAGE_THUMBNAIL_IMAGE, "classpath:/package/thumbnail.png");
    options.put(PROPERTY_PACKAGE_FILTERS, List.of(
        Map.of("filter", "/content/test/1"),
        Map.of("filter", "/content/test/2",
            "rules", List.of(Map.of("rule", "include", "pattern", "pattern1"),
                Map.of("rule", "exclude", "pattern", "pattern2")))));
    options.put(PROPERTY_PACKAGE_PROPERTIES, Map.of(
        "prop1", "value1",
        "my.custom.prop2", 123));
    options.put(PROPERTY_PACKAGE_FILES, List.of(
        Map.of("url", "classpath:/package/thumbnail.png", "path", "/content/image.png"),
        Map.of("file", "README.txt", "dir", "readme", "path", "/content/README.txt", "delete", true),
        Map.of("fileMatch", "file_(.*).txt", "dir", "files", "path", "/content/files/$1.txt", "delete", true)));

    // prepare JSON file
    File target = new File("target/" + ContentPackagePostProcessor.NAME + "-test");
    if (target.exists()) {
      FileUtils.deleteDirectory(target);
    }
    File contentPackageFile = new File(target, "test.json");
    FileUtils.copyFile(new File(getClass().getResource("/json/content.json").toURI()), contentPackageFile);

    // prepare additional binary files
    File readmeFolder = new File(target, "readme");
    readmeFolder.mkdir();
    File readmeFile = new File(readmeFolder, "README.txt");
    FileUtils.write(readmeFile, "readme", StandardCharsets.UTF_8);

    File filesFolder = new File(target, "files");
    filesFolder.mkdir();
    File file_1 = new File(filesFolder, "file_1.txt");
    FileUtils.write(file_1, "file_1", StandardCharsets.UTF_8);
    File file_2 = new File(filesFolder, "file_2.txt");
    FileUtils.write(file_2, "file_2", StandardCharsets.UTF_8);
    File file_3 = new File(filesFolder, "file_3.txt");
    FileUtils.write(file_3, "file_3", StandardCharsets.UTF_8);

    // post-process
    FileContext fileContext = new FileContext()
        .file(contentPackageFile)
        .charset(StandardCharsets.UTF_8)
        .targetDir(target);
    PluginManager pluginManager = new PluginManagerImpl();
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(pluginManager)
        .urlFileManager(new UrlFileManager(pluginManager, new UrlFilePluginContext()))
        .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));
    PostProcessorContext context = new PostProcessorContext()
        .pluginContextOptions(pluginContextOptions)
        .options(options);

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);

    // validate
    assertFalse(contentPackageFile.exists());

    File zipFile = new File(target, "test.zip");
    assertTrue(zipFile.exists());

    Document contentXml = getXmlFromZip(zipFile, "jcr_root/content/test/.content.xml");
    assertXpathEvaluatesTo("cq:Page", "/jcr:root/@jcr:primaryType", contentXml);

    Document filterXml = getXmlFromZip(zipFile, "META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("2", "count(/workspaceFilter/filter)", filterXml);
    assertXpathEvaluatesTo("/content/test/1", "/workspaceFilter/filter[1]/@root", filterXml);
    assertXpathEvaluatesTo("/content/test/2", "/workspaceFilter/filter[2]/@root", filterXml);
    assertXpathEvaluatesTo("pattern1", "/workspaceFilter/filter[2]/include[1]/@pattern", filterXml);
    assertXpathEvaluatesTo("pattern2", "/workspaceFilter/filter[2]/exclude[1]/@pattern", filterXml);

    Document propertiesXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");
    assertXpathEvaluatesTo("ignore", "/properties/entry[@key='acHandling']", propertiesXml);
    assertXpathEvaluatesTo("content", "/properties/entry[@key='packageType']", propertiesXml);
    assertXpathEvaluatesTo("Sample comment in content.json", "/properties/entry[@key='description']", propertiesXml);
    assertXpathEvaluatesTo("value1", "/properties/entry[@key='prop1']", propertiesXml);
    assertXpathEvaluatesTo("123", "/properties/entry[@key='my.custom.prop2']", propertiesXml);

    byte[] thumbnailImage = getDataFromZip(zipFile, "META-INF/vault/definition/thumbnail.png");
    assertNotNull(thumbnailImage);

    byte[] image = getDataFromZip(zipFile, "jcr_root/content/image.png");
    assertNotNull(image);

    byte[] readmeText = getDataFromZip(zipFile, "jcr_root/content/README.txt");
    assertNotNull(readmeText);
    assertFalse(readmeFile.exists());

    byte[] file_1Text = getDataFromZip(zipFile, "jcr_root/content/files/1.txt");
    assertNotNull(file_1Text);
    assertFalse(file_1.exists());

    byte[] file_2Text = getDataFromZip(zipFile, "jcr_root/content/files/2.txt");
    assertNotNull(file_2Text);
    assertFalse(file_2.exists());

    byte[] file_3Text = getDataFromZip(zipFile, "jcr_root/content/files/3.txt");
    assertNotNull(file_3Text);
    assertFalse(file_3.exists());
  }

}
