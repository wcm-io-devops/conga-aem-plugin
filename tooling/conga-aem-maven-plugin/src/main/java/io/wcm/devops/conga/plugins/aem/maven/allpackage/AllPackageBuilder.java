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
package io.wcm.devops.conga.plugins.aem.maven.allpackage;

import static io.wcm.devops.conga.generator.util.FileUtil.getCanonicalPath;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.RUNMODE_AUTHOR;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.RUNMODE_PUBLISH;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.eliminateAuthorPublishDuplicates;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.isAuthorAndPublish;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.isOnlyAuthor;
import static io.wcm.devops.conga.plugins.aem.maven.allpackage.RunModeUtil.isOnlyPublish;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_DEPENDENCIES;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_NAME;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;
import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_VERSION;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.jackrabbit.vault.packaging.Dependency;
import org.apache.jackrabbit.vault.packaging.DependencyUtil;
import org.apache.jackrabbit.vault.packaging.PackageType;
import org.apache.jackrabbit.vault.packaging.VersionRange;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.devops.conga.plugins.aem.maven.AutoDependenciesMode;
import io.wcm.devops.conga.plugins.aem.maven.BuildOutputTimestamp;
import io.wcm.devops.conga.plugins.aem.maven.PackageTypeValidation;
import io.wcm.devops.conga.plugins.aem.maven.PackageVersionMode;
import io.wcm.devops.conga.plugins.aem.maven.RunModeOptimization;
import io.wcm.devops.conga.plugins.aem.maven.model.BundleFile;
import io.wcm.devops.conga.plugins.aem.maven.model.ContentPackageFile;
import io.wcm.devops.conga.plugins.aem.maven.model.InstallableFile;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder;
import io.wcm.tooling.commons.contentpackagebuilder.PackageFilter;

/**
 * Builds "all" package based on given set of content packages.
 *
 * <p>
 * General concept:
 * </p>
 *
 * <ul>
 * <li>Iterates through all content packages that are generated or collected by CONGA and contained in the
 * model.json</li>
 * <li>Enforces the order defined in CONGA by automatically adding dependencies to all packages reflecting the file
 * order in model.json</li>
 * <li>Because the dependency chain may be different for each runmode (author/publish), each package is added once for
 * each runmode. Internally this separate dependency change for author and publish is optimized to have each package
 * included only once for author+publish, unless it has a different chain of dependencies for both runmodes, in which
 * case it is included separately for each run mode.</li>
 * <li>To avoid conflicts with duplicate packages with different dependency chains the names of packages that are
 * included in different versions for author/publish are changed and a runmode suffix (.author or .publish) is added,
 * and it is put in a corresponding install folder.</li>
 * <li>To avoid problems with nested sub packages, the sub packages are extracted from the packages and treated in the
 * same way as other packages.</li>
 * </ul>
 */
public final class AllPackageBuilder {

  private final File targetFile;
  private final String groupName;
  private final String packageName;
  private String version;
  private AutoDependenciesMode autoDependenciesMode = AutoDependenciesMode.OFF;
  private RunModeOptimization runModeOptimization = RunModeOptimization.OFF;
  private PackageTypeValidation packageTypeValidation = PackageTypeValidation.STRICT;
  private PackageVersionMode packageVersionMode = PackageVersionMode.DEFAULT;
  private Log log;
  private BuildOutputTimestamp buildOutputTimestamp;

  private static final String RUNMODE_DEFAULT = "$default$";
  private static final Set<String> ALLOWED_PACKAGE_TYPES = Set.of(
      PackageType.APPLICATION.name().toLowerCase(),
      PackageType.CONTAINER.name().toLowerCase(),
      PackageType.CONTENT.name().toLowerCase());
  private static final String VERSION_SUFFIX_SEPARATOR = "-";

  private final List<ContentPackageFileSet> contentPackageFileSets = new ArrayList<>();
  private final List<BundleFileSet> bundleFileSets = new ArrayList<>();

  /**
   * Constructor.
   * @param targetFile Target file
   * @param groupName Group name
   * @param packageName Package name
   */
  public AllPackageBuilder(File targetFile, String groupName, String packageName) {
    this.targetFile = targetFile;
    this.groupName = groupName;
    this.packageName = packageName;
  }

