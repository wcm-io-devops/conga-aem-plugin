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

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_GROUP;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.getMandatoryProp;
import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.plugins.aem.util.JsonContentLoader;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

/**
 * Transforms a JSON file describing a JCR content structure to an AEM content package
 * to be deployed via CRX package manager.
 */
public class ContentPackagePostProcessor implements PostProcessorPlugin {

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
  public List<FileContext> apply(FileContext fileContext, PostProcessorContext context) {
    File file = fileContext.getFile();
    Logger logger = context.getLogger();
    Map<String, Object> options = context.getOptions();

    try {

      // create AEM content package with configurations
      File zipFile = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".zip");
      logger.info("Generate " + zipFile.getCanonicalPath());

      ContentPackageBuilder builder = new ContentPackageBuilder()
      .rootPath(getMandatoryProp(options, PROPERTY_PACKAGE_ROOT_PATH))
      .group(getMandatoryProp(options, PROPERTY_PACKAGE_GROUP))
      .name(getMandatoryProp(options, PROPERTY_PACKAGE_NAME));

      try (ContentPackage contentPackage = builder.build(zipFile)) {
        Map<String, Object> content = jsonContentLoader.load(fileContext.getFile());
        contentPackage.addContent(contentPackage.getRootPath(), content);
      }

      // delete provisioning file after transformation
      file.delete();

      return ImmutableList.of(new FileContext().file(zipFile));
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to post-process JSON data file: " + FileUtil.getCanonicalPath(file), ex);
    }
  }

}
