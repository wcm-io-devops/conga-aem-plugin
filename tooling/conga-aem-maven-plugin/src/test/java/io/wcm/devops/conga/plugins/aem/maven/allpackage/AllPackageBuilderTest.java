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

import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ExpectedContentPackage.contentPackage;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ExpectedDependency.dep;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ExpectedFile.file;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.assertDirectories;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.assertFiles;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.assertNameDependencies;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.plugins.aem.maven.AutoDependenciesMode;
import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
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
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg");
    builder.add(files, cloudManagerTarget);
    assertTrue(builder.build(ImmutableMap.of("prop1", "value1")));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);

      assertFiles(applicationInstallDir, runmodeSuffix,
          contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0"),
          contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0"),
          contentPackage("core.wcm.components.content{runmode}", "2.17.0",
              dep("day/cq60/product:cq-platform-content:1.3.248")),
          contentPackage("core.wcm.components.extensions.amp.content{runmode}", "2.17.0"),
          contentPackage("acs-aem-commons-ui.apps{runmode}", "4.10.0",
              dep("day/cq60/product:cq-content:6.3.64")),
          contentPackage("aem-cms-system-config{runmode}", null,
              dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
              dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6")),
          file("io.wcm.caconfig.editor-1.11.0.jar"),
          file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    }

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.content" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix);
    }

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-package" + runmodeSuffix);
      assertNameDependencies(containerInstallDir, "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.all" + runmodeSuffix);
      assertNameDependencies(containerInstallDir, "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix);
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix);
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix);
    }
  }

  @ParameterizedTest
  @MethodSource("cloudManagerTargetVariants")
  void testBuild_IMMUTABLE_MUTABLE_COMBINED(Set<String> cloudManagerTarget, List<String> runmodeSuffixes) throws Exception {
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_COMBINED);
    builder.add(files, cloudManagerTarget);
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-system-config" + runmodeSuffix + ".zip",
          "io.wcm.caconfig.editor-1.11.0.jar",
          "io.wcm.wcm.ui.granite-1.9.2.jar");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-apps-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.content" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix,
          "Netcentric:accesscontroltool-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.content" + runmodeSuffix,
          "day/cq60/product:cq-platform-content:1.3.248,Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix,
          "day/cq60/product:cq-content:6.3.64");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6",
          "wcm-io-samples:aem-cms-author-replicationagents" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.content" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.apps" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix,
          "adobe/cq60:core.wcm.components.all" + runmodeSuffix + ":2.17.0");
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-complete" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.content" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.all" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
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
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_SEPARATE);
    builder.add(files, cloudManagerTarget);
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-system-config" + runmodeSuffix + ".zip",
          "io.wcm.caconfig.editor-1.11.0.jar",
          "io.wcm.wcm.ui.granite-1.9.2.jar");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-apps-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.apps" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix,
          "Netcentric:accesscontroltool-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.content" + runmodeSuffix,
          "day/cq60/product:cq-platform-content:1.3.248,Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix,
          "day/cq60/product:cq-content:6.3.64");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6",
          "adobe/cq60:core.wcm.components.all" + runmodeSuffix + ":2.17.0");
    }

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.content" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.content" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix,
          "wcm-io-samples:aem-cms-author-replicationagents" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.apps" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.all" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
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
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_ONLY);
    builder.add(files, cloudManagerTarget);
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File applicationInstallDir = new File(applicationDir, "install" + runmodeSuffix);
      assertFiles(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-system-config" + runmodeSuffix + ".zip",
          "io.wcm.caconfig.editor-1.11.0.jar",
          "io.wcm.wcm.ui.granite-1.9.2.jar");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-apps-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-apps-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.apps" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(applicationInstallDir, "accesscontroltool-oakindex-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-oakindex-package" + runmodeSuffix,
          "Netcentric:accesscontroltool-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.content" + runmodeSuffix,
          "day/cq60/product:cq-platform-content:1.3.248,Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "core.wcm.components.extensions.amp.content" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.extensions.amp.content" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(applicationInstallDir, "acs-aem-commons-ui.apps" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.apps" + runmodeSuffix,
          "day/cq60/product:cq-content:6.3.64");
      assertNameDependencies(applicationInstallDir, "aem-cms-system-config" + runmodeSuffix + ".zip",
          "aem-cms-system-config" + runmodeSuffix,
          "day/cq60/product:cq-ui-wcm-editor-content:1.1.224",
          "adobe/cq/product:cq-remotedam-client-ui-components:1.1.6",
          "adobe/cq60:core.wcm.components.all" + runmodeSuffix + ":2.17.0");
    }

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File contentInstallDir = new File(contentDir, "install" + runmodeSuffix);
      assertFiles(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(contentInstallDir, "acs-aem-commons-ui.content" + runmodeSuffix + "-4.10.0.zip",
          "acs-aem-commons-ui.content" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "aem-cms-author-replicationagents" + runmodeSuffix + ".zip",
          "aem-cms-author-replicationagents" + runmodeSuffix);
      assertNameDependencies(contentInstallDir, "wcm-io-samples-sample-content" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-sample-content" + runmodeSuffix);
    }

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, toInstallFolderNames("install", runmodeSuffixes));

    for (String runmodeSuffix : runmodeSuffixes) {
      File containerInstallDir = new File(containerDir, "install" + runmodeSuffix);
      assertFiles(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip");
      assertNameDependencies(containerInstallDir, "accesscontroltool-package" + runmodeSuffix + "-3.0.0.zip",
          "accesscontroltool-package" + runmodeSuffix,
          "adobe/consulting:acs-aem-commons-ui.apps" + runmodeSuffix + ":4.10.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.all" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.all" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(containerInstallDir, "core.wcm.components.config" + runmodeSuffix + "-2.17.0.zip",
          "core.wcm.components.config" + runmodeSuffix,
          "Netcentric:accesscontroltool-oakindex-package" + runmodeSuffix + ":3.0.0");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-aem-cms-config" + runmodeSuffix + ".zip",
          "wcm-io-samples-aem-cms-config" + runmodeSuffix,
          "wcm-io-samples:aem-cms-system-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
      assertNameDependencies(containerInstallDir, "wcm-io-samples-complete" + runmodeSuffix + "-1.3.1-SNAPSHOT.zip",
          "wcm-io-samples-complete" + runmodeSuffix,
          "wcm-io-samples:wcm-io-samples-aem-cms-config" + runmodeSuffix + ":1.3.1-SNAPSHOT");
    }
  }

  private String[] toInstallFolderNames(String baseName, List<String> runmodeSuffixes) {
    return runmodeSuffixes.stream()
        .map(suffix -> baseName + suffix)
        .toArray(size -> new String[size]);
  }

}
