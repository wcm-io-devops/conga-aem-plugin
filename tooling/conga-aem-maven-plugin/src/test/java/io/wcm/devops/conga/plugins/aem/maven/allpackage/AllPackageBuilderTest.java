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
package io.wcm.devops.conga.plugins.aem.maven.allpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

class AllPackageBuilderTest {

  private File nodeDir;
  private File targetDir;
  private File targetUnpackDir;

  @BeforeEach
  void setUp() throws IOException {
    nodeDir = new File("src/test/resources/node");
    targetDir = new File("target/test-" + getClass().getSimpleName());
    targetUnpackDir = new File(targetDir, "unpack");
    FileUtils.deleteDirectory(targetDir);
    targetDir.mkdirs();
    targetUnpackDir.mkdirs();
  }

  @Test
  void testBuild() throws IOException {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");
    assertTrue(AllPackageBuilder.build(targetFile, contentPackages, "test-group", "test-pkg"));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, "install.author");

    File applicationInstallDir = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDir, "aem-cms-system-config.zip");

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, "install.author");

    File contentInstallDir = new File(contentDir, "install.author");
    assertFiles(contentInstallDir, "aem-cms-author-replicationagents.zip", "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip");

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, "install.author");

    File containerInstallDir = new File(containerDir, "install.author");
    assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config.zip", "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip");
  }

  private void assertFiles(File dir, String... fileNames) {
    assertTrue(dir.exists(), "file exists: " + dir.getPath());
    assertTrue(dir.isDirectory(), "is directory: " + dir.getPath());
    Set<String> expectedFileNames = ImmutableSet.copyOf(fileNames);
    String[] files = dir.list();
    Set<String> actualFileNames = files != null ? ImmutableSet.copyOf(files) : ImmutableSet.of();
    assertEquals(expectedFileNames, actualFileNames, "files in " + dir.getPath());
  }

}
