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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.collect.ImmutableSet;

/**
 * Common functionality for mojos that generate configuration ZIP files for Adobe Cloud Manager.
 */
abstract class AbstractCloudManagerMojo extends AbstractMojo {

  /**
   * Selected environments to generate.
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
   * @return Target directory
   */
  protected File getTargetDir() {
    // create directory if it does not exist already
    if (!target.exists() && !target.mkdirs()) {
      throw new IllegalStateException("Unable to create target dir: " + getCanonicalPath(target));
    }
    return target;
  }

  /**
   * Get directory of the selected environment. It has to be exactly one matching environment.
   * @return Environment directory
   * @throws MojoExecutionException if no or multiple directories found
   */
  protected List<File> getEnvironmentDir() throws MojoExecutionException {
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
    return directories;
  }

  /**
   * Checks if the given environment was configured explicitly in plugin configuration.
   * @param environment Environment name
   * @return true if configured explicitly
   */
  protected boolean isEnvironmentConfiguredExplicitely(String environment) {
    Set<String> selectedEnvironments = toSet(this.environments);
    return selectedEnvironments.contains(environment);
  }

  /**
   * Get matching node directories from environment.
   * @param environmentDir Environment directory
   * @return List of directories
   */
  protected List<File> getNodeDirs(File environmentDir) {
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

  private static Set<String> toSet(String[] values) {
    if (values != null) {
      return ImmutableSet.copyOf(values);
    }
    else {
      return ImmutableSet.of();
    }
  }

}
