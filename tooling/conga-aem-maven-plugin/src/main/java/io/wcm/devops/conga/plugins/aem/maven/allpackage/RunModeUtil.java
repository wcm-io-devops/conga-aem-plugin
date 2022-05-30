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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;

final class RunModeUtil {

  static final String RUNMODE_AUTHOR = "author";
  static final String RUNMODE_PUBLISH = "publish";

  private RunModeUtil() {
    // static methods only
  }

  /**
   * Checks if the given variants map to author run mode, but not to publish run mode.
   * @param file Content package
   * @return true if only author run modes
   */
  public static boolean isOnlyAuthor(InstallableFile file) {
    Set<String> runModes = mapVariantsToRunModes(file.getVariants());
    return runModes.contains(RUNMODE_AUTHOR) && !runModes.contains(RUNMODE_PUBLISH);
  }

  /**
   * Checks if the given variants map to publish run mode, but not to author run mode.
   * @param file Content package
   * @return true if only publish run modes
   */
  public static boolean isOnlyPublish(InstallableFile file) {
    Set<String> runModes = mapVariantsToRunModes(file.getVariants());
    return runModes.contains(RUNMODE_PUBLISH) && !runModes.contains(RUNMODE_AUTHOR);
  }

  private static Set<String> mapVariantsToRunModes(Collection<String> variants) {
    return variants.stream()
        .map(RunModeUtil::mapVariantToRunMode)
        .collect(Collectors.toSet());
  }

  /**
   * Maps well-known variant names from CONGA AEM definitions to the corresponding run modes.
   * If the variant name is not well-known the variant name is used as run mode.
   * @param variant Variant
   * @return Run mode
   */
  private static String mapVariantToRunMode(String variant) {
    if ("aem-author".equals(variant)) {
      return RUNMODE_AUTHOR;
    }
    else if ("aem-publish".equals(variant)) {
      return RUNMODE_PUBLISH;
    }
    return variant;
  }

  /**
   * Flattens and optimizes multiple file sets into a single one, eliminating identical duplicate file references that
   * are present for both author and publish run mode. The order of the list is driven by the first list(s). Files
   * only present in the latter list(s) are appended to the end.
   * @param fileSets File sets with files with run modes
   * @return Flattened list of bundles with run modes. If a file is present for both author and publish runmode, no
   *         author/publish runmode is set.
   */
  public static <T extends InstallableFile> Collection<InstallableFileWithEnvironmentRunModes<T>> eliminateAuthorPublishDuplicates(
      List<? extends FileSet<T>> fileSets) {
    List<InstallableFileWithEnvironmentRunModes<T>> result = new ArrayList<>();
    // build distinct list of all files with combined run modes from variants, but separated by environment run modes
    fileSets.forEach(fileSet -> {
      fileSet.getFiles().forEach(file -> {
        Optional<InstallableFileWithEnvironmentRunModes<T>> existingFile = result.stream()
            .filter(item -> item.getEnvironmentRunModes().equals(fileSet.getEnvironmentRunModes())
                && isSameFileNameHash(item.getFile(), file))
            .findFirst();
        if (existingFile.isPresent()) {
          // if file was already added from other file set: eliminate duplicated, but add run modes
          existingFile.get().getFile().getVariants().addAll(file.getVariants());
        }
        else {
          result.add(new InstallableFileWithEnvironmentRunModes<>(file, fileSet.getEnvironmentRunModes()));
        }

      });
    });
    // eliminate author+publish run modes if both are set on same file
    result.forEach(file -> removeAuthorPublishRunModeIfBothPresent(file.getFile().getVariants()));
    return result;
  }

  private static boolean isSameFileNameHash(InstallableFile file1, InstallableFile file2) {
    if (!StringUtils.equals(file1.getFile().getName(), file2.getFile().getName())) {
      return false;
    }
    return file1.getHashCode().equals(file2.getHashCode());
  }

  /**
   * Removes author and publish runmodes from given set if both are present.
   * @param runModes Run modes
   */
  private static void removeAuthorPublishRunModeIfBothPresent(Set<String> runModes) {
    if (runModes.contains(RUNMODE_AUTHOR) && runModes.contains(RUNMODE_PUBLISH)) {
      runModes.remove(RUNMODE_AUTHOR);
      runModes.remove(RUNMODE_PUBLISH);
    }
  }

}
