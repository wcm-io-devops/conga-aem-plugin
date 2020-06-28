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
package io.wcm.devops.conga.plugins.aem.maven;

/**
 * Automatically generate dependencies between content packages based on file order in CONGA configuration.
 */
public enum AutoDependenciesMode {

  /**
   * Generate a single dependency chain spanning both immutable and mutable content packages.
   */
  IMMUTABLE_MUTABLE_COMBINED,

  /**
   * Generate separate dependency chains for immutable and mutable content packages.
   */
  IMMUTABLE_MUTABLE_SEPARATE,

  /**
   * Generate a dependency chain only for immutable content packages.
   */
  IMMUTABLE_ONLY,

  /**
   * Do not generate dependencies between content packages.
   */
  OFF

}
