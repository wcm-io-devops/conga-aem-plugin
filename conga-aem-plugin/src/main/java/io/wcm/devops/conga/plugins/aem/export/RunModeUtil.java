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
package io.wcm.devops.conga.plugins.aem.export;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class RunModeUtil {

  static final String RUNMODE_AUTHOR = "author";
  static final String RUNMODE_PUBLISH = "publish";

  private RunModeUtil() {
    // static methods only
  }

  static Set<String> mapVariantsToRunModes(List<String> variants) {
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
  static String mapVariantToRunMode(String variant) {
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
