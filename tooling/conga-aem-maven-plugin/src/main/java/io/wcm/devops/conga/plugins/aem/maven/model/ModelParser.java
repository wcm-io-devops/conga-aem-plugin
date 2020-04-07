/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import static io.wcm.devops.conga.generator.util.FileUtil.getCanonicalPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;
import org.yaml.snakeyaml.Yaml;

import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackagePropertiesPostProcessor;

/**
 * Parsers model.yaml files generated by CONGA.
 */
public final class ModelParser {

  private static final String MODEL_FILE = "model.yaml";

  private final Yaml yaml;

  /**
   * Constructor
   */
  public ModelParser() {
    this.yaml = YamlUtil.createYaml();
  }

  /**
   * Parses model.yaml file for given node and returns all content packages references in this fileData.
   * @param nodeDir Node directory
   * @return List of content packages
   */
  public List<ContentPackageFile> getContentPackagesForNode(File nodeDir) {
    File modelFile = new File(nodeDir, MODEL_FILE);
    if (!modelFile.exists() || !modelFile.isFile()) {
      throw new RuntimeException("Model file not found: " + getCanonicalPath(modelFile));
    }
    Map<String, Object> data = parseYaml(modelFile);
    return collectPackages(data, nodeDir);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseYaml(File modelFile) {
    try {
      try (InputStream is = new FileInputStream(modelFile);
          Reader reader = new InputStreamReader(is, CharEncoding.UTF_8)) {
        return yaml.loadAs(reader, Map.class);
      }
    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to parse " + getCanonicalPath(modelFile), ex);
    }
  }

  @SuppressWarnings("unchecked")
  private List<ContentPackageFile> collectPackages(Map<String, Object> data, File nodeDir) {
    List<ContentPackageFile> items = new ArrayList<>();
    List<Map<String, Object>> roles = (List<Map<String, Object>>)data.get("roles");
    if (roles != null) {
      for (Map<String, Object> role : roles) {
        List<Map<String, Object>> files = (List<Map<String, Object>>)role.get("files");
        if (files != null) {
          for (Map<String, Object> file : files) {
            if (file.get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY) != null) {
              items.add(toContentPackageFile(file, role, nodeDir));
            }
          }
        }
      }
    }
    return items;
  }

  private ContentPackageFile toContentPackageFile(Map<String, Object> fileData,
      Map<String, Object> roleData, File nodeDir) {
    String path = (String)fileData.get("path");
    File file = new File(nodeDir, path);
    return new ContentPackageFile(file, fileData, roleData);
  }

}