  /**
   * Automatically generate dependencies between content packages based on file order in CONGA configuration.
   * @param value mode
   * @return this
   */
  public AllPackageBuilder autoDependenciesMode(AutoDependenciesMode value) {
    this.autoDependenciesMode = value;
    return this;
  }

  /**
   * Configure run mode optimization.
   * @param value mode
   * @return this
   */
  public AllPackageBuilder runModeOptimization(RunModeOptimization value) {
    this.runModeOptimization = value;
    return this;
  }

  /**
   * How to validate package types to be included in "all" package.
   * @param value validation
   * @return this
   */
  public AllPackageBuilder packageTypeValidation(PackageTypeValidation value) {
    this.packageTypeValidation = value;
    return this;
  }

  /**
   * How to handle versions of packages and sub-packages inside "all" package.
   * @param value mode
   * @return this
   */
  public AllPackageBuilder packageVersionMode(PackageVersionMode value) {
    this.packageVersionMode = value;
    return this;
  }

  /**
   * Maven logger
   * @param value Maven logger
   * @return this
   */
  public AllPackageBuilder logger(Log value) {
    this.log = value;
    return this;
  }

  /**
   * Package version
   * @param value Package version
   * @return this
   */
  public AllPackageBuilder version(String value) {
    this.version = value;
    return this;
  }

  /**
   * Build output timestamp
   * @param value timestamp
   * @return this
   */
  public AllPackageBuilder buildOutputTimestamp(BuildOutputTimestamp value) {
    this.buildOutputTimestamp = value;
    return this;
  }

  private Log getLog() {
    if (this.log == null) {
      this.log = new SystemStreamLog();
    }
    return this.log;
  }

  /**
   * Add content packages and OSGi bundles to be contained in "all" content package.
   * @param files Content packages (invalid will be filtered out) and OSGi bundles
   * @param cloudManagerTarget Target environments/run modes the packages should be attached to
   * @throws IllegalArgumentException If and invalid package type is detected
   */
  public void add(List<InstallableFile> files, Set<String> cloudManagerTarget) {
    List<ContentPackageFile> contentPackages = filterFiles(files, ContentPackageFile.class);

    // collect list of cloud manager environment run modes
    List<String> environmentRunModes = new ArrayList<>();
    if (cloudManagerTarget.isEmpty()) {
      environmentRunModes.add(RUNMODE_DEFAULT);
    }
    else {
      environmentRunModes.addAll(cloudManagerTarget);
    }

    List<ContentPackageFile> validContentPackages;
    switch (packageTypeValidation) {
      case STRICT:
        validContentPackages = getValidContentPackagesStrictValidation(contentPackages);
        break;
      case WARN:
        validContentPackages = getValidContentPackagesWarnValidation(contentPackages);
        break;
      default:
        throw new IllegalArgumentException("Unsupported package type validation: " + packageTypeValidation);
    }

    if (!validContentPackages.isEmpty()) {
      contentPackageFileSets.add(new ContentPackageFileSet(validContentPackages, environmentRunModes));
    }

    // add OSGi bundles
    List<BundleFile> bundles = filterFiles(files, BundleFile.class);
    if (!bundles.isEmpty()) {
      bundleFileSets.add(new BundleFileSet(bundles, environmentRunModes));
    }
  }

  /**
   * Get valid content packages in strict mode: Ignore content packages without package type (with warning),
   * fail build if Content package with "mixed" mode is found.
   * @param contentPackages Content packages
   * @return Valid content packages
   */
  private List<ContentPackageFile> getValidContentPackagesStrictValidation(List<? extends ContentPackageFile> contentPackages) {
    // generate warning for each content packages without package type that is skipped
    contentPackages.stream()
        .filter(pkg -> !hasPackageType(pkg))
        .forEach(pkg -> getLog().warn("Skipping content package without package type: " + getCanonicalPath(pkg.getFile())));

    // fail build if content packages with non-allowed package types exist
    List<ContentPackageFile> invalidPackageTypeContentPackages = contentPackages.stream()
        .filter(AllPackageBuilder::hasPackageType)
        .filter(pkg -> !isValidPackageType(pkg))
        .collect(Collectors.toList());
    if (!invalidPackageTypeContentPackages.isEmpty()) {
      throw new IllegalArgumentException("Content packages found with unsupported package types: " +
          invalidPackageTypeContentPackages.stream()
              .map(pkg -> pkg.getName() + " -> " + pkg.getPackageType())
              .collect(Collectors.joining(", ")));
    }

    // collect AEM content packages with package type
    return contentPackages.stream()
        .filter(AllPackageBuilder::hasPackageType)
        .collect(Collectors.toList());
  }

