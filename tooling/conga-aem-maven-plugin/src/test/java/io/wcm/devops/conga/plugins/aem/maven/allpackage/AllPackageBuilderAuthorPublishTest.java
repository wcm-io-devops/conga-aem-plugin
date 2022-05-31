/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.assertDirectories;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.assertFiles;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.contentPackage;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.dep;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.FileTestUtil.file;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.zeroturnaround.zip.ZipUtil;

import io.wcm.devops.conga.plugins.aem.maven.AutoDependenciesMode;
import io.wcm.devops.conga.plugins.aem.maven.RunModeOptimization;
import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

class AllPackageBuilderAuthorPublishTest {

  private File nodeDirAuthor;
  private File nodeDirPublish;
  private File targetDir;
  private File targetUnpackDir;

  @BeforeEach
  void setUp(TestInfo testInfo) throws IOException {
    nodeDirAuthor = new File("src/test/resources/node/aem-author");
    nodeDirPublish = new File("src/test/resources/node/aem-publish");
    targetDir = new File("target/test-" + getClass().getSimpleName() + "_" + testInfo.getDisplayName());
    targetUnpackDir = new File(targetDir, "unpack");
    FileUtils.deleteDirectory(targetDir);
    targetDir.mkdirs();
    targetUnpackDir.mkdirs();
  }

  @Test
  void testBuild_AUTODEPENDENCIES_OFF_RUNMODEOPTIMIZATION_OFF() throws Exception {
    List<InstallableFile> filesAuthor = new ModelParser(nodeDirAuthor).getInstallableFilesForNode();
    List<InstallableFile> filesPublish = new ModelParser(nodeDirPublish).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg");
    builder.add(filesAuthor, Collections.emptySet());
    builder.add(filesPublish, Collections.emptySet());
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, "install.author", "install.publish");

    File applicationInstallDirAuthor = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0"),
        contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0"),
        contentPackage("core.wcm.components.content{runmode}", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248")),
        contentPackage("core.wcm.components.extensions.amp.content{runmode}", "2.17.0"),
        contentPackage("acs-aem-commons-ui.apps{runmode}", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config{runmode}",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6")),
        file("io.wcm.caconfig.editor-1.11.0.jar"),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    File applicationInstallDirPublish = new File(applicationDir, "install.publish");
    assertFiles(applicationInstallDirPublish, ".publish",
        contentPackage("core.wcm.components.content{runmode}", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248")),
        contentPackage("core.wcm.components.extensions.amp.content{runmode}", "2.17.0"),
        contentPackage("acs-aem-commons-ui.apps{runmode}", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config{runmode}",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6")),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, "install.author", "install.publish");

