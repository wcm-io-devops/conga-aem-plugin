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
import java.util.stream.Stream;

import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;

class ContentPackageFileSet implements FileSet<ContentPackageFileWithRunMode> {

  private final List<ContentPackageFile> contentPackages;
  private final List<String> environmentRunModes;

  ContentPackageFileSet(List<ContentPackageFile> contentPackages, List<String> environmentRunModes) {
    this.contentPackages = contentPackages;
    this.environmentRunModes = environmentRunModes;
  }

  public List<ContentPackageFile> getContentPackages() {
    return this.contentPackages;
  }

  @Override
  public List<String> getEnvironmentRunModes() {
    return this.environmentRunModes;
  }

  @Override
  public Stream<ContentPackageFileWithRunMode> toFilesWithRunMode() {
    return contentPackages.stream()
        .map(contentPackage -> new ContentPackageFileWithRunMode(contentPackage, environmentRunModes));
  }

}