  /**
   * Get all content packages, generate warnings if package type is missing or "mixed" mode package type is used.
   * @param contentPackages Content packages
   * @return Valid content packages
   */
  private List<ContentPackageFile> getValidContentPackagesWarnValidation(List<? extends ContentPackageFile> contentPackages) {
    // generate warning for each content packages without package type
    contentPackages.stream()
        .filter(pkg -> !hasPackageType(pkg))
        .forEach(pkg -> getLog().warn("Found content package without package type: " + getCanonicalPath(pkg.getFile())));

    // generate warning for each content packages with invalid package type
    contentPackages.stream()
        .filter(AllPackageBuilder::hasPackageType)
        .filter(pkg -> !isValidPackageType(pkg))
        .forEach(pkg -> getLog().warn("Found content package with invalid package type: "
            + getCanonicalPath(pkg.getFile()) + " -> " + pkg.getPackageType()));

    // return all content packages
    return contentPackages.stream().collect(Collectors.toList());
  }

  private static <T> List<T> filterFiles(List<? extends InstallableFile> files, Class<T> fileClass) {
    return files.stream()
        .filter(fileClass::isInstance)
        .map(fileClass::cast)
        .toList();
  }

  /**
   * Build "all" content package.
   * @param properties Specifies additional properties to be set in the properties.xml file.
   * @return true if "all" package was generated, false if no valid package was found.
   * @throws IOException I/O exception
   */
  public boolean build(Map<String, String> properties) throws IOException {

    if (contentPackageFileSets.isEmpty()) {
      return false;
    }

    // prepare content package metadata
    ContentPackageBuilder builder = new ContentPackageBuilder()
        .group(groupName)
        .name(packageName)
        .packageType("container");
    if (version != null) {
      builder.version(version);
    }

    // define root path for "all" package
    String rootPath = buildRootPath(groupName, packageName);
    builder.filter(new PackageFilter(rootPath));

    // additional package properties
    if (properties != null) {
      properties.entrySet().forEach(entry -> builder.property(entry.getKey(), entry.getValue()));
    }

    // build content package
    try (ContentPackage contentPackage = builder.build(targetFile)) {
      buildAddContentPackages(contentPackage, rootPath);
      buildAddBundles(contentPackage, rootPath);
    }

    return true;
  }

  @SuppressWarnings("java:S3776") // ignore complexity
  private void buildAddContentPackages(ContentPackage contentPackage, String rootPath) throws IOException {
    // build set with dependencies instances for each package contained in all filesets
    Set<Dependency> allPackagesFromFileSets = new HashSet<>();
    for (ContentPackageFileSet fileSet : contentPackageFileSets) {
      for (ContentPackageFile pkg : fileSet.getFiles()) {
        addDependencyInformation(allPackagesFromFileSets, pkg);
      }
    }

    Collection<ContentPackageFileSet> processedFileSets;
    if (runModeOptimization == RunModeOptimization.ELIMINATE_DUPLICATES) {
      // eliminate duplicates which are same for author and publish
      processedFileSets = eliminateAuthorPublishDuplicates(contentPackageFileSets,
        environmentRunMode -> new ContentPackageFileSet(new ArrayList<>(), Collections.singletonList(environmentRunMode)));
    }
    else {
      processedFileSets = contentPackageFileSets;
    }

    for (ContentPackageFileSet fileSet : processedFileSets) {
      for (String environmentRunMode : fileSet.getEnvironmentRunModes()) {
        List<ContentPackageFile> previousPackages = new ArrayList<>();
        for (ContentPackageFile pkg : fileSet.getFiles()) {
          ContentPackageFile previousPkg = getDependencyChainPreviousPackage(pkg, previousPackages);

          // set package name, wire previous package in package dependency
          List<TemporaryContentPackageFile> processedFiles = processContentPackage(pkg, previousPkg, environmentRunMode, allPackagesFromFileSets);

          // add processed content packages to "all" content package - and delete the temporary files
          try {
            for (TemporaryContentPackageFile processedFile : processedFiles) {
              String path = buildPackagePath(processedFile, rootPath, environmentRunMode);
              contentPackage.addFile(path, processedFile.getFile());
              if (log.isDebugEnabled()) {
                log.debug("  Add " + processedFile.getPackageInfoWithDependencies());
              }
            }
          }
          finally {
            processedFiles.stream()
                .map(TemporaryContentPackageFile::getFile)
                .forEach(FileUtils::deleteQuietly);
          }

          previousPackages.add(pkg);
        }
      }
    }
  }

