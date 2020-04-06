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
package io.wcm.devops.conga.plugins.aem.export;

import static io.wcm.devops.conga.plugins.aem.export.RunModeUtil.RUNMODE_AUTHOR;
import static io.wcm.devops.conga.plugins.aem.export.RunModeUtil.RUNMODE_PUBLISH;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_NAME;
import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions.PROPERTY_PACKAGE_ROOT_PATH;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.spi.export.NodeModelExportPlugin;
import io.wcm.devops.conga.generator.spi.export.context.ExportNodeRoleData;
import io.wcm.devops.conga.generator.spi.export.context.NodeModelExportContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.model.util.MapMerger;
import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackageOptions;
import io.wcm.devops.conga.plugins.aem.util.ContentPackageUtil;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;

/**
 * Export a combined "all" package including all content packages generated for this CONGA node.
 * This is useful for deploying to AEM cloud service.
 */
public final class ContentPackageNodeModelExport implements NodeModelExportPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "aem-contentpackage";

  private static final String PACKAGE_NAME = "aem-all-packages";
  static final String MODEL_FILE = PACKAGE_NAME + ".zip";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void export(NodeModelExportContext context) {

    // collect AEM content packages for this node
    List<EmbedPackage> contentPackages = context.getRoleData().stream()
        .flatMap(role -> role.getFiles().stream().map(file -> new EmbedPackage(file, role)))
        .filter(EmbedPackage::isValid)
        .collect(Collectors.toList());
    if (contentPackages.isEmpty()) {
      return;
    }

    // build merged config from all roles and environment to pick best-matching content package properties for all package
    Map<String, Object> mergedConfig = new HashMap<>();
    mergedConfig.put(PROPERTY_PACKAGE_NAME, PACKAGE_NAME);
    mergedConfig = MapMerger.merge(context.getEnvironment().getConfig(), mergedConfig);
    for (ExportNodeRoleData role : context.getRoleData()) {
      mergedConfig = MapMerger.merge(mergedConfig, role.getConfig());
    }

    // define root path for "all" package
    String rootPath = buildRootPath(mergedConfig);
    mergedConfig.put(PROPERTY_PACKAGE_ROOT_PATH, rootPath);

    // build content package
    File zipFile = new File(context.getNodeDir(), MODEL_FILE);
    ContentPackageBuilder builder = ContentPackageUtil.getContentPackageBuilder(mergedConfig, context.getUrlFileManager());
    try (ContentPackage contentPackage = builder.build(zipFile)) {
      for (EmbedPackage pkg : contentPackages) {
        String path = buildPackagePath(pkg, rootPath);
        contentPackage.addFile(path, pkg.getFile().getFileContext().getFile());
      }
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to generate AEM content package: " + FileUtil.getCanonicalPath(zipFile), ex);
    }

  }

  /**
   * Build root path to be used for embedded package.
   * @param mergedConfig Merged configuration
   * @return Package path
   */
  private static String buildRootPath(Map<String, Object> mergedConfig) {
    String groupName = ContentPackageUtil.getMandatoryProp(mergedConfig, ContentPackageOptions.PROPERTY_PACKAGE_GROUP);
    return "/apps/" + groupName + "-packages";
  }

  /**
   * Build path to be used for embedded package.
   * @param pkg Package
   * @param rootPath Root path
   * @return Package path
   */
  private static String buildPackagePath(EmbedPackage pkg, String rootPath) {
    String runModeSuffix = "";
    if (pkg.isOnlyAuthorRunMode()) {
      runModeSuffix = "." + RUNMODE_AUTHOR;
    }
    else if (pkg.isOnlyPublishRunMode()) {
      runModeSuffix = "." + RUNMODE_PUBLISH;
    }
    return rootPath + "/" + pkg.getPackageType() + "/install" + runModeSuffix + "/"
        + pkg.getFile().getFileContext().getFile().getName();
  }

}
