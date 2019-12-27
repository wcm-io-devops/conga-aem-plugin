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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.provisioning.model.Model;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.plugins.postprocessor.AbstractPostProcessor;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.FileHeaderContext;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageUtil;
import io.wcm.devops.conga.plugins.sling.util.ConfigConsumer;
import io.wcm.devops.conga.plugins.sling.util.OsgiConfigUtil;
import io.wcm.devops.conga.plugins.sling.util.ProvisioningUtil;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;

/**
 * Transforms a Sling Provisioning file into OSGi configurations (ignoring all other provisioning contents)
 * and then packages them up in an AEM content package to be deployed via CRX package manager.
 */
public class ContentPackageOsgiConfigPostProcessor extends AbstractPostProcessor {

  /**
   * Plugin name
   */
  public static final String NAME = "aem-contentpackage-osgiconfig";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, PostProcessorContext context) {
    return ProvisioningUtil.isProvisioningFile(file);
  }

  @Override
  public List<FileContext> apply(FileContext fileContext, PostProcessorContext context) {
    File file = fileContext.getFile();
    Logger logger = context.getLogger();
    Map<String, Object> options = context.getOptions();

    try {
      // extract file header
      FileHeaderContext fileHeader = extractFileHeader(fileContext, context);

      // generate OSGi configurations
      Model model = ProvisioningUtil.getModel(fileContext);

      // create AEM content package with configurations
      File zipFile = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".zip");
      logger.info("Generate " + zipFile.getCanonicalPath());

      String rootPath = ContentPackageUtil.getMandatoryProp(options, PROPERTY_PACKAGE_ROOT_PATH);

      ContentPackageBuilder builder = ContentPackageUtil.getContentPackageBuilder(options, fileHeader);

      try (ContentPackage contentPackage = builder.build(zipFile)) {

        // always create folder for root path
        contentPackage.addContent(rootPath, ImmutableMap.of("jcr:primaryType", "nt:folder"));

        generateOsgiConfigurations(model, contentPackage, rootPath, fileHeader, context);
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
      throw new GeneratorException("Unable to post-process sling provisioning OSGi configurations.", ex);
    }
  }

  /**
   * Generate OSGi configuration for all feature and run modes.
   * @param model Provisioning Model
   * @param contentPackage Content package
   * @param rootPath Root path
   * @param fileHeader File header
   * @param context Post processor context
   */
  private void generateOsgiConfigurations(Model model, ContentPackage contentPackage,
      String rootPath, FileHeaderContext fileHeader, PostProcessorContext context) throws IOException {
    ProvisioningUtil.visitOsgiConfigurations(model, new ConfigConsumer<Void>() {
      @Override
      public Void accept(String path, Dictionary<String, Object> properties) throws IOException {
        String contentPath = rootPath + (StringUtils.contains(path, "/") ? "." : "/") + path;
        context.getLogger().info("  Include " + contentPath);

        // write configuration to temporary file
        File tempFile = File.createTempFile(NAME, ".config");
        try (OutputStream os = new FileOutputStream(tempFile)) {
          OsgiConfigUtil.write(os, properties);
        }
        try {
          FileContext tempFileContext = new FileContext().file(tempFile).charset(StandardCharsets.UTF_8);

          // apply file header
          applyFileHeader(tempFileContext, fileHeader, context);

          // write configuration to content package
          try (InputStream is = new BufferedInputStream(new FileInputStream(tempFile))) {
            contentPackage.addFile(contentPath, is, "text/plain;charset=" + StandardCharsets.UTF_8.name());
          }
        }
        finally {
          // remove temporary file
          tempFile.delete();
        }
        return null;
      }
    });
  }

}
