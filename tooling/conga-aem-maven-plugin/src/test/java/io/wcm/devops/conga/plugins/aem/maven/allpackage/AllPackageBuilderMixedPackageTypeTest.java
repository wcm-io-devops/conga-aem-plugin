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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.wcm.devops.conga.plugins.aem.maven.PackageTypeValidation;
import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

class AllPackageBuilderMixedPackageTypeTest {

  private static final Set<String> NO_RUNMODES = Collections.emptySet();

  private File nodeDir;
  private File targetDir;
  private File targetUnpackDir;

  @BeforeEach
  void setUp(TestInfo testInfo) throws IOException {
    nodeDir = new File("src/test/resources/node-mixed-package");
    targetDir = new File("target/test-" + getClass().getSimpleName() + "_" + testInfo.getDisplayName());
    targetUnpackDir = new File(targetDir, "unpack");
    FileUtils.deleteDirectory(targetDir);
    targetDir.mkdirs();
    targetUnpackDir.mkdirs();
  }

  @Test
  void testBuild_Strict() throws Exception {
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .packageTypeValidation(PackageTypeValidation.STRICT);

    // should fail due to "mixed" packageType
    assertThrows(IllegalArgumentException.class, () -> {
      builder.add(files, NO_RUNMODES);
    });
  }

  @Test
  void testBuild_Warn() throws Exception {
    List<InstallableFile> files = new ModelParser(nodeDir).getInstallableFilesForNode();
    File targetFile = new File(targetDir, "all.zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, "test-group", "test-pkg")
        .packageTypeValidation(PackageTypeValidation.WARN);

    // should not fail due to "mixed" packageType
    builder.add(files, NO_RUNMODES);

    assertTrue(builder.build(Collections.emptyMap()));
  }

}
