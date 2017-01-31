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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.plugins.postprocessor.AbstractPostProcessor;
import io.wcm.devops.conga.generator.spi.ImplicitApplyOptions;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageUtil;

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

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, PostProcessorContext context) {
    return FileUtil.matchesExtension(file, FILE_EXTENSION);
  }

  @Override
  public ImplicitApplyOptions implicitApply(FileContext file, PostProcessorContext context) {
    return ImplicitApplyOptions.ALWAYS;
  }

  @Override
  public List<FileContext> apply(FileContext fileContext, PostProcessorContext context) {
    Logger logger = context.getLogger();

    try {
      Map<String, Object> properties = ContentPackageUtil.getPackageProperties(fileContext.getFile());
      if (!properties.isEmpty()) {
        fileContext.getModelOptions().put(MODEL_OPTIONS_PROPERTY, properties);
        logger.info("Extracted properties from AEM content package.");
      }
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to extract properties from AEM package " + fileContext.getCanonicalPath(), ex);
    }

    return ImmutableList.of();
  }

}