  /**
   * Gets the previous package in the order defined by CONGA to define as package dependency in current package.
   * @param currentPackage Current package
   * @param previousPackages List of previous packages
   * @return Package to define as dependency, or null if no dependency should be defined
   */
  private @Nullable ContentPackageFile getDependencyChainPreviousPackage(@NotNull ContentPackageFile currentPackage,
      @NotNull List<ContentPackageFile> previousPackages) {
    if ((autoDependenciesMode == AutoDependenciesMode.OFF)
        || (autoDependenciesMode == AutoDependenciesMode.IMMUTABLE_ONLY && isMutable(currentPackage))) {
      return null;
    }
    // get last previous package
    return previousPackages.stream()
        // if not IMMUTABLE_MUTABLE_COMBINED active only that of the same mutability type
        .filter(item -> (autoDependenciesMode == AutoDependenciesMode.IMMUTABLE_MUTABLE_COMBINED) || mutableMatches(item, currentPackage))
        // make sure author-only or publish-only packages are only taken into account if the current package has same restriction
        .filter(item -> isAuthorAndPublish(item)
            || (isOnlyAuthor(item) && isOnlyAuthor(currentPackage))
            || (isOnlyPublish(item) && isOnlyPublish(currentPackage)))
        // ignore packages that are marked as dependency chain ignore
        .filter(item -> !item.isDependencyChainIgnore())
        // get last in list
        .reduce((first, second) -> second).orElse(null);
  }

  private void buildAddBundles(ContentPackage contentPackage, String rootPath) throws IOException {
    Collection<BundleFileSet> processedFileSets;
    if (runModeOptimization == RunModeOptimization.ELIMINATE_DUPLICATES) {
      // eliminate duplicates which are same for author and publish
      processedFileSets = eliminateAuthorPublishDuplicates(bundleFileSets,
          environmentRunMode -> new BundleFileSet(new ArrayList<>(), Collections.singletonList(environmentRunMode)));
    }
    else {
      processedFileSets = bundleFileSets;
    }

    for (BundleFileSet bundleFileSet : processedFileSets) {
      for (String environmentRunMode : bundleFileSet.getEnvironmentRunModes()) {
        for (BundleFile bundleFile : bundleFileSet.getFiles()) {
          String path = buildBundlePath(bundleFile, rootPath, environmentRunMode);
          contentPackage.addFile(path, bundleFile.getFile());
        }
      }
    }
  }

  private static boolean hasPackageType(ContentPackageFile pkg) {
    // accept only content packages with a package type set
    return pkg.getPackageType() != null;
  }

  private static boolean isValidPackageType(ContentPackageFile pkg) {
    // check if the package type is an allowed package type
    return ALLOWED_PACKAGE_TYPES.contains(pkg.getPackageType());
  }

  private static boolean isMutable(ContentPackageFile pkg) {
    return Strings.CS.equals("content", pkg.getPackageType());
  }

  private static boolean mutableMatches(ContentPackageFile pkg1, ContentPackageFile pkg2) {
    if (pkg1 == null || pkg2 == null) {
      return false;
    }
    return isMutable(pkg1) == isMutable(pkg2);
  }

  /**
   * Build root path to be used for embedded package.
   * @param groupName Group name
   * @param packageName Package name
   * @return Package path
   */
  private static String buildRootPath(String groupName, String packageName) {
    return "/apps/" + groupName + "-" + packageName + "-packages";
  }

