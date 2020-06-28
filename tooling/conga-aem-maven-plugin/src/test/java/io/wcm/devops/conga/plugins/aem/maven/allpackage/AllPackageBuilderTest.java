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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.plugins.aem.maven.AutoDependenciesMode;
import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

class AllPackageBuilderTest {

  private File nodeDir;
  private File targetDir;
  private File targetUnpackDir;

  @BeforeEach
  void setUp(TestInfo testInfo) throws IOException {
    nodeDir = new File("src/test/resources/node");
    targetDir = new File("target/test-" + getClass().getSimpleName()
        + (testInfo.getTestMethod().isPresent() ? "_" + testInfo.getTestMethod().get().getName() : ":")
        + "_" + testInfo.getDisplayName());
    targetUnpackDir = new File(targetDir, "unpack");
    FileUtils.deleteDirectory(targetDir);
    targetDir.mkdirs();
    targetUnpackDir.mkdirs();
  }

  private static Stream<Arguments> cloudManagerTargetVariants() {
    return Stream.of(
        // test with no environment (=all environments)
        Arguments.of(ImmutableSet.of(), ImmutableList.of(".author")),
        // test with two environments
        Arguments.of(ImmutableSet.of("stage", "prod"), ImmutableList.of(".author.stage", ".author.prod")));
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuild(Set<String> cloudManagerTarget, List<String> runmodeSuffixes) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg");
    assertTrue(builder.build(contentPackages, cloudManagerTarget, ImmutableMap.of("prop1", "value1")));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix);
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix);
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix);
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuil_IMMUTABLE_MUTABLE_COMBINED(Set<String> cloudManagerTarget, List<String> runmodeSuffixes) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_COMBINED);
    assertTrue(builder.build(contentPackages, cloudManagerTarget, null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6",
          "wcm-io-samples:aem-cms-author-replicationagents" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-complete" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix,
          "wcm-io-samples:aem-cms-system-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-aem-cms-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuild_IMMUTABLE_MUTABLE_SEPARATE(Set<String> cloudManagerTarget, List<String> runmodeSuffixes) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_SEPARATE);
    assertTrue(builder.build(contentPackages, cloudManagerTarget, null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix,
          "wcm-io-samples:aem-cms-author-replicationagents" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix,
          "wcm-io-samples:aem-cms-system-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-aem-cms-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuild_IMMUTABLE_ONLY(Set<String> cloudManagerTarget, List<String> runmodeSuffixes) throws Exception {
    List<ContentPackageFile> contentPackages = new ModelParser().getContentPackagesForNode(nodeDir);
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_ONLY);
    assertTrue(builder.build(contentPackages, cloudManagerTarget, null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertFiles(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertFiles(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6");
    }

    File contentDir = new File(appsDir, "content");
    assertFiles(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix);
    }

    File containerDir = new File(appsDir, "container");
    assertFiles(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix,
          "wcm-io-samples:aem-cms-system-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-aem-cms-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
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

  private void assertNameDependencies(File dir, String fileName, String packageName,
      String... dependencies) throws Exception {
    File zipFile = new File(dir, fileName);
    Document filterXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");

    assertXpathEvaluatesTo(packageName, "/properties/entry[@key='name']", filterXml);

    String expecedDependencies = "";
    if (dependencies.length > 0) {
      expecedDependencies = StringUtils.join(dependencies, ",");
    }
    assertXpathEvaluatesTo(expecedDependencies, "/properties/entry[@key='dependencies']", filterXml);
  }

  private String[] toInstallFolderNames(String baseName, List<String> runmodeSuffixes) {
    return runmodeSuffixes.stream()
        .map(suffix -> baseName + suffix)
        .toArray(size -> new String[size]);
  }

}
