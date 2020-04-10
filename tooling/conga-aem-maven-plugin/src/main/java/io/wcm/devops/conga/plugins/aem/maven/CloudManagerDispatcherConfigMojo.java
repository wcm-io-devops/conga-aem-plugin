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
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

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

  @Component(role = Archiver.class, hint = "zip")
  private ZipArchiver zipArchiver;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }

    File environmentDir = getEnvironmentDir();
    List<File> nodeDirs = getNodeDirs(environmentDir);
    ModelParser modelParser = new ModelParser();
    for (File nodeDir : nodeDirs) {
      if (modelParser.hasRole(nodeDir, ROLE_AEM_DISPATCHER_CLOUD)) {
        buildDispatcherConfig(nodeDir);
      }
    }
  }

  private void buildDispatcherConfig(File nodeDir) throws MojoFailureException {
    File targetFile = new File(getTargetDir(), nodeDir.getName() + ".dispatcher-config.zip");

    zipArchiver.setDestFile(targetFile);
    String[] includes = new String[] { "**/*" };
    String[] excludes = new String[] { ModelParser.MODEL_FILE };
    zipArchiver.addDirectory(nodeDir, includes, excludes);
    try {
      zipArchiver.createArchive();
    }
    catch (ArchiverException | IOException ex) {
      throw new MojoFailureException("Unable to build file " + targetFile.getPath() + ": " + ex.getMessage(), ex);
    }
  }

}
