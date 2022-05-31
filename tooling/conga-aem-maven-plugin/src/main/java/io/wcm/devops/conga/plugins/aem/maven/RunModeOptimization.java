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
package io.wcm.devops.conga.plugins.aem.maven;

/**
 * How to optimize author/publish run modes in resulting "all" package.
 */
public enum RunModeOptimization {

  /**
   * No optimization takes place. Content packages and bundles are duplicated for author/publish
   * run modes to ensure strict following of dependency chain defined in CONGA.
   */
  OFF,

  /**
   * Eliminates duplicates to ensure that content packages and bundles that are installed on both author and publish
   * instances are contained only once in the "all" package.
   */
  ELIMINATE_DUPLICATES

}
