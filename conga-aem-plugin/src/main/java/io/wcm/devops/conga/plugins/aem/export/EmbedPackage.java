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
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;

import java.util.Map;
import java.util.Set;

import io.wcm.devops.conga.generator.spi.export.context.ExportNodeRoleData;
import io.wcm.devops.conga.generator.spi.export.context.GeneratedFileContext;
import io.wcm.devops.conga.model.util.MapExpander;
import io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackagePropertiesPostProcessor;

class EmbedPackage {

  private final GeneratedFileContext file;
  private final String packageType;
  private final Set<String> runModes;

  EmbedPackage(GeneratedFileContext file, ExportNodeRoleData role) {
    this.file = file;
    this.packageType = getPackageType(file);
    this.runModes = RunModeUtil.mapVariantsToRunModes(role.getRoleVariants());
  }

  private static String getPackageType(GeneratedFileContext file) {
    Map<String, Object> modelOptions = file.getFileContext().getModelOptions();
    if (modelOptions != null) {
      String key = ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY + "." + NAME_PACKAGE_TYPE;
      return (String)MapExpander.getDeep(modelOptions, key);
    }
    return null;
  }

  public GeneratedFileContext getFile() {
    return this.file;
  }

  public String getPackageType() {
    return this.packageType;
  }

  public boolean isValid() {
    // accept only files that are content packages and have a packageType set
    return this.packageType != null;
  }

  public boolean isOnlyAuthorRunMode() {
    return runModes.contains(RUNMODE_AUTHOR) && !runModes.contains(RUNMODE_PUBLISH);
  }

  public boolean isOnlyPublishRunMode() {
    return runModes.contains(RUNMODE_PUBLISH) && !runModes.contains(RUNMODE_AUTHOR);
  }

}
