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
package io.wcm.devops.conga.plugins.aem.maven.model;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Represents an OSGi bundle file referenced by CONGA.
 */
public final class BundleFile implements InstallableFile {

  private final File file;

  private final Boolean install;

  private final List<String> variants;

  /**
   * @param file JAR file
   * @param fileData File data
   * @param variants Variants
   */
  public BundleFile(File file, Map<String, Object> fileData, List<String> variants) {
    this.file = file;

    this.install = (Boolean)fileData.get("install");

    this.variants = variants;
  }

  @Override
  public File getFile() {
    return this.file;
  }

  @Override
  public Boolean getInstall() {
    return this.install;
  }

  @Override
  public List<String> getVariants() {
    return this.variants;
  }

  @Override
  public String toString() {
    return file.toString();
  }

}
