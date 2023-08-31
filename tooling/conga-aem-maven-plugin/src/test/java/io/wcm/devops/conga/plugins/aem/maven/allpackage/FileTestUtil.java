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

import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ContentPackageTestUtil.assertXpathEvaluatesTo;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.ContentPackageTestUtil.getXmlFromZip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

public final class FileTestUtil {

  private FileTestUtil() {
    // static methods only
  }

  static @NotNull ExpectedFile file(@NotNull String fileName) {
    return new ExpectedFile(fileName);
  }

  static @NotNull ExpectedContentPackage contentPackage(@NotNull String packageName,
      @NotNull ExpectedDependency @NotNull... dependencies) {
    return new ExpectedContentPackage(packageName, null, Arrays.asList(dependencies));
  }

  static @NotNull ExpectedContentPackage contentPackage(@NotNull String packageName, @Nullable String version,
      @NotNull ExpectedDependency @NotNull... dependencies) {
    return new ExpectedContentPackage(packageName, version, Arrays.asList(dependencies));
  }

  static @NotNull ExpectedDependency dep(@NotNull String packageReference) {
    return new ExpectedDependency(packageReference);
  }


  /**
   * Assert existence of given content packages (with dependencies) and other files.
   * @param dir Directory
   * @param expectedFiles Expected content packages/files and their dependencies
   */
  public static void assertFiles(File dir, String runmodeSuffix, ExpectedFile... expectedFiles) throws Exception {
    assertTrue(dir.exists(), "file exists: " + dir.getPath());
    assertTrue(dir.isDirectory(), "is directory: " + dir.getPath());

    // assert existing files match the expected files
    Set<String> expectedFileNames = Stream.of(expectedFiles)
        .map(file -> file.getFileName(runmodeSuffix))
        .collect(Collectors.toCollection(TreeSet::new));
    String[] fileNames = dir.list();
    Set<String> actualFileNames = fileNames != null ? new TreeSet<>(Set.of(fileNames)) : Set.of();
    assertEquals(expectedFileNames, actualFileNames, "files in " + dir.getPath());

    // extra check for content packages
    for (ExpectedFile file : expectedFiles) {
      if (file instanceof ExpectedContentPackage) {
        ExpectedContentPackage cp = (ExpectedContentPackage)file;
        File zipFile = new File(dir, cp.getFileName(runmodeSuffix));
        Document propsXml = getXmlFromZip(zipFile, "META-INF/vault/properties.xml");

        // assert package name
        String expectedPackageName = cp.getPackageName(runmodeSuffix);
        assertXpathEvaluatesTo(expectedPackageName, "/properties/entry[@key='name']", propsXml,
            "Package name of " + file.getFileName(runmodeSuffix));

        // assert package version
        String expectedVersion = cp.getVersion();
        if (expectedVersion != null) {
          assertXpathEvaluatesTo(expectedVersion, "/properties/entry[@key='version']", propsXml,
              "Package version of " + file.getFileName(runmodeSuffix));
        }

        // assert package dependencies
        String expectedDependencies = cp.getDependencies().stream()
            .map(dep -> dep.getPackageReference(runmodeSuffix))
            .collect(Collectors.joining(","));
        assertXpathEvaluatesTo(expectedDependencies, "/properties/entry[@key='dependencies']", propsXml,
            "Package dependencies of " + file.getFileName(runmodeSuffix));
      }
    }
  }

  /**
   * Assert existence of given directories.
   * @param dir Directory
   * @param fileNames Expected file names in directory
   */
  public static void assertDirectories(File dir, String... fileNames) {
    assertTrue(dir.exists(), "directory exists: " + dir.getPath());
    assertTrue(dir.isDirectory(), "is directory: " + dir.getPath());
    Set<String> expectedDirectoryNames = Set.of(fileNames);
    Set<String> actualDirectoryNames = Collections.emptySet();
    File[] files = dir.listFiles();
    if (files != null) {
      actualDirectoryNames = Stream.of(files)
          .filter(File::isDirectory)
          .map(File::getName)
          .collect(Collectors.toSet());
    }
    assertEquals(expectedDirectoryNames, actualDirectoryNames, "files in " + dir.getPath());
  }

}