  /**
   * Generate suffix for instance and environment run modes.
   * @param file File
   * @return Suffix string
   */
  private static String buildRunModeSuffix(InstallableFile file, String environmentRunMode) {
    StringBuilder runModeSuffix = new StringBuilder();
    if (isOnlyAuthor(file)) {
      runModeSuffix.append(".").append(RUNMODE_AUTHOR);
    }
    else if (isOnlyPublish(file)) {
      runModeSuffix.append(".").append(RUNMODE_PUBLISH);
    }
    if (!Strings.CS.equals(environmentRunMode, RUNMODE_DEFAULT)) {
      runModeSuffix.append(".").append(environmentRunMode);
    }
    return runModeSuffix.toString();
  }

  /**
   * Generate suffix for versions of content packages.
   * @param pkg Content package
   * @param ignoreSnapshot Do not build version suffix for SNAPSHOT versions
   * @return Suffix string
   */
  private String buildVersionSuffix(ContentPackageFile pkg, boolean ignoreSnapshot) {
    StringBuilder versionSuffix = new StringBuilder();

    if (this.packageVersionMode == PackageVersionMode.RELEASE_SUFFIX_VERSION
        && (!ArtifactUtils.isSnapshot(pkg.getVersion()) || !ignoreSnapshot)
        && !Strings.CS.equals(pkg.getVersion(), this.version)
        && this.version != null) {
      versionSuffix.append(VERSION_SUFFIX_SEPARATOR)
          // replace dots with underlines in version suffix to avoid confusion with main version number
          .append(Strings.CS.replace(this.version, ".", "_"));
    }

    return versionSuffix.toString();
  }

  /**
   * Build path to be used for embedded package.
   * @param pkg Package
   * @param rootPath Root path
   * @return Package path
   */
  @SuppressWarnings("java:S1075") // no filesystem path
  private String buildPackagePath(ContentPackageFile pkg, String rootPath, String environmentRunMode) {
    if (packageTypeValidation == PackageTypeValidation.STRICT && !isValidPackageType(pkg)) {
      throw new IllegalArgumentException("Package " + pkg.getPackageInfo() + " has invalid package type: '" + pkg.getPackageType() + "'.");
    }

    String runModeSuffix = buildRunModeSuffix(pkg, environmentRunMode);

    // add run mode suffix to both install folder path and package file name
    String path = rootPath + "/" + Objects.toString(pkg.getPackageType(), "misc") + "/install" + runModeSuffix;

    String versionSuffix = "";
    String packageVersion = pkg.getVersion();
    String packageVersionWithoutSuffix = packageVersion;
    if (this.packageVersionMode == PackageVersionMode.RELEASE_SUFFIX_VERSION && this.version != null) {
      packageVersionWithoutSuffix = Strings.CS.removeEnd(packageVersion, buildVersionSuffix(pkg, false));
    }
    if (packageVersion != null && pkg.getFile().getName().contains(packageVersionWithoutSuffix)) {
      versionSuffix = "-" + packageVersion;
    }
    String fileName = pkg.getName() + versionSuffix
        + "." + FilenameUtils.getExtension(pkg.getFile().getName());
    return path + "/" + fileName;
  }

  /**
   * Build path to be used for embedded bundle.
   * @param bundleFile Bundle
   * @param rootPath Root path
   * @return Package path
   */
  private static String buildBundlePath(BundleFile bundleFile, String rootPath, String environmentRunMode) {
    String runModeSuffix = buildRunModeSuffix(bundleFile, environmentRunMode);

    // add run mode suffix to both install folder path and package file name
    String path = rootPath + "/application/install" + runModeSuffix;

    return path + "/" + bundleFile.getFile().getName();
  }