    File contentInstallDirAuthor = new File(contentDir, "install.author");
    assertFiles(contentInstallDirAuthor, ".author",
        contentPackage("acs-aem-commons-ui.content{runmode}", "4.10.0"),
        contentPackage("aem-cms-author-replicationagents{runmode}"),
        contentPackage("wcm-io-samples-sample-content{runmode}", "1.3.1-SNAPSHOT"));
    File contentInstallDirPublish = new File(contentDir, "install.publish");
    assertFiles(contentInstallDirPublish, ".publish",
        contentPackage("acs-aem-commons-ui.content{runmode}", "4.10.0"),
        contentPackage("wcm-io-samples-sample-content{runmode}", "1.3.1-SNAPSHOT"),
        contentPackage("aem-cms-publish-sling-mapping{runmode}"));

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, "install.author", "install.publish");

    File containerInstallDirAuthor = new File(containerDir, "install.author");
    assertFiles(containerInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-package{runmode}", "3.0.0"),
        contentPackage("core.wcm.components.all{runmode}", "2.17.0"),
        contentPackage("core.wcm.components.config{runmode}", "2.17.0"),
        contentPackage("wcm-io-samples-aem-cms-config{runmode}"),
        contentPackage("wcm-io-samples-complete{runmode}", "1.3.1-SNAPSHOT"));
    File containerInstallDirPublish = new File(containerDir, "install.publish");
    assertFiles(containerInstallDirPublish, ".publish",
        contentPackage("core.wcm.components.all{runmode}", "2.17.0"),
        contentPackage("core.wcm.components.config{runmode}", "2.17.0"),
        contentPackage("wcm-io-samples-aem-cms-config{runmode}"),
        contentPackage("wcm-io-samples-complete{runmode}", "1.3.1-SNAPSHOT"));
  }

  @Test
  void testBuild_AUTODEPENDENCIES_OFF() throws Exception {
    List<InstallableFile> filesAuthor = new ModelParser(nodeDirAuthor).getInstallableFilesForNode();
    List<InstallableFile> filesPublish = new ModelParser(nodeDirPublish).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .runModeOptimization(RunModeOptimization.ELIMINATE_DUPLICATES);
    builder.add(filesAuthor, Collections.emptySet());
    builder.add(filesPublish, Collections.emptySet());
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, "install", "install.author");

    File applicationInstallDir = new File(applicationDir, "install");
    assertFiles(applicationInstallDir, "",
        contentPackage("core.wcm.components.content", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248")),
        contentPackage("core.wcm.components.extensions.amp.content", "2.17.0"),
        contentPackage("acs-aem-commons-ui.apps", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6")),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    File applicationInstallDirAuthor = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0"),
        contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0"),
        file("io.wcm.caconfig.editor-1.11.0.jar"));

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, "install", "install.author", "install.publish");

    File contentInstallDir = new File(contentDir, "install");
    assertFiles(contentInstallDir, "",
        contentPackage("acs-aem-commons-ui.content", "4.10.0"),
        contentPackage("wcm-io-samples-sample-content", "1.3.1-SNAPSHOT"));
    File contentInstallDirAuthor = new File(contentDir, "install.author");
    assertFiles(contentInstallDirAuthor, ".author",
        contentPackage("aem-cms-author-replicationagents{runmode}"));
    File contentInstallDirPublish = new File(contentDir, "install.publish");
    assertFiles(contentInstallDirPublish, ".publish",
        contentPackage("aem-cms-publish-sling-mapping{runmode}"));

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, "install", "install.author");

    File containerInstallDir = new File(containerDir, "install");
    assertFiles(containerInstallDir, "",
        contentPackage("core.wcm.components.all", "2.17.0"),
        contentPackage("core.wcm.components.config", "2.17.0"),
        contentPackage("wcm-io-samples-aem-cms-config"),
        contentPackage("wcm-io-samples-complete", "1.3.1-SNAPSHOT"));
    File containerInstallDirAuthor = new File(containerDir, "install.author");
    assertFiles(containerInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-package{runmode}", "3.0.0"));
  }

  @Test
  void testBuild_IMMUTABLE_MUTABLE_COMBINED() throws Exception {
    List<InstallableFile> filesAuthor = new ModelParser(nodeDirAuthor).getInstallableFilesForNode();
    List<InstallableFile> filesPublish = new ModelParser(nodeDirPublish).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_COMBINED)
        .runModeOptimization(RunModeOptimization.ELIMINATE_DUPLICATES);
    builder.add(filesAuthor, Collections.emptySet());
    builder.add(filesPublish, Collections.emptySet());
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, "install", "install.author");

    File applicationInstallDir = new File(applicationDir, "install");
    assertFiles(applicationInstallDir, "",
        contentPackage("core.wcm.components.content", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248"),
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")),
        contentPackage("core.wcm.components.extensions.amp.content", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")),
        contentPackage("acs-aem-commons-ui.apps", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6"),
            dep("adobe/cq60:core.wcm.components.all:2.17.0")),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    File applicationInstallDirAuthor = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")),
        contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0",
            dep("Netcentric:accesscontroltool-package{runmode}:3.0.0")),
        file("io.wcm.caconfig.editor-1.11.0.jar"));

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, "install", "install.author", "install.publish");

    File contentInstallDir = new File(contentDir, "install");
    assertFiles(contentInstallDir, "",
        contentPackage("acs-aem-commons-ui.content", "4.10.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("wcm-io-samples-sample-content", "1.3.1-SNAPSHOT",
            dep("wcm-io-samples:wcm-io-samples-complete:1.3.1-SNAPSHOT")));
    File contentInstallDirAuthor = new File(contentDir, "install.author");
    assertFiles(contentInstallDirAuthor, ".author",
        contentPackage("aem-cms-author-replicationagents{runmode}",
            dep("adobe/cq60:core.wcm.components.all:2.17.0")));

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, "install", "install.author");

    File containerInstallDir = new File(containerDir, "install");
    assertFiles(containerInstallDir, ".",
        contentPackage("core.wcm.components.all", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")),
        contentPackage("core.wcm.components.config", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")),
        contentPackage("wcm-io-samples-aem-cms-config",
            dep("wcm-io-samples:aem-cms-system-config:1.3.1-SNAPSHOT")),
        contentPackage("wcm-io-samples-complete", "1.3.1-SNAPSHOT",
            dep("wcm-io-samples:wcm-io-samples-aem-cms-config:1.3.1-SNAPSHOT")));
    File containerInstallDirAuthor = new File(containerDir, "install.author");
    assertFiles(containerInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")));
  }

  @Test
  void testBuild_IMMUTABLE_MUTABLE_SEPARATE() throws Exception {
    List<InstallableFile> filesAuthor = new ModelParser(nodeDirAuthor).getInstallableFilesForNode();
    List<InstallableFile> filesPublish = new ModelParser(nodeDirPublish).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_MUTABLE_SEPARATE)
        .runModeOptimization(RunModeOptimization.ELIMINATE_DUPLICATES);
    builder.add(filesAuthor, Collections.emptySet());
    builder.add(filesPublish, Collections.emptySet());
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, "install", "install.author");

    File applicationInstallDir = new File(applicationDir, "install");
    assertFiles(applicationInstallDir, "",
        contentPackage("core.wcm.components.content", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248"),
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("core.wcm.components.extensions.amp.content", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("acs-aem-commons-ui.apps", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6"),
            dep("adobe/cq60:core.wcm.components.all:2.17.0")),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    File applicationInstallDirAuthor = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0",
            dep("Netcentric:accesscontroltool-package{runmode}:3.0.0")),
        file("io.wcm.caconfig.editor-1.11.0.jar"));

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, "install", "install.author", "install.publish");

    File contentInstallDir = new File(contentDir, "install");
    assertFiles(contentInstallDir, "",
        contentPackage("acs-aem-commons-ui.content", "4.10.0"),
        contentPackage("wcm-io-samples-sample-content", "1.3.1-SNAPSHOT",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")));
    File contentInstallDirAuthor = new File(contentDir, "install.author");
    assertFiles(contentInstallDirAuthor, ".author",
        contentPackage("aem-cms-author-replicationagents{runmode}",
            dep("adobe/consulting:acs-aem-commons-ui.content:4.10.0")));
    File contentInstallDirPublish = new File(contentDir, "install.publish");
    assertFiles(contentInstallDirPublish, ".publish",
        contentPackage("aem-cms-publish-sling-mapping{runmode}",
            dep("wcm-io-samples:wcm-io-samples-sample-content:1.3.1-SNAPSHOT")));

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, "install", "install.author");

    File containerInstallDir = new File(containerDir, "install");
    assertFiles(containerInstallDir, "",
        contentPackage("core.wcm.components.all", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("core.wcm.components.config", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("wcm-io-samples-aem-cms-config",
            dep("wcm-io-samples:aem-cms-system-config:1.3.1-SNAPSHOT")),
        contentPackage("wcm-io-samples-complete", "1.3.1-SNAPSHOT",
            dep("wcm-io-samples:wcm-io-samples-aem-cms-config:1.3.1-SNAPSHOT")));
    File containerInstallDirAuthor = new File(containerDir, "install.author");
    assertFiles(containerInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")));
  }

  @Test
  void testBuild_IMMUTABLE_ONLY() throws Exception {
    List<InstallableFile> filesAuthor = new ModelParser(nodeDirAuthor).getInstallableFilesForNode();
    List<InstallableFile> filesPublish = new ModelParser(nodeDirPublish).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .autoDependenciesMode(AutoDependenciesMode.IMMUTABLE_ONLY)
        .runModeOptimization(RunModeOptimization.ELIMINATE_DUPLICATES);
    builder.add(filesAuthor, Collections.emptySet());
    builder.add(filesPublish, Collections.emptySet());
    assertTrue(builder.build(null));

    ZipUtil.unpack(targetFile, targetUnpackDir);

    File appsDir = new File(targetUnpackDir, "jcr_root/apps/test-group-test-pkg-packages");
    assertDirectories(appsDir, "application", "content", "container");

    File applicationDir = new File(appsDir, "application");
    assertDirectories(applicationDir, "install", "install.author");

    File applicationInstallDir = new File(applicationDir, "install");
    assertFiles(applicationInstallDir, "",
        contentPackage("core.wcm.components.content", "2.17.0",
            dep("day/cq60/product:cq-platform-content:1.3.248"),
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("core.wcm.components.extensions.amp.content", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("acs-aem-commons-ui.apps", "4.10.0",
            dep("day/cq60/product:cq-content:6.3.64")),
        contentPackage("aem-cms-system-config",
            dep("day/cq60/product:cq-ui-wcm-editor-content:1.1.224"),
            dep("adobe/cq/product:cq-remotedam-client-ui-components:1.1.6"),
            dep("adobe/cq60:core.wcm.components.all:2.17.0")),
        file("io.wcm.wcm.ui.granite-1.9.2.jar"));
    File applicationInstallDirAuthor = new File(applicationDir, "install.author");
    assertFiles(applicationInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-apps-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("accesscontroltool-oakindex-package{runmode}", "3.0.0",
            dep("Netcentric:accesscontroltool-package{runmode}:3.0.0")),
        file("io.wcm.caconfig.editor-1.11.0.jar"));

    File contentDir = new File(appsDir, "content");
    assertDirectories(contentDir, "install", "install.author", "install.publish");

    File contentInstallDir = new File(contentDir, "install");
    assertFiles(contentInstallDir, "",
        contentPackage("acs-aem-commons-ui.content", "4.10.0"),
        contentPackage("wcm-io-samples-sample-content", "1.3.1-SNAPSHOT"));
    File contentInstallDirAuthor = new File(contentDir, "install.author");
    assertFiles(contentInstallDirAuthor, ".author",
        contentPackage("aem-cms-author-replicationagents{runmode}"));
    File contentInstallDirPublish = new File(contentDir, "install.publish");
    assertFiles(contentInstallDirPublish, ".publish",
        contentPackage("aem-cms-publish-sling-mapping{runmode}"));

    File containerDir = new File(appsDir, "container");
    assertDirectories(containerDir, "install", "install.author");

    File containerInstallDir = new File(containerDir, "install");
    assertFiles(containerInstallDir, "",
        contentPackage("core.wcm.components.all", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("core.wcm.components.config", "2.17.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")),
        contentPackage("wcm-io-samples-aem-cms-config",
            dep("wcm-io-samples:aem-cms-system-config:1.3.1-SNAPSHOT")),
        contentPackage("wcm-io-samples-complete", "1.3.1-SNAPSHOT",
            dep("wcm-io-samples:wcm-io-samples-aem-cms-config:1.3.1-SNAPSHOT")));
    File containerInstallDirAuthor = new File(containerDir, "install.author");
    assertFiles(containerInstallDirAuthor, ".author",
        contentPackage("accesscontroltool-package{runmode}", "3.0.0",
            dep("adobe/consulting:acs-aem-commons-ui.apps:4.10.0")));
  }

}
