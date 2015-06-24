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

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_GROUP;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.PluginManager;
import io.wcm.devops.conga.plugins.sling.postprocessor.ProvisioningOsgiConfigPostProcessor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableMap;

public class ContentPackagePostProcessorTest {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
  }

  private PostProcessorPlugin underTest;

  @Before
  public void setUp() {
    underTest = new PluginManager().get(ContentPackagePostProcessor.NAME, PostProcessorPlugin.class);
  }

  @Test
  public void testPostProcess() throws Exception {
    Map<String, Object> options = ImmutableMap.<String, Object>of(
        PROPERTY_PACKAGE_GROUP, "myGroup",
        PROPERTY_PACKAGE_NAME, "myName",
        PROPERTY_PACKAGE_ROOT_PATH, "/apps/test/config");

    // prepare JSON file
    File target = new File("target/" + ContentPackagePostProcessor.NAME + "-test");
    if (target.exists()) {
      FileUtils.deleteDirectory(target);
    }
    File contentPackageFile = new File(target, "test.json");
    FileUtils.copyFile(new File(getClass().getResource("/json/content.json").toURI()), contentPackageFile);

    // post-process
    FileContext fileContext = new FileContext()
    .file(contentPackageFile)
    .charset(CharEncoding.UTF_8);
    PostProcessorContext context = new PostProcessorContext()
    .options(options)
    .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);

    // validate
    assertFalse(contentPackageFile.exists());

    File zipFile = new File(target, "test.zip");
    assertTrue(zipFile.exists());

    // TODO: validate content package content
  }

  private byte[] getDataFromZip(File file, String path) throws Exception {
    byte[] data = ZipUtil.unpackEntry(file, path);
    if (data == null) {
      throw new FileNotFoundException("File not found in ZIP: " + path);
    }
    return data;
  }

  private Document getXmlFromZip(File file, String path) throws Exception {
    byte[] data = getDataFromZip(file, path);
    DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
    return documentBuilder.parse(new ByteArrayInputStream(data));
  }

}
