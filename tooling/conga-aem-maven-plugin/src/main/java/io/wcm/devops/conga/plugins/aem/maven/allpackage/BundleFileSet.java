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
package io.wcm.devops.conga.plugins.aem.maven.allpackage;

import java.util.Collection;
import java.util.List;

import io.wcm.devops.conga.plugins.aem.maven.model.BundleFile;

/**
 * Set of bundle files.
 */
class BundleFileSet implements FileSet<BundleFile> {

  private final List<BundleFile> bundles;
  private final List<String> environmentRunModes;

  BundleFileSet(List<BundleFile> bundles, List<String> environmentRunModes) {
    this.bundles = bundles;
    this.environmentRunModes = environmentRunModes;
  }

  @Override
  public List<BundleFile> getFiles() {
    return this.bundles;
  }

  @Override
  public Collection<String> getEnvironmentRunModes() {
    return this.environmentRunModes;
  }

  @Override
  public String toString() {
    return environmentRunModes + ": " + bundles;
  }

}
