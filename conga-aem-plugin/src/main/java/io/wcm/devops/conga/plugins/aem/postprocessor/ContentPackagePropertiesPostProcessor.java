/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_PACKAGE_TYPE;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.plugins.postprocessor.AbstractPostProcessor;
import io.wcm.devops.conga.generator.spi.ImplicitApplyOptions;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageUtil;
import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;

/**
 * Extracts properties from a given AEM content package and stores them as additional
 * model options in the file context.
 */
public class ContentPackagePropertiesPostProcessor extends AbstractPostProcessor {

  /**
   * Plugin name
   */
  public static final String NAME = "aem-contentpackage-properties";

  /**
   * Holds map containing the content package properties.
   */
  public static final String MODEL_OPTIONS_PROPERTY = "aemContentPackageProperties";

  private static final String FILE_EXTENSION = "zip";
  private static final String ALTERNATE_FILE_EXTENSION = "jar";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, PostProcessorContext context) {
    return FileUtil.matchesExtension(file, FILE_EXTENSION)
        || FileUtil.matchesExtension(file, ALTERNATE_FILE_EXTENSION);
  }

  @Override
  public ImplicitApplyOptions implicitApply(FileContext file, PostProcessorContext context) {
    if (FileUtil.matchesExtension(file, FILE_EXTENSION)) {
      return ImplicitApplyOptions.ALWAYS;
    }
    else {
      return ImplicitApplyOptions.NEVER;
    }
  }

  @Override
  public List<FileContext> apply(FileContext fileContext, PostProcessorContext context) {
    Logger logger = context.getLogger();

    try {
      Map<String, Object> properties = ContentPackageProperties.get(fileContext.getFile());
      if (!properties.isEmpty()) {

        // allow to redefine the packageType content package property via post processor options
        // this is useful when 3rdparty packages do not define a package type
        String packageType = ContentPackageUtil.getOptionalProp(context.getOptions(), PROPERTY_PACKAGE_PACKAGE_TYPE);
        if (StringUtils.isNotBlank(packageType)) {
          properties.put(NAME_PACKAGE_TYPE, packageType);
        }

        fileContext.getModelOptions().put(MODEL_OPTIONS_PROPERTY, properties);
        logger.info("Extracted properties from AEM content package.");
      }
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to extract properties from AEM package " + fileContext.getCanonicalPath(), ex);
    }

    return List.of();
  }

}
