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

import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.apache.maven.archiver.MavenArchiver;
import org.jetbrains.annotations.Nullable;

/**
 * Parse/convert ${project.build.outputTimestamp}.
 */
public class BuildOutputTimestamp {

  private final Date date;

  /**
   * @param outputTimestamp Configured output timestamp
   */
  public BuildOutputTimestamp(@Nullable String outputTimestamp) {
    MavenArchiver mavenArchiver = new MavenArchiver();
    this.date = mavenArchiver.parseOutputTimestamp(outputTimestamp);
  }

  /**
   * @return true if a valid timestamp is configured
   */
  public boolean isValid() {
    return date != null;
  }

  /**
   * @return Date or null if not a valid date
   */
  @Nullable
  public Date toDate() {
    return date;
  }

  /**
   * @return FileTime or null if not a valid date
   */
  @Nullable
  public FileTime toFileTime() {
    if (date != null) {
      return FileTime.fromMillis(date.toInstant().getEpochSecond());
    }
    return null;
  }

}