  /**
   * Rewrite content package ZIP file while adding to "all" package:
   * Add dependency to previous package in CONGA configuration file oder.
   * @param pkg Package to process (can be parent packe of the actual file)
   * @param previousPkg Previous package to get dependency information from.
   *          Is null if no previous package exists or auto dependency mode is switched off.
   * @param environmentRunMode Environment run mode
   * @param allPackagesFromFileSets Set with all packages from all file sets as dependency instances
   * @return Returns a list of content package *temporary* files - have to be deleted when processing is completed.
   * @throws IOException I/O error
   */
  @SuppressWarnings("java:S3776") // ignore complexity
  private List<TemporaryContentPackageFile> processContentPackage(ContentPackageFile pkg,
      ContentPackageFile previousPkg, String environmentRunMode,
      Set<Dependency> allPackagesFromFileSets) throws IOException {

    List<TemporaryContentPackageFile> result = new ArrayList<>();
    List<TemporaryContentPackageFile> subPackages = new ArrayList<>();

    // create temp zip file to create rewritten copy of package
    File tempFile = File.createTempFile(FilenameUtils.getBaseName(pkg.getFile().getName()), ".zip");

    // open original content package
    try (ZipFile zipFileIn = new ZipFile.Builder().setFile(pkg.getFile()).get()) {

      // iterate through entries and write them to the temp. zip file
      try (FileOutputStream fos = new FileOutputStream(tempFile);
          ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(fos)) {
        Enumeration<? extends ZipArchiveEntry> zipInEntries = zipFileIn.getEntries();
        while (zipInEntries.hasMoreElements()) {
          ZipArchiveEntry zipInEntry = zipInEntries.nextElement();
          if (!zipInEntry.isDirectory()) {
            try (InputStream is = zipFileIn.getInputStream(zipInEntry)) {
              boolean processedEntry = false;

              // if entry is properties.xml, update dependency information
              if (Strings.CS.equals(zipInEntry.getName(), "META-INF/vault/properties.xml")) {
                FileVaultProperties fileVaultProps = new FileVaultProperties(is);
                Properties props = fileVaultProps.getProperties();
                addSuffixToPackageName(props, pkg, environmentRunMode);
                addSuffixToVersion(props, pkg);

                // update package dependencies
                ContentPackageFile dependencyFile = previousPkg;
                if (autoDependenciesMode == AutoDependenciesMode.OFF || pkg.isDependencyChainIgnore()) {
                  dependencyFile = null;
                }
                updateDependencies(pkg, props, dependencyFile, environmentRunMode, allPackagesFromFileSets);

                // if package type is missing in package properties, put in the type defined in model
                String packageType = pkg.getPackageType();
                if (props.get(NAME_PACKAGE_TYPE) == null && packageType != null) {
                  props.put(NAME_PACKAGE_TYPE, packageType);
                }

                ZipArchiveEntry zipOutEntry = newZipEntry(zipInEntry);
                zipOut.putArchiveEntry(zipOutEntry);
                fileVaultProps.storeToXml(zipOut);
                zipOut.closeArchiveEntry();
                processedEntry = true;
              }

              // process sub-packages as well: add runmode suffix and update dependencies
              else if (Strings.CS.equals(FilenameUtils.getExtension(zipInEntry.getName()), "zip")) {
                File tempSubPackageFile = File.createTempFile(FilenameUtils.getBaseName(zipInEntry.getName()), ".zip");
                try (FileOutputStream subPackageFos = new FileOutputStream(tempSubPackageFile)) {
                  IOUtils.copy(is, subPackageFos);
                }

                // check if contained ZIP file is really a content package
                // then process it as well, remove if from the content package is was contained in
                // and add it as "1st level package" to the all package
                TemporaryContentPackageFile tempSubPackage = new TemporaryContentPackageFile(tempSubPackageFile, pkg.getVariants());
                if (packageTypeValidation == PackageTypeValidation.STRICT && !isValidPackageType(tempSubPackage)) {
                  throw new IllegalArgumentException("Package " + pkg.getPackageInfo() + " contains sub package " + tempSubPackage.getPackageInfo()
                      + " with invalid package type: '" + StringUtils.defaultString(tempSubPackage.getPackageType()) + "'");
                }
                if (StringUtils.isNoneBlank(tempSubPackage.getGroup(), tempSubPackage.getName())) {
                  subPackages.add(tempSubPackage);
                  processedEntry = true;
                }
                else {
                  FileUtils.deleteQuietly(tempSubPackageFile);
                }
              }

              // otherwise transfer the binary data 1:1
              if (!processedEntry) {
                ZipArchiveEntry zipOutEntry = newZipEntry(zipInEntry);
                zipOut.putArchiveEntry(zipOutEntry);
                IOUtils.copy(is, zipOut);
                zipOut.closeArchiveEntry();
              }
            }

          }
        }
      }

      // add sub package metadata to set with dependency information
      for (TemporaryContentPackageFile tempSubPackage : subPackages) {
        addDependencyInformation(allPackagesFromFileSets, tempSubPackage);
      }

      // process sub packages and add to result
      for (TemporaryContentPackageFile tempSubPackage : subPackages) {
        result.addAll(processContentPackage(tempSubPackage, previousPkg, environmentRunMode, allPackagesFromFileSets));
      }

      result.add(new TemporaryContentPackageFile(tempFile, pkg.getVariants()));
    }
    return result;
  }

