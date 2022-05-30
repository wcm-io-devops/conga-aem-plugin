/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_DEPENDENCIES;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_GROUP;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_NAME;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_VERSION;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import io.wcm.devops.conga.plugins.aem.maven.model.AbstractInstallableFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;

/**
 * References a temporary file representing an partially processed content package to be included in the "all" package.
 */
final class TemporaryContentPackageFile extends AbstractInstallableFile implements ContentPackageFile {

  private final String name;
  private final String group;
  private final String version;
  private final String packageType;
  private final String dependencies;

  TemporaryContentPackageFile(File file, Collection<String> variants) throws IOException {
    super(file, variants);
    Map<String, Object> props = ContentPackageProperties.get(file);
    this.name = (String)props.get(NAME_NAME);
    this.group = (String)props.get(NAME_GROUP);
    this.version = (String)props.get(NAME_VERSION);
    this.packageType = (String)props.get(NAME_PACKAGE_TYPE);
    this.dependencies = (String)props.get(NAME_DEPENDENCIES);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getGroup() {
    return this.group;
  }

  @Override
  public String getVersion() {
    return this.version;
  }

  @Override
  public String getPackageType() {
    return this.packageType;
  }

  public String getDependencies() {
    return this.dependencies;
  }

  public String getPackageInfoWithDependencies() {
    StringBuilder sb = new StringBuilder();
    sb.append(getPackageInfo());
    if (this.dependencies != null) {
      sb.append(" (dependencies: ");
      sb.append(this.dependencies);
      sb.append(")");
    }
    return sb.toString();
  }

}
