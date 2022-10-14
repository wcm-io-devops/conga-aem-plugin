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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import io.wcm.devops.conga.plugins.aem.maven.allpackage.AllPackageBuilder;
import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ModelParser;

/**
 * Builds an "all" content package dedicated for deployment via Adobe Cloud Manager
 * for the given environment and node(s).
 * <p>
 * By default, it builds one "all" package per environment and node without adding any Cloud Manager
 * environment-specific run mode suffixes to the folders. By defining a parameter <code>cloudManager.target</code>
 * (contains a list of string values) in the CONGA environment it is possible:
 * </p>
 * <ul>
 * <li>If it contains <code>none</code> no "all" package is build.</li>
 * <li>If set to one or multiple environment names (normally dev/stage/prod) one "all" package for each of
 * them is defined, and the environment name is added as runmode suffix to all config and install folders.</li>
 * </ul>
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
   * Build one single content package for all environments and nodes.
   */
  @Parameter(property = "conga.cloudManager.allPackage.singlePackage", defaultValue = "false")
  private boolean singlePackage;

  /**
   * Attach "all" content package(s) as artifacts to maven build lifecycle.
   * The given package name will be used as classifier.
   */
  @Parameter(property = "conga.cloudManager.allPackage.attachArtifact", defaultValue = "false")
  private boolean attachArtifact;

  /**
   * Automatically generate dependencies between content packages based on file order in CONGA configuration.
   * <p>
   * Possible values:
   * </p>
   * <ul>
   * <li><code>IMMUTABLE_MUTABLE_COMBINED</code>: Generate a single dependency chain spanning both immutable and mutable
   * content packages.</li>
   * <li><code>IMMUTABLE_MUTABLE_SEPARATE</code>: Generate separate dependency chains for immutable and mutable content
   * packages.</li>
   * <li><code>IMMUTABLE_ONLY</code>: Generate a dependency chain only for immutable content packages.</li>
   * <li><code>OFF</code>: Do not generate dependencies between content packages.</li>
   * </ul>
   */
  @Parameter(property = "conga.cloudManager.allPackage.autoDependenciesMode")
  private AutoDependenciesMode autoDependenciesMode;

  /**
   * How to optimize author/publish run modes in resulting "all" package.
   * <p>
   * Possible values:
   * </p>
   * <ul>
   * <li><code>OFF</code>: No optimization takes place. Content packages and bundles are duplicated for author/publish
   * run modes to ensure strict following of dependency chain defined in CONGA.</li>
   * <li><code>ELIMINATE_DUPLICATES</code>: Eliminates duplicates to ensure that content packages and bundles that are
   * installed on both author and publish instances are contained only once in the "all" package.</li>
   * </ul>
   */
  @Parameter(property = "conga.cloudManager.allPackage.runModeOptimization", defaultValue = "OFF")
  private RunModeOptimization runModeOptimization;

  /**
   * How to validate package types to be included in "all" package.
   * <p>
   * Possible values:
   * </p>
   * <ul>
   * <li><code>STRICT</code>: Strict mode: Ignores packages without content type (with warning), fails build if "mixed"
   * package types are used. For AEMaaCS it is mandatory to use this mode.</li>
   * <li><code>WARN</code>: Includes all packages, but generated warnings about packages without content type or "mixed"
   * package types.</li>
   * </ul>
   */
  @Parameter(property = "conga.cloudManager.allPackage.packageTypeValidation", defaultValue = "STRICT")
  private PackageTypeValidation packageTypeValidation;

  /**
   * Automatically generate dependencies between content packages based on file order in CONGA configuration.
   * @deprecated Please use autoDependenciesMode instead.
   */
  @Deprecated
  @Parameter(property = "conga.cloudManager.allPackage.autoDependencies", defaultValue = "true")
  private boolean autoDependencies;

  /**
   * Use separate dependency chains for mutable and immutable packages.
   * @deprecated Please use autoDependenciesMode instead.
   */
  @Deprecated
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

  @Parameter(defaultValue = "${project.build.outputTimestamp}")
  private String outputTimestamp;

  @Parameter(readonly = true, defaultValue = "${project}")
  private MavenProject project;
  @Component
  private MavenProjectHelper projectHelper;

  private static final String CLOUDMANAGER_TARGET_NONE = "none";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      return;
    }

    if (this.autoDependenciesMode == null) {
      if (this.autoDependencies) {
        if (this.autoDependenciesSeparateMutable) {
          this.autoDependenciesMode = AutoDependenciesMode.IMMUTABLE_MUTABLE_SEPARATE;
        }
        else {
          this.autoDependenciesMode = AutoDependenciesMode.IMMUTABLE_MUTABLE_COMBINED;
        }
      }
      else {
        this.autoDependenciesMode = AutoDependenciesMode.OFF;
      }
    }

    if (singlePackage) {
      buildSingleAllPackage();
    }
    else if (runModeOptimization == RunModeOptimization.ELIMINATE_DUPLICATES) {
      buildAllPackagesPerEnvironment();
    }
    else {
      buildAllPackagesPerEnvironmentAndNode();
    }
  }

  /**
   * Build an "all" package for each environment and node.
   */
  private void buildAllPackagesPerEnvironmentAndNode() throws MojoExecutionException, MojoFailureException {
    visitEnvironmentsNodes((environmentDir, nodeDir, cloudManagerTarget, files) -> {
      String packageName = environmentDir.getName() + "." + nodeDir.getName() + "." + this.name;
      AllPackageBuilder builder = createBuilder(packageName);
      try {
        builder.add(files, cloudManagerTarget);
      }
      catch (IllegalArgumentException ex) {
        throw new MojoFailureException(ex.getMessage(), ex);
      }
      buildAllPackage(builder);
    });
  }

  /**
   * Build an "all" package for each environment, including all nodes of that environment in a single file.
   */
  private void buildAllPackagesPerEnvironment() throws MojoExecutionException, MojoFailureException {
    SortedMap<String, AllPackageBuilder> builderPerEnvironment = new TreeMap<>();
    visitEnvironmentsNodes((environmentDir, nodeDir, cloudManagerTarget, files) -> {
      String packageName = environmentDir.getName() + "." + this.name;
      AllPackageBuilder builder = builderPerEnvironment.computeIfAbsent(packageName, this::createBuilder);
      try {
        builder.add(files, cloudManagerTarget);
      }
      catch (IllegalArgumentException ex) {
        throw new MojoFailureException(ex.getMessage(), ex);
      }
    });
    for (AllPackageBuilder builder : builderPerEnvironment.values()) {
      buildAllPackage(builder);
    }
  }

  /**
   * Build a single "all" package containing packages from all environments and nodes.
   */
  private void buildSingleAllPackage() throws MojoExecutionException, MojoFailureException {
    String packageName = this.name;
    AllPackageBuilder builder = createBuilder(packageName);
    visitEnvironmentsNodes((environmentDir, nodeDir, cloudManagerTarget, files) -> {
      try {
        builder.add(files, cloudManagerTarget);
      }
      catch (IllegalArgumentException ex) {
        throw new MojoFailureException(ex.getMessage(), ex);
      }
    });
    buildAllPackage(builder);
  }

  private AllPackageBuilder createBuilder(String packageName) {
    String fileName;
    if (attachArtifact) {
      fileName = project.getArtifactId() + "." + packageName + "-" + project.getVersion() + ".zip";
    }
    else {
      fileName = packageName + ".zip";
    }
    File targetFile = new File(getTargetDir(), fileName);
    return new AllPackageBuilder(targetFile, this.group, packageName)
        .version(project.getVersion())
        .autoDependenciesMode(this.autoDependenciesMode)
        .runModeOptimization(this.runModeOptimization)
        .packageTypeValidation(this.packageTypeValidation)
        .logger(getLog())
        .buildOutputTimestamp(new BuildOutputTimestamp(outputTimestamp));
  }

  private void buildAllPackage(AllPackageBuilder builder) throws MojoExecutionException {
    try {
      getLog().debug("Start generating " + getCanonicalPath(builder.getTargetFile()) + "...");
      if (builder.build(properties)) {
        getLog().info("Generated " + getCanonicalPath(builder.getTargetFile()));
        if (attachArtifact) {
          projectHelper.attachArtifact(this.project, "zip", builder.getPackageName(), builder.getTargetFile());
        }
      }
      else {
        getLog().debug("Skipped " + getCanonicalPath(builder.getTargetFile()) + " - no valid package.");
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Unable to generate " + getCanonicalPath(builder.getTargetFile()), ex);
    }
  }

  private void visitEnvironmentsNodes(EnvironmentNodeVisitor visitor) throws MojoExecutionException, MojoFailureException {
    List<File> environmentDirs = getEnvironmentDir();
    for (File environmentDir : environmentDirs) {
      List<File> nodeDirs = getNodeDirs(environmentDir);
      for (File nodeDir : nodeDirs) {
        ModelParser modelParser = new ModelParser(nodeDir);
        Set<String> cloudManagerTarget = modelParser.getCloudManagerTarget();

        boolean validNodeForAllPackage = false;
        if (cloudManagerTarget.contains(CLOUDMANAGER_TARGET_NONE)) {
          if (isEnvironmentConfiguredExplicitely(environmentDir.getName())) {
            // cloud manager target is set to "none" - but environment is configured explicitly, so include it
            validNodeForAllPackage = true;
            cloudManagerTarget.remove(CLOUDMANAGER_TARGET_NONE);
          }
        }
        else {
          // cloud manager target is not set to "none" - include node
          validNodeForAllPackage = true;
        }

        if (validNodeForAllPackage) {
          List<InstallableFile> files = modelParser.getInstallableFilesForNode();
          visitor.visit(environmentDir, nodeDir, cloudManagerTarget, files);
        }
      }
    }
  }

  interface EnvironmentNodeVisitor {
    void visit(File environmentDir, File nodeDir, Set<String> cloudManagerTarget,
        List<InstallableFile> files) throws MojoExecutionException, MojoFailureException;
  }

}