  private ZipArchiveEntry newZipEntry(ZipArchiveEntry in) {
    ZipArchiveEntry out = new ZipArchiveEntry(in.getName());
    if (buildOutputTimestamp != null && buildOutputTimestamp.isValid()) {
      out.setLastModifiedTime(buildOutputTimestamp.toFileTime());
    }
    else if (in.getLastModifiedTime() != null) {
      out.setLastModifiedTime(in.getLastModifiedTime());
    }
    return out;
  }

  /**
   * Add dependency information to dependencies string in properties (if it does not exist already).
   * @param pkg Current content package
   * @param props Properties
   * @param dependencyFile Dependency package
   * @param allPackagesFromFileSets Set with all packages from all file sets as dependency instances
   */
  private void updateDependencies(ContentPackageFile pkg, Properties props, ContentPackageFile dependencyFile,
      String environmentRunMode, Set<Dependency> allPackagesFromFileSets) {
    String[] existingDepsStrings = StringUtils.split(props.getProperty(NAME_DEPENDENCIES), ",");
    Dependency[] existingDeps = null;
    if (existingDepsStrings != null && existingDepsStrings.length > 0) {
      existingDeps = Dependency.fromString(existingDepsStrings);
    }
    if (existingDeps != null) {
      existingDeps = autoDependenciesMode == AutoDependenciesMode.OFF
          ? rewriteReferencesToManagedPackages(pkg, environmentRunMode, allPackagesFromFileSets, existingDeps)
          : removeReferencesToManagedPackages(existingDeps, allPackagesFromFileSets);
    }

    Dependency[] deps;
    if (dependencyFile != null) {
      Dependency newDependency = createDependencyFromContentPackageFile(dependencyFile, environmentRunMode);
      deps = addDependency(existingDeps, newDependency);
    }
    else {
      deps = existingDeps;
    }

    if (deps != null) {
      String dependenciesString = Dependency.toString(deps);
      props.put(NAME_DEPENDENCIES, dependenciesString);
    }
  }

  private @NotNull Dependency createDependencyFromContentPackageFile(@NotNull ContentPackageFile dependencyFile,
      @NotNull String environmentRunMode) {
    String runModeSuffix = buildRunModeSuffix(dependencyFile, environmentRunMode);
    String dependencyVersion = dependencyFile.getVersion() + buildVersionSuffix(dependencyFile, true);
    return new Dependency(dependencyFile.getGroup(),
        dependencyFile.getName() + runModeSuffix,
        VersionRange.fromString(dependencyVersion));
  }

  private static Dependency[] addDependency(Dependency[] existingDeps, Dependency newDependency) {
    if (existingDeps != null) {
      return DependencyUtil.add(existingDeps, newDependency);
    }
    else {
      return new Dependency[] { newDependency };
    }
  }

  private static void addSuffixToPackageName(Properties props, ContentPackageFile pkg, String environmentRunMode) {
    String runModeSuffix = buildRunModeSuffix(pkg, environmentRunMode);
    String packageName = props.getProperty(NAME_NAME) + runModeSuffix;
    props.put(NAME_NAME, packageName);
  }

  private void addSuffixToVersion(Properties props, ContentPackageFile pkg) {
    // package version
    if (StringUtils.isEmpty(pkg.getVersion())) {
      return;
    }
    String suffixedVersion = pkg.getVersion() + buildVersionSuffix(pkg, true);
    props.put(NAME_VERSION, suffixedVersion);
  }

