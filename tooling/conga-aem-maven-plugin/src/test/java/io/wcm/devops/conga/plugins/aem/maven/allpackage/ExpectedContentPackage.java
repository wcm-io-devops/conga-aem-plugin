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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ExpectedContentPackage extends ExpectedFile {

  private final String packageName;
  private final String version;
  private final Collection<ExpectedDependency> dependencies;

  ExpectedContentPackage(@NotNull String packageName, @Nullable String version, @NotNull Collection<ExpectedDependency> dependencies) {
    super(buildFileName(packageName, version));
    this.packageName = packageName;
    this.version = version;
    this.dependencies = dependencies;
  }

  private static String buildFileName(@NotNull String packageName, @Nullable String version) {
    StringBuilder sb = new StringBuilder();
    sb.append(packageName);
    if (version != null) {
      sb.append("-").append(version);
    }
    sb.append(".zip");
    return sb.toString();
  }

  public @NotNull String getPackageName(@NotNull String runModeSuffix) {
    return StringUtils.replace(this.packageName, RUNMODE_SUFFIX_PLACEHOLDER, runModeSuffix);
  }

  public @Nullable String getVersion() {
    return this.version;
  }

  public @NotNull Collection<ExpectedDependency> getDependencies() {
    return this.dependencies;
  }

}
