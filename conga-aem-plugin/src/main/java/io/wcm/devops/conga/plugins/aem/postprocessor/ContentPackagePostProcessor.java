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
package io.wcm.devops.conga.plugins.aem.postprocessor;

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.plugins.postprocessor.AbstractPostProcessor;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.FileHeaderContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageBinaryFile;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageUtil;
import io.wcm.devops.conga.plugins.aem.util.JsonContentLoader;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Transforms a JSON file describing a JCR content structure to an AEM content package
 * to be deployed via CRX package manager.
 */
public class ContentPackagePostProcessor extends AbstractPostProcessor {

  /**
   * Plugin name
   */
  public static final String NAME = "aem-contentpackage";

  private static final String FILE_EXTENSION = "json";

  private final JsonContentLoader jsonContentLoader = new JsonContentLoader();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, PostProcessorContext context) {
    return FileUtil.matchesExtension(file, FILE_EXTENSION);
  }

  @Override
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public List<FileContext> apply(FileContext fileContext, PostProcessorContext context) {
    File file = fileContext.getFile();
    Logger logger = context.getLogger();
    Map<String, Object> options = context.getOptions();

    try {
      // extract file header
      FileHeaderContext fileHeader = extractFileHeader(fileContext, context);

      // create AEM content package with configurations
      File zipFile = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".zip");
      logger.info("Generate {}", zipFile.getCanonicalPath());

      String rootPath = ContentPackageUtil.getMandatoryProp(options, PROPERTY_PACKAGE_ROOT_PATH);

      ContentPackageBuilder builder = ContentPackageUtil.getContentPackageBuilder(options, context.getUrlFileManager(), fileHeader);
      try (ContentPackage contentPackage = builder.build(zipFile)) {

        // add content from JSON file
        ContentElement content = jsonContentLoader.load(fileContext.getFile());
        contentPackage.addContent(rootPath, content);

        // add additional binary files
        for (ContentPackageBinaryFile binaryFile : ContentPackageUtil.getFiles(options)) {
          String path = binaryFile.getPath();
          try (InputStream is = binaryFile.getInputStream(context.getUrlFileManager(), fileContext.getTargetDir())) {
            contentPackage.addFile(path, is);
          }
          binaryFile.deleteIfRequired(context.getUrlFileManager(), fileContext.getTargetDir());
        }

      }

      // delete provisioning file after transformation
      file.delete();

      // set force to true by default for CONGA-generated packages (but allow override from role definition)
      Map<String, Object> modelOptions = new HashMap<>();
      modelOptions.put("force", true);
      modelOptions.putAll(fileContext.getModelOptions());

      return ImmutableList.of(new FileContext().file(zipFile).modelOptions(modelOptions));
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to post-process JSON data file: " + FileUtil.getCanonicalPath(file), ex);
    }
  }

}
