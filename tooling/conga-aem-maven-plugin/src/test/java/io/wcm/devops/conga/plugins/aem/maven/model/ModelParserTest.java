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
package io.wcm.devops.conga.plugins.aem.maven.model;

import static io.wcm.devops.conga.generator.util.FileUtil.getCanonicalPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModelParserTest {

  private File nodeDir;
  private File dispatcherNodeDir;
  private ModelParser nodeModelParser;
  private ModelParser dispatcherNodeModelParser;

  @BeforeEach
  void setUp() {
    nodeDir = new File("src/test/resources/node/aem-author");
    nodeModelParser = new ModelParser(nodeDir);
    dispatcherNodeDir = new File("src/test/resources/node-dispatcher");
    dispatcherNodeModelParser = new ModelParser(dispatcherNodeDir);
  }

  @Test
  void testGetContentPackagesForNode() {
    List<InstallableFile> files = nodeModelParser.getInstallableFilesForNode();

    assertEquals(13, files.size());

    assertBundle(files.get(0), "bundles/io.wcm.caconfig.editor-1.11.0.jar");
    assertPackage(files.get(1), "packages/acs-aem-commons-ui.apps-4.10.0-min.zip", "application", null);
    assertPackage(files.get(2), "packages/acs-aem-commons-ui.content-4.10.0-min.zip", "content", null);
    assertPackage(files.get(3), "packages/accesscontroltool-package-3.0.0-cloud.zip", "container", null);
    assertPackage(files.get(4), "packages/accesscontroltool-oakindex-package-3.0.0-cloud.zip", "application", null);
    assertPackage(files.get(5), "packages/core.wcm.components.all-2.17.0.zip", "container", null);
    assertPackage(files.get(6), "packages/aem-cms-author-replicationagents.zip", "content", true);
    assertPackage(files.get(7), "packages/aem-cms-system-config.zip", "application", true);
    assertPackage(files.get(8), "packages/wcm-io-samples-aem-cms-config.zip", "container", true);
    assertPackage(files.get(9), "packages/wcm-io-samples-aem-cms-author-systemusers.zip", null, true);
    assertPackage(files.get(10), "packages/wcm-io-samples-complete-1.3.1-SNAPSHOT.zip", "container", null);
    assertPackage(files.get(11), "packages/wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip", "content", null);
    assertBundle(files.get(12), "bundles/io.wcm.wcm.ui.granite-1.9.2.jar");

    ModelContentPackageFile pkg6 = (ModelContentPackageFile)files.get(6);
    assertEquals(Set.of("aem-author"), pkg6.getVariants());
    assertEquals(false, pkg6.getInstall());
    assertEquals(true, pkg6.getRecursive());
    assertEquals(30, pkg6.getDelayAfterInstallSec());
    assertEquals(60, pkg6.getHttpSocketTimeoutSec());
    assertEquals("wcm-io-samples", pkg6.getGroup());
    assertEquals("aem-cms-author-replicationagents", pkg6.getName());
    assertEquals("1.3.1-SNAPSHOT", pkg6.getVersion());

    BundleFile bundle12 = (BundleFile)files.get(12);
    assertEquals(Set.of("aem-author"), bundle12.getVariants());
    assertEquals(false, bundle12.getInstall());
  }

  private void assertPackage(InstallableFile file, String path, String packageType, Boolean force) {
    assertTrue(file instanceof ModelContentPackageFile);
    ModelContentPackageFile pkg = (ModelContentPackageFile)file;
    String actualPath = StringUtils.substringAfter(getPath(pkg.getFile()), getPath(nodeDir) + "/");
    assertEquals(path, actualPath, "package path");
    assertEquals(packageType, pkg.getPackageType(), packageType);
    assertEquals(force, pkg.getForce());
  }

  private void assertBundle(InstallableFile file, String path) {
    assertTrue(file instanceof BundleFile);
    String actualPath = StringUtils.substringAfter(getPath(file.getFile()), getPath(nodeDir) + "/");
    assertEquals(path, actualPath, "package path");
  }

  private String getPath(File file) {
    return Strings.CS.replace(getCanonicalPath(file), "\\", "/");
  }

  @Test
  void testHasRole() {
    assertTrue(dispatcherNodeModelParser.hasRole("aem-dispatcher-cloud"));
    assertFalse(nodeModelParser.hasRole("aem-dispatcher-cloud"));
  }

  @Test
  void testGetCloudManagerTarget() {
    assertEquals(Set.of("stage", "prod"), nodeModelParser.getCloudManagerTarget());
    assertTrue(dispatcherNodeModelParser.getCloudManagerTarget().isEmpty());
  }

}
