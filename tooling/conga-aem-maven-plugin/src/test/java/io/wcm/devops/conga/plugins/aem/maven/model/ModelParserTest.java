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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

class ModelParserTest {

  private ModelParser underTest;
  private File nodeDir;
  private File dispatcherNodeDir;

  @BeforeEach
  void setUp() {
    underTest = new ModelParser();
    nodeDir = new File("src/test/resources/node");
    dispatcherNodeDir = new File("src/test/resources/node-dispatcher");
  }

  @Test
  void testGetContentPackagesForNode() {
    List<ModelContentPackageFile> contentPackages = underTest.getContentPackagesForNode(nodeDir);

    assertEquals(10, contentPackages.size());

    assertPackage(contentPackages.get(0), "packages/acs-aem-commons-ui.apps-4.10.0-min.zip", "application", null);
    assertPackage(contentPackages.get(1), "packages/acs-aem-commons-ui.content-4.10.0-min.zip", "content", null);
    assertPackage(contentPackages.get(2), "packages/accesscontroltool-package-3.0.0-cloud.zip", "container", null);
    assertPackage(contentPackages.get(3), "packages/accesscontroltool-oakindex-package-3.0.0-cloud.zip", "application", null);
    assertPackage(contentPackages.get(4), "packages/aem-cms-author-replicationagents.zip", "content", true);
    assertPackage(contentPackages.get(5), "packages/aem-cms-system-config.zip", "application", true);
    assertPackage(contentPackages.get(6), "packages/wcm-io-samples-aem-cms-config.zip", "container", true);
    assertPackage(contentPackages.get(7), "packages/wcm-io-samples-aem-cms-author-systemusers.zip", null, true);
    assertPackage(contentPackages.get(8), "packages/wcm-io-samples-complete-1.3.1-SNAPSHOT.zip", "container", null);
    assertPackage(contentPackages.get(9), "packages/wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip", "content", null);

    ModelContentPackageFile pkg1 = contentPackages.get(4);
    assertEquals(ImmutableList.of("aem-author"), pkg1.getVariants());
    assertEquals(false, pkg1.getInstall());
    assertEquals(true, pkg1.getRecursive());
    assertEquals(30, pkg1.getDelayAfterInstallSec());
    assertEquals(60, pkg1.getHttpSocketTimeoutSec());
    assertEquals("wcm-io-samples", pkg1.getGroup());
    assertEquals("aem-cms-author-replicationagents", pkg1.getName());
    assertEquals("1.3.1-SNAPSHOT", pkg1.getVersion());
  }

  private void assertPackage(ModelContentPackageFile pkg, String path, String packageType, Boolean force) {
    String actualPath = StringUtils.substringAfter(getPath(pkg.getFile()), getPath(nodeDir) + "/");
    assertEquals(path, actualPath, "package path");
    assertEquals(packageType, pkg.getPackageType(), packageType);
    assertEquals(force, pkg.getForce());
  }

  private String getPath(File file) {
    return StringUtils.replace(getCanonicalPath(file), "\\", "/");
  }

  @Test
  void testHasRole() {
    assertTrue(underTest.hasRole(dispatcherNodeDir, "aem-dispatcher-cloud"));
    assertFalse(underTest.hasRole(nodeDir, "aem-dispatcher-cloud"));
  }

  @Test
  void testGetCloudManagerTarget() {
    assertEquals(ImmutableSet.of("stage", "prod"), underTest.getCloudManagerTarget(nodeDir));
    assertTrue(underTest.getCloudManagerTarget(dispatcherNodeDir).isEmpty());
  }

}
