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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import com.google.inject.name.Named;

import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

/**
 * Builds an Dispatcher configuration ZIP file dedicated for deployment via Adobe Cloud Manager
 * for the given environment and node(s).
 * Only nodes with role <code>aem-dispatcher-cloud</code> are respected.
 */
@Mojo(name = "cloudmanager-dispatcher-config", threadSafe = true)
public final class CloudManagerDispatcherConfigMojo extends AbstractCloudManagerMojo {

  private static final String ROLE_AEM_DISPATCHER_CLOUD = "aem-dispatcher-cloud";

  /**
   * Set this to "true" to skip installing packages to CRX although configured in the POM.
   */
  @Parameter(property = "conga.cloudManager.dispatcherConfig.skip", defaultValue = "false")
  private boolean skip;

  @Inject
  @Named("zip")
  private ZipArchiver zipArchiver;

  @Parameter(defaultValue = "${project.build.outputTimestamp}")
  private String outputTimestamp;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }

    int dispatcherNodeCount = 0;
    List<File> environmentDirs = getEnvironmentDir();
    for (File environmentDir : environmentDirs) {
      List<File> nodeDirs = getNodeDirs(environmentDir);
      for (File nodeDir : nodeDirs) {
        ModelParser modelParser = new ModelParser(nodeDir);
        if (modelParser.hasRole(ROLE_AEM_DISPATCHER_CLOUD)) {
          buildDispatcherConfig(environmentDir, nodeDir);
          dispatcherNodeCount++;
        }
      }
    }

    if (dispatcherNodeCount > 1) {
      throw new MojoFailureException("More than one node with role '" + ROLE_AEM_DISPATCHER_CLOUD + "' found - "
          + "AEM Cloud service supports only a single dispatcher configuration.");
    }
  }

  private void buildDispatcherConfig(File environmentDir, File nodeDir) throws MojoExecutionException {
    File targetFile = new File(getTargetDir(), environmentDir.getName() + "." + nodeDir.getName() + ".dispatcher-config.zip");

    try {
      String basePath = toZipDirectoryPath(nodeDir);
      addZipDirectory(basePath, nodeDir, Collections.singleton(ModelParser.MODEL_FILE));
      zipArchiver.setDestFile(targetFile);

      BuildOutputTimestamp buildOutputTimestamp = new BuildOutputTimestamp(outputTimestamp);
      if (buildOutputTimestamp.isValid()) {
        zipArchiver.configureReproducibleBuild(buildOutputTimestamp.toFileTime());
      }

      zipArchiver.createArchive();
    }
    catch (ArchiverException | IOException ex) {
      throw new MojoExecutionException("Unable to build file " + targetFile.getPath() + ": " + ex.getMessage(), ex);
    }
  }

  /**
   * Recursive through all directory and add file to zipArchiver.
   * This method has special support for symlinks which are required for dispatcher configuration.
   * @param basePath Base path
   * @param directory Directory to include
   * @param excludeFiles Exclude filenames
   * @throws IOException I/O exception
   */
  @SuppressWarnings("java:S3776") // ignore complexity
  private void addZipDirectory(String basePath, File directory, Set<String> excludeFiles) throws IOException {
    String directoryPath = toZipDirectoryPath(directory);
    if (Strings.CS.startsWith(directoryPath, basePath)) {
      String relativeDirectoryPath = StringUtils.substring(directoryPath, basePath.length());
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (excludeFiles.contains(file.getName())) {
            continue;
          }
          if (file.isDirectory()) {
            addZipDirectory(basePath, file, Collections.emptySet());
          }
          else if (Files.isSymbolicLink(file.toPath())) {
            Path linkPath = file.toPath();
            Path targetPath = linkPath.toRealPath();
            Path symlinkPath = file.getParentFile().toPath().relativize(targetPath);
            zipArchiver.addSymlink(relativeDirectoryPath + file.getName(), sanitizePathSeparators(symlinkPath.toString()));
          }
          else {
            zipArchiver.addFile(file, relativeDirectoryPath + file.getName());
          }
        }
      }
    }
  }

  private String toZipDirectoryPath(File directory) {
    String canoncialPath = FileUtil.getCanonicalPath(directory);
    return sanitizePathSeparators(canoncialPath) + "/";
  }

  private String sanitizePathSeparators(String path) {
    return Strings.CS.replace(path, "\\", "/");
  }

}
