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

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_AC_HANDLING;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_DESCRIPTION;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_FILTERS;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_GROUP;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_THUMBNAIL_IMAGE;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_VERSION;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.UrlFileManager;
import io.wcm.devops.conga.generator.spi.context.FileHeaderContext;
import io.wcm.devops.conga.model.util.MapExpander;
import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOsgiConfigPostProcessor;
import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackagePostProcessor;
import io.wcm.tooling.commons.contentpackagebuilder.AcHandling;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;

/**
 * Option property names for content package post processors {@link ContentPackagePostProcessor} and
 * {@link ContentPackageOsgiConfigPostProcessor}.
 */
public final class ContentPackageUtil {

  private static final String THUMBNAIL_IMAGE_DEFAULT = "/default-package-thumbnail.png";

  private ContentPackageUtil() {
    // constants only
  }

  /**
   * Builds content package builder populated with options from options map.
   * @param options Options
   * @param fileHeader File header
   * @return Content package builder
   */
  public static ContentPackageBuilder getContentPackageBuilder(Map<String, Object> options, FileHeaderContext fileHeader) {
    ContentPackageBuilder builder = new ContentPackageBuilder()
        .group(getMandatoryProp(options, PROPERTY_PACKAGE_GROUP))
        .name(getMandatoryProp(options, PROPERTY_PACKAGE_NAME))
        .description(mergeDescriptionFileHeader(getOptionalProp(options, PROPERTY_PACKAGE_DESCRIPTION), fileHeader))
        .version(getOptionalProp(options, PROPERTY_PACKAGE_VERSION));

    AcHandling acHandling = getAcHandling(options);
    if (acHandling != null) {
      builder.acHandling(acHandling);
    }

    String thumbnailImageUrl = getOptionalProp(options, PROPERTY_PACKAGE_THUMBNAIL_IMAGE);
    if (StringUtils.isNotBlank(thumbnailImageUrl)) {
      UrlFileManager urlFileManager = fileHeader.getUrlFileManager();
      try {
        builder.thumbnailImage(urlFileManager.getFile(thumbnailImageUrl));
      }
      catch (IOException ex) {
        throw new GeneratorException("Unable to set package thumbnail to " + thumbnailImageUrl, ex);
      }
    }
    else {
      try (InputStream is = ContentPackageUtil.class.getResourceAsStream(THUMBNAIL_IMAGE_DEFAULT)) {
        builder.thumbnailImage(is);
      }
      catch (IOException ex) {
        throw new GeneratorException("Unable to set package thumbnail to default " + THUMBNAIL_IMAGE_DEFAULT, ex);
      }
    }

    getFilters(options).forEach(builder::filter);

    return builder;
  }

  /**
   * Merges description and file header to a single string.
   * @param description Description from configuration - may be null
   * @param fileHeader File header from file - may be null
   * @return Merged description or null if all input is null
   */
  private static String mergeDescriptionFileHeader(String description, FileHeaderContext fileHeader) {
    boolean hasDescription = StringUtils.isNotBlank(description);
    boolean hasFileHeader = fileHeader != null && !fileHeader.getCommentLines().isEmpty();

    if (!hasDescription && !hasFileHeader) {
      return null;
    }

    StringBuilder result = new StringBuilder();

    if (hasDescription) {
      result.append(description);
    }

    if (hasDescription && hasFileHeader) {
      result.append("\n---\n");
    }

    if (hasFileHeader) {
      @SuppressWarnings("null")
      String fileHeaderString = StringUtils.trim(fileHeader.getCommentLines().stream()
          .filter(line -> !StringUtils.contains(line, "*****"))
          .map(line -> StringUtils.trim(line))
          .collect(Collectors.joining("\n")));
      result.append(fileHeaderString);
    }

    return result.toString();
  }

  /**
   * Get filter definitions either from contentPackageRootPath or from contentPackageFilters property.
   * @param options Options
   * @return Filters list
   */
  private static List<PackageFilter> getFilters(Map<String, Object> options) {
    List<PackageFilter> filters = new ArrayList<>();

    String rootPath = getMandatoryProp(options, PROPERTY_PACKAGE_ROOT_PATH);
    List<Map<String, Object>> filterDefinitions = getOptionalPropMapList(options, PROPERTY_PACKAGE_FILTERS);

    if (filterDefinitions != null) {
      for (Map<String, Object> filterDefinition : filterDefinitions) {
        String filterRoot = getMandatoryProp(filterDefinition, "filter");
        PackageFilter filter = new PackageFilter(filterRoot);
        filters.add(filter);

        List<Map<String, Object>> ruleDefinitions = getOptionalPropMapList(filterDefinition, "rules");
        if (ruleDefinitions != null) {
          for (Map<String, Object> ruleDefinition : ruleDefinitions) {
            String rule = getMandatoryProp(ruleDefinition, "rule");
            String pattern = getMandatoryProp(ruleDefinition, "pattern");
            if (StringUtils.equals(rule, "include")) {
              filter.addIncludeRule(pattern);
            }
            else if (StringUtils.equals(rule, "exclude")) {
              filter.addExcludeRule(pattern);
            }
            else {
              throw new GeneratorException("Invalude rule '" + rule + "' in post processor options.");
            }
          }
        }
      }
    }
    else if (rootPath != null) {
      filters.add(new PackageFilter(rootPath));
    }

    if (filters.isEmpty()) {
      throw new GeneratorException("No content package filters defines in post processor options.");
    }

    return filters;
  }

  /**
   * Get and validate AC handling value.
   * @param options
   * @return AC handling value or null if not set.
   */
  private static AcHandling getAcHandling(Map<String, Object> options) {
    String acHandlingString = getOptionalProp(options, PROPERTY_PACKAGE_AC_HANDLING);
    if (StringUtils.isBlank(acHandlingString)) {
      return null;
    }
    for (AcHandling acHandling : AcHandling.values()) {
      if (StringUtils.equals(acHandling.getMode(), acHandlingString)) {
        return acHandling;
      }
    }
    throw new GeneratorException("Invalid content package acHandling value: " + acHandlingString);
  }


  /**
   * Get property from options and throw exception if it is not set.
   * @param options Options
   * @param key Key
   * @return Option value
   */
  public static String getMandatoryProp(Map<String, Object> options, String key) {
    Object value = MapExpander.getDeep(options, key);
    if (value instanceof String) {
      return (String)value;
    }
    throw new GeneratorException("Missing post processor option '" + key + "'.");
  }

  /**
   * Get property from options and returns null if not set.
   * @param options Options
   * @param key Key
   * @return Option value or null
   */
  public static String getOptionalProp(Map<String, Object> options, String key) {
    Object value = MapExpander.getDeep(options, key);
    if (value instanceof String) {
      return (String)value;
    }
    return null;
  }

  /**
   * Get property from options and throw exception if it is not set.
   * @param options Options
   * @param key Key
   * @return Option value
   */
  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> getOptionalPropMapList(Map<String, Object> options, String key) {
    Object value = MapExpander.getDeep(options, key);
    if (value instanceof List || value == null) {
      return (List<Map<String, Object>>)value;
    }
    throw new GeneratorException("Missing post processor option '" + key + "'.");
  }

}
