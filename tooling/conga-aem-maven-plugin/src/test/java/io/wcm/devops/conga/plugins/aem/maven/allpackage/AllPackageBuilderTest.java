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

import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ContentPackageTestUtil.getXmlFromZip;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

class AllPackageBuilderTest {

  private File nodeDir;
  private File targetDir;
  private File targetUnpackDir;

  @BeforeEach
  void setUp(TestInfo testInfo) throws IOException {
    nodeDir = new File("src/test/resources/node");
    targetDir = new File("target/test-" + getClass().getSimpleName() + "_" + testInfo.getDisplayName());
    targetUnpackDir = new File(targetDir, "unpack");
    FileUtils.deleteDirectory(targetDir);
    targetDir.mkdirs();
    targetUnpackDir.mkdirs();
  }

  private static Stream<Arguments> cloudManagerTargetVariants() {
    return Stream.of(
        // test with no environment (=all environments)
        Arguments.of(ImmutableSet.of(), new String[] { "install.author" }),
        // test with two environments
        Arguments.of(ImmutableSet.of("stage", "prod"), new String[] { "install.author.stage", "install.author.prod" }));
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuild(Set<String> cloudManagerTarget, String[] expectedEnvironmentDirs) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg");
    assertTrue(builder.build(contentPackages, cloudManagerTarget));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File applicationInstallDir = new File(applicationDir, expectedEnvironmentDir);
      assertFiles(applicationInstallDir, "aem-cms-system-config.zip");
      assertDependencies(applicationInstallDir, "aem-cms-system-config.zip",
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File contentInstallDir = new File(contentDir, expectedEnvironmentDir);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents.zip", "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip");
      assertDependencies(contentInstallDir, "aem-cms-author-replicationagents.zip");
      assertDependencies(contentInstallDir, "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip");
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File containerInstallDir = new File(containerDir, expectedEnvironmentDir);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config.zip", "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip");
      assertDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config.zip");
      assertDependencies(containerInstallDir, "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip");
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuildWithAutoDependencies(Set<String> cloudManagerTarget, String[] expectedEnvironmentDirs) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependencies(true);
    assertTrue(builder.build(contentPackages, cloudManagerTarget));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File applicationInstallDir = new File(applicationDir, expectedEnvironmentDir);
      assertFiles(applicationInstallDir, "aem-cms-system-config.zip");
      assertDependencies(applicationInstallDir, "aem-cms-system-config.zip",
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6",
          "wcm-io-samples:aem-cms-author-replicationagents:1.3.1-SNAPSHOT");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File contentInstallDir = new File(contentDir, expectedEnvironmentDir);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents.zip", "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip");
      assertDependencies(contentInstallDir, "aem-cms-author-replicationagents.zip");
      assertDependencies(contentInstallDir, "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples:wcm-io-samples-complete:1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File containerInstallDir = new File(containerDir, expectedEnvironmentDir);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config.zip", "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip");
      assertDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config.zip",
          "wcm-io-samples:aem-cms-system-config:1.3.1-SNAPSHOT");
      assertDependencies(containerInstallDir, "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples:wcm-io-samples-aem-cms-config:1.3.1-SNAPSHOT");
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuildWithAutoDependenciesSeparateMutable(Set<String> cloudManagerTarget, String[] expectedEnvironmentDirs) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependencies(true)
        .autoDependenciesSeparateMutable(true);
    assertTrue(builder.build(contentPackages, cloudManagerTarget));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File applicationInstallDir = new File(applicationDir, expectedEnvironmentDir);
      assertFiles(applicationInstallDir, "aem-cms-system-config.zip");
      assertDependencies(applicationInstallDir, "aem-cms-system-config.zip",
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File contentInstallDir = new File(contentDir, expectedEnvironmentDir);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents.zip", "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip");
      assertDependencies(contentInstallDir, "aem-cms-author-replicationagents.zip");
      assertDependencies(contentInstallDir, "wcm-io-samples-sample-content-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples:aem-cms-author-replicationagents:1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, expectedEnvironmentDirs);

    for (String expectedEnvironmentDir : expectedEnvironmentDirs) {
      File containerInstallDir = new File(containerDir, expectedEnvironmentDir);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config.zip", "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip");
      assertDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config.zip",
          "wcm-io-samples:aem-cms-system-config:1.3.1-SNAPSHOT");
      assertDependencies(containerInstallDir, "wcm-io-samples-complete-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples:wcm-io-samples-aem-cms-config:1.3.1-SNAPSHOT");
    }
  }

  private void assertFiles(File dir, String... fileNames) {
    assertTrue(dir.exists(), "file exists: " + dir.getPath());
    assertTrue(dir.isDirectory(), "is directory: " + dir.getPath());
    Set<String> expectedFileNames = ImmutableSet.copyOf(fileNames);
    String[] files = dir.list();
    Set<String> actualFileNames = files != null ? ImmutableSet.copyOf(files) : ImmutableSet.of();
    assertEquals(expectedFileNames, actualFileNames, "files in " + dir.getPath());
  }

  private void assertDependencies(File dir, String fileName, String... dependencies) throws Exception {
    File zipFile = new File(dir, fileName);
    Document filterXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");

    String expecedDependencies = "";
    if (dependencies.length > 0) {
      expecedDependencies = StringUtils.join(dependencies, ",");
    }
    assertXpathEvaluatesTo(expecedDependencies, "/properties/entry[@key='dependencies']", filterXml);
  }

}
