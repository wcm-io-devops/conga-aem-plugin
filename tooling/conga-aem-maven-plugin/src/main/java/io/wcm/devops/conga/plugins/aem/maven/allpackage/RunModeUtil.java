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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
   * Checks if the given package is to be installed on both author and publish instances
   * @param file Content package
   * @return true if author and publish run mode (or no run mode = no restriction)
   */
  public static boolean isAuthorAndPublish(InstallableFile file) {
    Set<String> runModes = mapVariantsToRunModes(file.getVariants());
    return (!runModes.contains(RUNMODE_AUTHOR) && !runModes.contains(RUNMODE_PUBLISH))
        || (runModes.contains(RUNMODE_AUTHOR) && runModes.contains(RUNMODE_PUBLISH));
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
   * Builds an optimized list of file sets separated for each environment run mode, but combined for author and publish
   * variant files. Those files are reduced to single items if they are present in both author and publish variants. The
   * order of the resulting file sets if driven by the first file set(s) in the list, additional files from other file
   * sets are added at the end of the result list(s).
   * @param fileSets Existing list of filesets
   * @param fileSetFactory Creates a new (empty) file set for given environment run mode
   * @return Optimized list of file sets (one per environment run mode)
   */
  public static <T extends InstallableFile, S extends FileSet<T>> Collection<S> eliminateAuthorPublishDuplicates(
      List<S> fileSets, Function<String, S> fileSetFactory) {
    Map<String, S> result = new LinkedHashMap<>();
    fileSets.forEach(fileSet -> fileSet.getEnvironmentRunModes().forEach(environmentRunMode -> {
        FileSet<T> resultFileSet = result.computeIfAbsent(environmentRunMode, fileSetFactory);
        fileSet.getFiles().forEach(file -> {
          Optional<T> existingFile = resultFileSet.getFiles().stream()
              .filter(item -> isSameFileNameHash(item, file))
              .findFirst();
          if (existingFile.isPresent()) {
            // if file was already added from other file set: eliminate duplicate, but add run modes
            existingFile.get().getVariants().addAll(file.getVariants());
          }
          else {
            resultFileSet.getFiles().add(file);
          }
        });
    }));
    // eliminate author+publish run modes if both are set on same file
    result.values().forEach(
        fileSet -> fileSet.getFiles().forEach(file -> removeAuthorPublishRunModeIfBothPresent(file.getVariants())));
    return result.values();
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
