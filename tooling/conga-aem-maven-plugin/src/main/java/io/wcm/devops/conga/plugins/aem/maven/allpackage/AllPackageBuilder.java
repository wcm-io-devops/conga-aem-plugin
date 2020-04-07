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
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_DEPENDENCIES;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.DependencyUtil;
import org.apache.jackrabbit.vault.packaging.VersionRange;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;

/**
 * Builds "all" package based on given set of content packages.
 */
public final class AllPackageBuilder {

  private final File targetFile;
  private final String groupName;
  private final String packageName;
  private boolean autoDependencies;

  /**
   * @param targetFile Target file
   * @param groupName Group name
   * @param packageName Package name
   */
  public AllPackageBuilder(File targetFile, String groupName, String packageName) {
    this.targetFile = targetFile;
    this.groupName = groupName;
    this.packageName = packageName;
  }

  /**
   * @param value Automatically generate dependencies between content packages based on file order in CONGA
   *          configuration.
   * @return this
   */
  public AllPackageBuilder autoDependencies(boolean value) {
    this.autoDependencies = value;
    return this;
  }

  /**
   * Build "all" content package.
   * @param contentPackages Content packages (invalid will be filtered out)
   * @return true if "all" package was generated, false if not valid package was found.
   * @throws IOException I/O exception
   */
  public boolean build(List<ContentPackageFile> contentPackages) throws IOException {

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
    ContentPackageFile previousPkg = null;
    try (ContentPackage contentPackage = builder.build(targetFile)) {
      for (ContentPackageFile pkg : validContentPackages) {
        String path = buildPackagePath(pkg, rootPath);
        if (autoDependencies && previousPkg != null) {
          // wire previous package in package dependency
          addFileWithDependency(contentPackage, path, pkg, previousPkg);
        }
        else {
          // add package file directly
          contentPackage.addFile(path, pkg.getFile());
        }
        previousPkg = pkg;
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

  /**
   * Rewrite content package ZIP file while adding to "all" package:
   * Add dependency to previous package in CONGA configuration file oder.
   * @param contentPackage Target content page
   * @param path Path in target content package
   * @param pkg Package to add
   * @param previousPkg Previous package to get dependency information from
   * @throws IOException I/O error
   */
  private static void addFileWithDependency(ContentPackage contentPackage, String path,
      ContentPackageFile pkg, ContentPackageFile previousPkg) throws IOException {

    // create temp zip file to create rewritten copy of package
    File tempFile = File.createTempFile("pkg", ".zip");

    // open original content package
    try (ZipFile zipFileIn = new ZipFile(pkg.getFile())) {

      // iterate through entries and write them to the temp. zip file
      try (FileOutputStream fos = new FileOutputStream(tempFile);
          ZipOutputStream zipOut = new ZipOutputStream(fos)) {
        Enumeration<? extends ZipEntry> zipInEntries = zipFileIn.entries();
        while (zipInEntries.hasMoreElements()) {
          ZipEntry zipInEntry = zipInEntries.nextElement();
          ZipEntry zipOutEntry = new ZipEntry(zipInEntry.getName());
          if (!zipInEntry.isDirectory()) {
            zipOut.putNextEntry(zipOutEntry);
            if (StringUtils.equals(zipInEntry.getName(), "META-INF/vault/properties.xml")) {
              // if entry is properties.xml, update dependency information
              try (InputStream is = zipFileIn.getInputStream(zipInEntry)) {
                Properties props = new Properties();
                props.loadFromXML(is);
                updateDependencies(props, previousPkg);
                props.storeToXML(zipOut, null);
              }
            }
            else {
              // otherwise transfer the binary data 1:1
              try (InputStream is = zipFileIn.getInputStream(zipInEntry)) {
                IOUtils.copy(is, zipOut);
              }
            }
            zipOut.closeEntry();
          }
        }
      }

      // add temp zip file to "all" content package
      contentPackage.addFile(path, tempFile);
    }
    finally {
      FileUtils.deleteQuietly(tempFile);
    }
  }

  /**
   * Add dependency information to dependencies string in properties (if it does not exist already).
   * @param props Properties
   * @param dependencyFile Dependency package
   */
  private static void updateDependencies(Properties props, ContentPackageFile dependencyFile) {
    String[] existingDepsStrings = StringUtils.split(props.getProperty(NAME_DEPENDENCIES), ",");
    Dependency[] existingDeps = null;
    if (existingDepsStrings != null && existingDepsStrings.length > 0) {
      existingDeps = Dependency.fromString(existingDepsStrings);
    }

    Dependency newDependency = new Dependency(dependencyFile.getGroup(), dependencyFile.getName(),
        VersionRange.fromString(dependencyFile.getVersion()));
    Dependency[] deps;
    if (existingDeps != null) {
      deps = DependencyUtil.add(existingDeps, newDependency);
    }
    else {
      deps = new Dependency[] { newDependency };
    }

    props.put(NAME_DEPENDENCIES, Dependency.toString(deps));
  }

}
