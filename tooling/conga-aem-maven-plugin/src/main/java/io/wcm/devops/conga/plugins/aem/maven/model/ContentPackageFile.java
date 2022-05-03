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
package io.wcm.devops.conga.plugins.aem.maven.model;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic representation of content package file.
 */
public interface ContentPackageFile extends InstallableFile {

  /**
   * @return Package name
   */
  @NotNull
  String getName();

  /**
   * @return Package group
   */
  @NotNull
  String getGroup();

  /**
   * @return Package version
   */
  @NotNull
  String getVersion();

  /**
   * @return Package type
   */
  @Nullable
  String getPackageType();

  default String getPackageInfo() {
    return StringUtils.defaultString(getGroup())
        + ":" + StringUtils.defaultString(getName())
        + ":" + StringUtils.defaultString(getVersion());
  }

}
