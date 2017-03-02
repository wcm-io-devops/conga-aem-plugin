/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.fscontentparser.ContentFileExtension;
import org.apache.sling.fscontentparser.ContentFileParser;
import org.apache.sling.fscontentparser.ContentFileParserFactory;
import org.apache.sling.fscontentparser.ParserOptions;

import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.util.FileUtil;

/**
 * Imports JSON data and binary data into Sling resource hierarchy.
 */
public final class JsonContentLoader {

  private static final Set<String> IGNORED_NAMES = ImmutableSet.of(
      JcrConstants.JCR_BASEVERSION,
      JcrConstants.JCR_PREDECESSORS,
      JcrConstants.JCR_SUCCESSORS,
      JcrConstants.JCR_CREATED,
      JcrConstants.JCR_VERSIONHISTORY,
      "jcr:checkedOut",
      "jcr:isCheckedOut",
      ":jcr:data");

  private final ContentFileParser parser;

  /**
   * Constructor
   */
  public JsonContentLoader() {
    this.parser = ContentFileParserFactory.create(ContentFileExtension.JSON, new ParserOptions()
        .detectCalendarValues(true)
        .ignorePropertyNames(IGNORED_NAMES)
        .ignoreResourceNames(IGNORED_NAMES));
  }

  /**
   * Load a JSON file and transform the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param jsonFile JSON file
   * @return Nested map with content data
   */
  public Map<String, Object> load(File jsonFile) {
    try (InputStream is = new FileInputStream(jsonFile)) {
      return parser.parse(is);
    }
    catch (Throwable ex) {
      throw new GeneratorException("Unable to parse JSON file: " + FileUtil.getCanonicalPath(jsonFile), ex);
    }
  }

  /**
   * Loads a JSON content and transform the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param inputStream JSON input stream
   * @return Nested map with content data
   * @throws IOException I/O exception
   */
  public Map<String, Object> load(InputStream inputStream) throws IOException {
    try {
      return parser.parse(inputStream);
    }
    catch (Throwable ex) {
      throw new GeneratorException("Unable to parse JSON stream.", ex);
    }
  }

}