  private @NotNull Dependency[] rewriteReferencesToManagedPackages(@NotNull ContentPackageFile pkg,
      @NotNull String environmentRunMode, @NotNull Set<Dependency> allPackagesFromFileSets, @NotNull Dependency[] deps) {
    return Arrays.stream(deps)
        .map(dep -> rewriteReferenceIfDependencyIsManagedPackage(pkg, environmentRunMode, allPackagesFromFileSets, dep))
        .toArray(Dependency[]::new);
  }

  private @NotNull Dependency rewriteReferenceIfDependencyIsManagedPackage(@NotNull ContentPackageFile pkg,
      @NotNull String environmentRunMode, @NotNull Set<Dependency> allPackagesFromFileSets, @NotNull Dependency dep) {
    // not a managed package, return as is
    if (!allPackagesFromFileSets.contains(dep)) {
      return dep;
    }
    return findContentPackageFileForDependency(pkg, dep)
        // found a content package file for the dependency, rewrite the dependency
        .map(contentPackageFile -> createDependencyFromContentPackageFile(contentPackageFile, environmentRunMode))
        // found no content package file for the dependency, use current run mode suffix
        .orElseGet(() -> createDependencyWithCurrentPackageRunModeSuffix(pkg, environmentRunMode, dep));
  }

  private @NotNull Optional<ContentPackageFile> findContentPackageFileForDependency(@NotNull ContentPackageFile pkg,
      @NotNull Dependency dep) {
    // look for content package in all file sets
    return contentPackageFileSets.stream()
            // prefer file set which contains the current package to use current run mode
            .sorted((fileSet1, fileSet2) -> sortFileSetsContainingPackageFirst(pkg, fileSet1, fileSet2))
            .flatMap(fileSet -> fileSet.getFiles().stream())
            .filter(contentPackageFile -> isContentPackageForDependency(contentPackageFile, dep))
            .findFirst();
  }

  private int sortFileSetsContainingPackageFirst(@NotNull ContentPackageFile pkg,
      @NotNull ContentPackageFileSet fileSet1, @NotNull ContentPackageFileSet fileSet2) {
    int fileSet1ContainsPackage = fileSet1.getFiles().contains(pkg) ? 1 : 0;
    int fileSet2ContainsPackage = fileSet2.getFiles().contains(pkg) ? 1 : 0;
    return fileSet2ContainsPackage - fileSet1ContainsPackage;
  }

  private boolean isContentPackageForDependency(@NotNull ContentPackageFile contentPackageFile, @NotNull Dependency dep) {
    return contentPackageFile.getGroup().equals(dep.getGroup())
            && contentPackageFile.getName().equals(dep.getName());
  }

  private @NotNull Dependency createDependencyWithCurrentPackageRunModeSuffix(@NotNull ContentPackageFile pkg,
      @NotNull String environmentRunMode, @NotNull Dependency dep) {
    String runModeSuffix = buildRunModeSuffix(pkg, environmentRunMode);
    return new Dependency(dep.getGroup(), dep.getName() + runModeSuffix, dep.getRange());
  }

  /**
   * Removes existing references to packages contained in the list of packages to manage by this builder because
   * they are added new (and probably with a different package name) during processing.
   * @param deps Dependencies list
   * @param allPackagesFromFileSets Set with all packages from all file sets as dependency instances
   * @return Dependencies list
   */
  private static Dependency[] removeReferencesToManagedPackages(Dependency[] deps, Set<Dependency> allPackagesFromFileSets) {
    return Arrays.stream(deps)
        .filter(dep -> !allPackagesFromFileSets.contains(dep))
        .toArray(size -> new Dependency[size]);
  }

  private static void addDependencyInformation(Set<Dependency> allPackagesFromFileSets, ContentPackageFile pkg) {
    allPackagesFromFileSets.add(new Dependency(pkg.getGroup(), pkg.getName(), VersionRange.fromString(pkg.getVersion())));
  }

  /**
   * @return Group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * @return Package name
   */
  public String getPackageName() {
    return this.packageName;
  }

  /**
   * @return Target file
   */
  public File getTargetFile() {
    return this.targetFile;
  }

}
