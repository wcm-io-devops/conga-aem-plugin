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
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import io.wcm.devops.conga.generator.util.FileUtil;

/**
 * Describes an installable file with a set of run modes and (lazily evaluated) hash code.
 */
public abstract class AbstractInstallableFile implements InstallableFile {

  private final File file;
  private final Set<String> variants;
  private HashCode hashCode;

  protected AbstractInstallableFile(File file, Collection<String> variants) {
    this.file = file;
    this.variants = new LinkedHashSet<>(variants);
  }

  @Override
  @NotNull
  public File getFile() {
    return file;
  }

  @Override
  @NotNull
  public Set<String> getVariants() {
    return variants;
  }

  @Override
  @NotNull
  public HashCode getHashCode() {
    if (this.hashCode == null) {
      this.hashCode = getHashCode(file);
    }
    return this.hashCode;
  }

  @Override
  public String toString() {
    return file.toString();
  }

  private static HashCode getHashCode(File file) {
    try {
      return Files.asByteSource(file).hash(Hashing.sha256());
    }
    catch (IOException ex) {
      throw new IllegalArgumentException("Unable to get hashcode for " + FileUtil.getCanonicalPath(file), ex);
    }
  }

}
