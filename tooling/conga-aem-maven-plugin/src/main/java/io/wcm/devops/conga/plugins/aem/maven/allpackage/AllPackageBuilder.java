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

import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.RUNMODE_AUTHOR;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.RUNMODE_PUBLISH;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;

/**
 * Builds "all" package based on given set of content packages.
 */
public final class AllPackageBuilder {

  private AllPackageBuilder() {
    // static methods only
  }

  /**
   * Build "all" content package.
   * @param targetFile Target file
   * @param contentPackages Content packages (invalid will be filtered out)
   * @param groupName Group name
   * @param packageName Package name
   * @return true if "all" package was generated, false if not valid package was found.
   */
  public static boolean build(File targetFile,
      List<ContentPackageFile> contentPackages,
      String groupName, String packageName) throws IOException {

    // collect AEM content packages for this node
    List<ContentPackageFile> validContentPackages = contentPackages.stream()
        .filter(AllPackageBuilder::isValid)
        .collect(Collectors.toList());
    if (validContentPackages.isEmpty()) {
      return false;
    }

    // prepare content package metadata
    ContentPackageBuilder builder = new ContentPackageBuilder()
        .group(groupName)
        .name(packageName)
        .packageType("container");

    // define root path for "all" package
    String rootPath = buildRootPath(groupName, packageName);
    builder.filter(new PackageFilter(rootPath));

    // build content package
    try (ContentPackage contentPackage = builder.build(targetFile)) {
      for (ContentPackageFile pkg : validContentPackages) {
        String path = buildPackagePath(pkg, rootPath);
        contentPackage.addFile(path, pkg.getFile());
      }
    }
    return true;
  }

  private static boolean isValid(ContentPackageFile pkg) {
    // accept only content packages with package type
    return pkg.getPackageType() != null;
  }

  /**
   * Build root path to be used for embedded package.
   * @param groupName Group name
   * @param packageName Package name
   * @return Package path
   */
  private static String buildRootPath(String groupName, String packageName) {
    return "/apps/" + groupName + "-" + packageName + "-packages";
  }

  /**
   * Build path to be used for embedded package.
   * @param pkg Package
   * @param rootPath Root path
   * @return Package path
   */
  private static String buildPackagePath(ContentPackageFile pkg, String rootPath) {
    String runModeSuffix = "";
    if (RunModeUtil.isOnlyAuthor(pkg)) {
      runModeSuffix = "." + RUNMODE_AUTHOR;
    }
    else if (RunModeUtil.isOnlyPublish(pkg)) {
      runModeSuffix = "." + RUNMODE_PUBLISH;
    }
    return rootPath + "/" + pkg.getPackageType() + "/install" + runModeSuffix + "/" + pkg.getFile().getName();
  }

}
