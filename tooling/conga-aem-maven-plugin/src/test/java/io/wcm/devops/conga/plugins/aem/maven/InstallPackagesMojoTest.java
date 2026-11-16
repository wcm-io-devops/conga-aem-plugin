/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
package io.wcm.devops.conga.plugins.aem.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;

import org.apache.jackrabbit.vault.packaging.DependencyHandling;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;
import io.wcm.tooling.commons.packmgr.install.PackageFile;

class InstallPackagesMojoTest {

  private final InstallPackagesMojo underTest = new InstallPackagesMojo();

  @Test
  void testToPackageFile() {
    List<InstallableFile> files = new ModelParser(new File("src/test/resources/node/aem-author")).getInstallableFilesForNode();

    // package with parameters defined in model.yaml
    PackageFile pkg6 = underTest.toPackageFile((ModelContentPackageFile)files.get(6));
    assertEquals(false, pkg6.isInstall());
    assertEquals(true, pkg6.isForce());
    assertEquals(true, pkg6.isRecursive());
    assertEquals(DependencyHandling.BEST_EFFORT, pkg6.getDependencyHandling());
    assertEquals(30, pkg6.getDelayAfterInstallSec());
    assertEquals(60, pkg6.getHttpSocketTimeoutSec());

    // package without parameters falls back to mojo defaults
    PackageFile pkg1 = underTest.toPackageFile((ModelContentPackageFile)files.get(1));
    assertNull(pkg1.getDependencyHandling());
    assertNull(pkg1.getHttpSocketTimeoutSec());
  }

}
