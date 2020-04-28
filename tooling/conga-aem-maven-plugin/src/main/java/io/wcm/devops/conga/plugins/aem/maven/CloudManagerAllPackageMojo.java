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
package io.wcm.devops.conga.plugins.aem.maven;

import static io.wcm.devops.conga.generator.util.FileUtil.getCanonicalPath;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.wcm.devops.conga.plugins.aem.maven.allpackage.AllPackageBuilder;
import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

/**
 * Builds an "all" content package dedicated for deployment via Adobe Cloud Manager
 * for the given environment and node(s).
 */
@Mojo(name = "cloudmanager-all-package", threadSafe = true)
public final class CloudManagerAllPackageMojo extends AbstractCloudManagerMojo {

  /**
   * Package name for the "all" content package.
   */
  @Parameter(property = "conga.cloudManager.allPackage.name", defaultValue = "all")
  private String name;

  /**
   * Group name for the "all" content package.
   */
  @Parameter(property = "conga.cloudManager.allPackage.group", required = true)
  private String group;

  /**
   * Automatically generate dependencies between content packages based on file order in CONGA configuration.
   */
  @Parameter(property = "conga.cloudManager.allPackage.autoDependencies", defaultValue = "true")
  private boolean autoDependencies;

  /**
   * Use separate dependency chains for mutable and immutable packages.
   */
  @Parameter(property = "conga.cloudManager.allPackage.autoDependenciesSeparateMutable", defaultValue = "false")
  private boolean autoDependenciesSeparateMutable;

  /**
   * Specifies additional properties to be set in the properties.xml file.
   */
  @Parameter
  private Map<String, String> properties;

  /**
   * Set this to "true" to skip installing packages to CRX although configured in the POM.
   */
  @Parameter(property = "conga.cloudManager.allPackage.skip", defaultValue = "false")
  private boolean skip;

  private static final String CLOUDMANAGER_TARGET_NONE = "none";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }

    List<File> environmentDirs = getEnvironmentDir();
    for (File environmentDir : environmentDirs) {
      List<File> nodeDirs = getNodeDirs(environmentDir);
      ModelParser modelParser = new ModelParser();
      for (File nodeDir : nodeDirs) {
        Set<String> cloudManagerTarget = modelParser.getCloudManagerTarget(nodeDir);
        if (!cloudManagerTarget.contains(CLOUDMANAGER_TARGET_NONE)) {
          buildAllPackage(environmentDir, nodeDir, cloudManagerTarget, modelParser);
        }
      }
    }
  }

  private void buildAllPackage(File environmentDir, File nodeDir, Set<String> cloudManagerTarget,
      ModelParser modelParser) throws MojoExecutionException {
    String groupName = this.group;
    String packageName = environmentDir.getName() + "." + nodeDir.getName() + "." + this.name;

    List<ContentPackageFile> contentPackages = modelParser.getContentPackagesForNode(nodeDir);
    File targetFile = new File(getTargetDir(), packageName + ".zip");

    AllPackageBuilder builder = new AllPackageBuilder(targetFile, groupName, packageName)
        .autoDependencies(this.autoDependencies)
        .autoDependenciesSeparateMutable(this.autoDependenciesSeparateMutable)
        .logger(getLog());

    try {
      if (builder.build(contentPackages, cloudManagerTarget, properties)) {
        getLog().info("Generated " + getCanonicalPath(targetFile));
      }
      else {
        getLog().debug("Skipped " + getCanonicalPath(targetFile) + " - no valid package.");
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Unable to generate " + getCanonicalPath(targetFile), ex);
    }
  }

}
