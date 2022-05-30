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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import io.wcm.devops.conga.generator.util.FileUtil;

/**
 * Describes a file with a set of run modes and (lazily evaluated) hash code.
 */
abstract class FileWithRunMode {

  private final File file;
  private final Set<String> environmentRunModes;
  private HashCode hashCode;

  protected FileWithRunMode(File file, Collection<String> environmentRunModes) {
    this.file = file;
    this.environmentRunModes = new LinkedHashSet<>(environmentRunModes);
  }

  /**
   * @return Set of run modes. It's allowed to manipulated the set from outside.
   */
  Set<String> getEnvironmentRunModes() {
    return environmentRunModes;
  }

  HashCode getHashCode() {
    if (this.hashCode == null) {
      this.hashCode = getHashCode(file);
    }
    return this.hashCode;
  }

  boolean isSameFileNameHash(FileWithRunMode other) {
    if (!StringUtils.equals(file.getName(), other.file.getName())) {
      return false;
    }
    return getHashCode().equals(other.getHashCode());
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
