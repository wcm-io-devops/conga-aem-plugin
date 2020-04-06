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
package io.wcm.devops.conga.plugins.aem.export;

import static io.wcm.devops.conga.plugins.aem.export.ContentPackageNodeModelExport.MODEL_FILE;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.export.NodeModelExportPlugin;
import io.wcm.devops.conga.generator.spi.export.context.ExportNodeRoleData;
import io.wcm.devops.conga.generator.spi.export.context.GeneratedFileContext;
import io.wcm.devops.conga.generator.spi.export.context.NodeModelExportContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.model.environment.Environment;
import io.wcm.devops.conga.model.environment.Node;
import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackagePropertiesPostProcessor;

class ContentPackageNodeModelExportTest {

  private NodeModelExportPlugin underTest;
  private File nodeDir;
  private File packagesDir;
  private File unpackDir;

  @BeforeEach
  void setUp() throws Exception {
    // prepare test directories
    nodeDir = new File("target/test-" + getClass().getSimpleName());
    packagesDir = new File(nodeDir, "packages");
    unpackDir = new File(nodeDir, "unpack");
    FileUtils.deleteDirectory(nodeDir);
    nodeDir.mkdirs();
    packagesDir.mkdirs();
    unpackDir.mkdirs();

    underTest = new PluginManagerImpl().get(ContentPackageNodeModelExport.NAME, NodeModelExportPlugin.class);
  }

  @Test
  void testExport() throws IOException {

    // prepare test packages
    GeneratedFileContext noContentPackage = preparePackage("noContent1.zip", null);
    GeneratedFileContext applicationPackage = preparePackage("application1.zip", "application");
    GeneratedFileContext contentPackage = preparePackage("content1.zip", "content");

    // prepare test context
    ExportNodeRoleData role1 = new ExportNodeRoleData()
        .roleVariant(ImmutableList.of("aem-author", "aem-publish"))
        .files(ImmutableList.of(noContentPackage, applicationPackage));
    ExportNodeRoleData role2 = new ExportNodeRoleData()
        .roleVariant(ImmutableList.of("aem-author"))
        .files(ImmutableList.of(contentPackage));
    Environment environment = new Environment();
    environment.setConfig(ImmutableMap.of("contentPackage", ImmutableMap.of("group", "test-group")));
    Node node = new Node();
    node.setNode("test-node");
    NodeModelExportContext context = new NodeModelExportContext()
        .environment(environment)
        .node(node)
        .nodeDir(nodeDir)
        .roleData(ImmutableList.of(role1, role2));

    // execute export plugin
    underTest.export(context);

    // asset result
    File resultPackage = new File(nodeDir, MODEL_FILE);
    assertTrue(resultPackage.exists());

    ZipUtil.unpack(resultPackage, unpackDir);
    assertTrue(new File(unpackDir, "jcr_root/apps/test-group-packages/application/install/application1.zip").exists());
    assertTrue(new File(unpackDir, "jcr_root/apps/test-group-packages/content/install.author/content1.zip").exists());
  }

  private GeneratedFileContext preparePackage(String packageName, String packageType) throws IOException {
    File file = new File(packagesDir, packageName);
    FileUtils.copyFile(new File("src/test/resources/package/example.zip"), file);
    FileContext fileContext = new FileContext()
        .file(file);
    if (packageType != null) {
      fileContext.modelOptions(ImmutableMap.of(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY,
          ImmutableMap.of(NAME_PACKAGE_TYPE, packageType)));
    }
    return new GeneratedFileContext()
        .fileContext(fileContext);
  }

}
