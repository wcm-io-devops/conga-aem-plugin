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
 * How to handle versions of packages and sub-packages inside "all" package.
 */
public enum PackageVersionMode {

  /**
   * Keep original versions.
   */
  DEFAULT,

  /**
   * Suffix the version number of all packages with a release version with the version of the POM the Mojo runs in.
   * Within the version suffix, dots are replaced with underlines to avoid convision with the main version number.
   * This is useful when deploying to AMS with Cloud Manager.
   * <p>
   * Example:
   * </p>
   * <ul>
   * <li>Original package version: 2.5.0</li>
   * <li>POM version: 2022.1103.152749.0000000571</li>
   * <li>Resulting package version: 2.5.0-2022_1103_152749_0000000571</li>
   * </ul>
   */
  RELEASE_SUFFIX_VERSION

}
