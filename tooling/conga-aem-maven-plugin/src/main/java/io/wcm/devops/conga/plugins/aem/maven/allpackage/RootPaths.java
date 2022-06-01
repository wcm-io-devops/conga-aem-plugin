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

import java.util.ArrayList;
import java.util.List;

import io.wcm.devops.conga.plugins.aem.maven.EmbedPackageMode;

/**
 * Manages root paths inside "all" package.
 */
class RootPaths {

  private final String contentPackagesRootPath;
  private final String bundlesRootPath;

  RootPaths(String groupName, String packageName, EmbedPackageMode embedPackageMode) {
    this.bundlesRootPath = buildEmbedRootPath(groupName, packageName);
    switch (embedPackageMode) {
      case EMBED:
        this.contentPackagesRootPath = this.bundlesRootPath;
        break;
      case SUB_PACKAGE:
        this.contentPackagesRootPath = buildSubPackageRootPath(groupName, packageName);
        break;
      default:
        throw new IllegalArgumentException("Invalid embed package mode: " + embedPackageMode);
    }
  }

  public String getContentPackagesRootPath() {
    return this.contentPackagesRootPath;
  }

  public String getBundlesRootPath() {
    return this.bundlesRootPath;
  }

  /**
   * Get list of actually used root paths in "all" packages (for package filter).
   * @param contentPackageFileSets Content package file sets
   * @param bundleFileSets Bundle file sets
   * @return Root Paths
   */
  public List<String> getActualUsedRootPaths(
      List<ContentPackageFileSet> contentPackageFileSets, List<BundleFileSet> bundleFileSets) {
    List<String> result = new ArrayList<>();
    if (hasAnyFile(contentPackageFileSets)) {
      result.add(this.contentPackagesRootPath);
    }
    if (hasAnyFile(bundleFileSets)) {
      result.add(this.bundlesRootPath);
    }
    return result;
  }

  private boolean hasAnyFile(List<? extends FileSet> fileSets) {
    return fileSets.stream()
        .anyMatch(fileSet -> !fileSet.getFiles().isEmpty());
  }

  /**
   * Build root path to be used for embedded packages/bundles in /apps.
   * @param groupName Group name
   * @param packageName Package name
   * @return Package path
   */
  private static String buildEmbedRootPath(String groupName, String packageName) {
    return "/apps/" + groupName + "-" + packageName + "-packages";
  }

  /**
   * Build root path to be used for sub packages in /etc/packages.
   * @param groupName Group name
   * @param packageName Package name
   * @return Package path
   */
  private static String buildSubPackageRootPath(String groupName, String packageName) {
    return "/etc/packages/" + groupName + "-" + packageName;
  }

}
