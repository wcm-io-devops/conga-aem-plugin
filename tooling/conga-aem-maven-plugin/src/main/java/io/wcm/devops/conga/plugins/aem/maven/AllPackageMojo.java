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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.collect.ImmutableSet;

import io.wcm.devops.conga.plugins.aem.maven.allpackage.AllPackageBuilder;
import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

/**
 * Builds an "all" content package dedicated for deployment via Adobe Cloud Manager
 * for the given environment and node(s).
 */
@Mojo(name = "all-package", threadSafe = true, requiresProject = false)
public final class AllPackageMojo extends AbstractMojo {

  /**
   * Package name for the "all" content package.
   */
  @Parameter(property = "conga.allPackage.name", defaultValue = "all")
  private String name;

  /**
   * Group name for the "all" content package.
   */
  @Parameter(property = "conga.allPackage.group", required = true)
  private String group;

  /**
   * Selected environments to generate. It's only allowed to define a single environment for this mojo,
   * but to be compatible with the other CONGA plugins it's uses the same semantic for defining multiple
   * environments.
   */
  @Parameter(property = "conga.environments")
  private String[] environments;

  /**
   * Selected nodes to generate.
   */
  @Parameter(property = "conga.nodes")
  private String[] nodes;

  /**
   * Path for the generated configuration files.
   */
  @Parameter(defaultValue = "${project.build.directory}/configuration")
  private File configurationDir;

  /**
   * Target path for the generated copied files.
   */
  @Parameter(defaultValue = "${project.build.directory}")
  private File target;

  /**
   * Set this to "true" to skip installing packages to CRX although configured in the POM.
   */
  @Parameter(property = "conga.allPackage.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }

    File environmentDir = getEnvironmentDir();
    List<File> nodeDirs = getNodeDirs(environmentDir);
    ModelParser modelParser = new ModelParser();
    for (File nodeDir : nodeDirs) {
      buildAllPackage(nodeDir, modelParser);
    }
  }

  /**
   * Get directory of the selected environment. It has to be exactly one matching environment.
   * @return Environment directory
   * @throw MojoExecutionException if no or multiple directories found
   */
  private File getEnvironmentDir() throws MojoExecutionException {
    List<File> directories = null;
    Set<String> selectedEnvironments = toSet(this.environments);
    if (configurationDir.exists() && configurationDir.isDirectory()) {
      File[] files = configurationDir.listFiles();
      if (files != null) {
        directories = Arrays.stream(files)
            .filter(File::isDirectory)
            .filter(dir -> (selectedEnvironments.isEmpty() || selectedEnvironments.contains(dir.getName())))
            .collect(Collectors.toList());
      }
    }
    if (directories == null || directories.isEmpty()) {
      throw new MojoExecutionException("No matching environment directory found in " + getCanonicalPath(configurationDir));
    }
    if (directories.size() > 1) {
      throw new MojoExecutionException("Multiple environments found in " + getCanonicalPath(configurationDir)
          + " - please specify a single environment via the 'environments' parameter.");
    }
    return directories.get(0);
  }

  /**
   * Get matching node directories from environment.
   * @param environmentDir Environment directory
   * @return List of directories
   */
  private List<File> getNodeDirs(File environmentDir) {
    Set<String> selectedNodes = toSet(this.nodes);
    File[] files = environmentDir.listFiles();
    if (files != null) {
      return Arrays.stream(files)
          .filter(File::isDirectory)
          .filter(dir -> selectedNodes.isEmpty() || selectedNodes.contains(dir.getName()))
          .collect(Collectors.toList());
    }
    else {
      return Collections.emptyList();
    }
  }

  private void buildAllPackage(File nodeDir, ModelParser modelParser) throws MojoFailureException {
    String groupName = this.group;
    String packageName = nodeDir.getName() + "-" + this.name;

    List<ContentPackageFile> contentPackages = modelParser.getContentPackagesForNode(nodeDir);
    File targetFile = new File(target, packageName + ".zip");
    try {
      if (AllPackageBuilder.build(targetFile, contentPackages, groupName, packageName)) {
        getLog().info("Generated " + getCanonicalPath(targetFile));
      }
      else {
        getLog().debug("Skipped " + getCanonicalPath(targetFile) + " - no valid package.");
      }
    }
    catch (IOException ex) {
      throw new MojoFailureException("Unable to generate " + getCanonicalPath(targetFile), ex);
    }
  }

  private static Set<String> toSet(String[] values) {
    if (values != null) {
      return ImmutableSet.copyOf(values);
    }
    else {
      return ImmutableSet.of();
    }
  }

}
