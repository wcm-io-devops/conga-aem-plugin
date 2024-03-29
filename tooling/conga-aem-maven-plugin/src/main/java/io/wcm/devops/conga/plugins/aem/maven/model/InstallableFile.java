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
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.hash.HashCode;

/**
 * Generic representation of an installable file (content package or OSGi bundle).
 */
public interface InstallableFile {

  /**
   * @return File.
   */
  @NotNull
  File getFile();

  /**
   * @return Variants/Run modes for file.
   */
  @NotNull
  Set<String> getVariants();

  /**
   * @return Hash code for file.
   */
  @NotNull
  HashCode getHashCode();

}
