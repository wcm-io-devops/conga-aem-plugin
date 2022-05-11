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
 * How to validate package types to be included in "all" package.
 */
public enum PackageTypeValidation {

  /**
   * Strict mode: Ignores packages without content type (with warning), fails build if "mixed" package types are used.
   * For AEMaaCS it is mandatory to use this mode.
   */
  STRICT,

  /**
   * Includes all packages, but generated warnings about packages without content type or "mixed" package types.
   */
  WARN

}
