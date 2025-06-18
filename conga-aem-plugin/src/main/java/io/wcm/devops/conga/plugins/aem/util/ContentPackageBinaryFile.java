/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.devops.conga.plugins.aem.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;

import io.wcm.devops.conga.generator.UrlFileManager;

/**
 * Binary to be added to content package.
 */
public final class ContentPackageBinaryFile {

  private final String fileName;
  private final String dir;
  private final String url;
  private final String path;
  private final boolean delete;

  ContentPackageBinaryFile(String file, String dir, String url, String path, boolean delete) {
    this.fileName = file;
    this.dir = dir;
    this.url = url;
    this.path = path;
    this.delete = delete;
  }

  /**
   * @param urlFileManager URL file manager
   * @param targetDir Target directory for configuration
   * @return Input stream with binary data of file.
   * @throws IOException I/O exception
   */
  public InputStream getInputStream(UrlFileManager urlFileManager, File targetDir) throws IOException {
    if (StringUtils.isNotBlank(url)) {
      return urlFileManager.getFile(url);
    }
    File file = getFile(targetDir);
    if (file != null) {
      return new BufferedInputStream(new FileInputStream(file));
    }
    else {
      throw new IllegalArgumentException("For a file definition either url or file has to be specified.");
    }
  }

  private File getFile(File targetDir) {
    File parent = targetDir;
    if (StringUtils.isNotEmpty(dir)) {
      parent = new File(targetDir, dir);
    }
    if (StringUtils.isNotEmpty(fileName)) {
      return new File(parent, fileName);
    }
    return null;
  }

  /**
   * @return Target path for file (including file name).
   */
  public String getPath() {
    return this.path;
  }

  /**
   * If delete was configured for the file delete it.
   * @param urlFileManager URL file manager
   * @param targetDir Target directory for configuration
   * @throws IOException I/O exception
   */
  public void deleteIfRequired(UrlFileManager urlFileManager, File targetDir) throws IOException {
    if (delete) {
      if (StringUtils.isNotBlank(url)) {
        urlFileManager.deleteFile(url);
        return;
      }
      File file = getFile(targetDir);
      if (file != null) {
        Files.delete(file.toPath());
      }
    }
  }

}
