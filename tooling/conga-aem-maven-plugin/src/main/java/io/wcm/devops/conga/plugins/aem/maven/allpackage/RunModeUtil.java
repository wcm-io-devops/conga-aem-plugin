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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;

final class RunModeUtil {

  static final String RUNMODE_AUTHOR = "author";
  static final String RUNMODE_PUBLISH = "publish";

  private RunModeUtil() {
    // static methods only
  }

  /**
   * Checks if the given variants map to author run mode, but not to publish run mode.
   * @param pkg Content package
   * @return true if only author run modes
   */
  public static boolean isOnlyAuthor(ContentPackageFile pkg) {
    Set<String> runModes = mapVariantsToRunModes(pkg.getVariants());
    return runModes.contains(RUNMODE_AUTHOR) && !runModes.contains(RUNMODE_PUBLISH);
  }

  /**
   * Checks if the given variants map to publish run mode, but not to author run mode.
   * @param pkg Content package
   * @return true if only publish run modes
   */
  public static boolean isOnlyPublish(ContentPackageFile pkg) {
    Set<String> runModes = mapVariantsToRunModes(pkg.getVariants());
    return runModes.contains(RUNMODE_PUBLISH) && !runModes.contains(RUNMODE_AUTHOR);
  }

  private static Set<String> mapVariantsToRunModes(List<String> variants) {
    return variants.stream()
        .map(RunModeUtil::mapVariantToRunMode)
        .collect(Collectors.toSet());
  }

  /**
   * Maps well-known variant names from CONG AEM definitions to the corresponding run modes.
   * If the variant name is not well-known the variant name is used as run mode.
   * @param variant Variant
   * @return Run mode
   */
  private static String mapVariantToRunMode(String variant) {
    //
    if ("aem-author".equals(variant)) {
      return RUNMODE_AUTHOR;
    }
    else if ("aem-publish".equals(variant)) {
      return RUNMODE_PUBLISH;
    }
    return variant;
  }

}
