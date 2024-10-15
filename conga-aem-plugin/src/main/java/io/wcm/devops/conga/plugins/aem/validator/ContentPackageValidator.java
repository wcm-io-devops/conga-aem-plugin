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
package io.wcm.devops.conga.plugins.aem.validator;

import static org.apache.jackrabbit.vault.packaging.PackageProperties.NAME_PACKAGE_TYPE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.jackrabbit.filevault.maven.packaging.ValidatorSettings;
import org.apache.jackrabbit.filevault.maven.packaging.mojo.ValidatePackageMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import io.wcm.devops.conga.generator.spi.ImplicitApplyOptions;
import io.wcm.devops.conga.generator.spi.ValidationException;
import io.wcm.devops.conga.generator.spi.ValidatorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.ValidatorContext;
import io.wcm.devops.conga.generator.util.FileUtil;
import io.wcm.devops.conga.model.util.MapExpander;
import io.wcm.devops.conga.model.util.MapMerger;
import io.wcm.devops.conga.tooling.maven.plugin.util.MavenContext;
import io.wcm.tooling.commons.packmgr.util.ContentPackageProperties;

/**
 * Validates AEM content packages with Jackrabbit FileVault validator.
 */
public class ContentPackageValidator implements ValidatorPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "aem-contentpackage";

  private static final String FILE_EXTENSION = "zip";

  private static final String OPTION_VALIDATORS_SETTINGS = "contentPackage.validatorsSettings";

  // apply default validation for AEM and wcm.io node types
  private static final Map<String, Object> DEFAULT_VALIDATORS_SETTINGS = Map.of("jackrabbit-nodetypes",
      Map.of("options", Map.of("cnds", "tccl:aem.cnd,tccl:wcmio.cnd")));

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, ValidatorContext context) {
    return FileUtil.matchesExtension(file, FILE_EXTENSION);
  }

  @Override
  public ImplicitApplyOptions implicitApply(FileContext file, ValidatorContext context) {
    if (FileUtil.matchesExtension(file, FILE_EXTENSION)) {
      return ImplicitApplyOptions.ALWAYS;
    }
    else {
      return ImplicitApplyOptions.NEVER;
    }
  }

  @Override
  public Void apply(FileContext file, ValidatorContext context) throws ValidationException {
    try {
      // validate package if a package type is defined
      // supported only within Maven
      String packageType = Objects.toString(ContentPackageProperties.get(file.getFile()).get(NAME_PACKAGE_TYPE), null);
      if (packageType != null && context.getContainerContext() instanceof MavenContext) {
        validateContentPackage(file.getFile(), context, (MavenContext)context.getContainerContext());
      }
    }
    catch (IOException | IllegalAccessException | MojoExecutionException | MojoFailureException
        | IllegalArgumentException ex) {
      throw new ValidationException("Unable to process content package: " + ex.getMessage(), ex);
    }
    return null;
  }

  private void validateContentPackage(File packageFile, ValidatorContext context, MavenContext mavenContext)
      throws IllegalAccessException, MojoExecutionException, MojoFailureException, IllegalArgumentException {

    // instantiate Mojo to execute the validation (a bit hacky as we have to manually
    // pass through the maven context objects here via reflection)
    ValidatePackageMojo mojo = new ValidatePackageMojo(mavenContext.getRepoSystem(), mavenContext.getBuildContext());
    mojo.setLog(new InfoPrefixLog(mavenContext.getLog(), "    "));

    setProperty(mojo, "packageFile", packageFile);
    setProperty(mojo, "enforceRecursiveSubpackageValidation", false);
    setProperty(mojo, "failOnDependencyErrors", true);
    setProperty(mojo, "project", mavenContext.getProject());
    setProperty(mojo, "session", mavenContext.getSession());
    setProperty(mojo, "failOnValidationWarnings", false);
    setProperty(mojo, "mapPackageDependencyToMavenGa", Collections.emptyList());
    setProperty(mojo, "resolutionErrorHandler", mavenContext.getResolutionErrorHandler());
    setProperty(mojo, "attachedArtifacts", Collections.emptyList());

    Object validatorsSettings = MapExpander.getDeep(context.getOptions(), OPTION_VALIDATORS_SETTINGS);
    Map<String,Object> validatorSettingsMap = MapMerger.merge(toMap(validatorsSettings), DEFAULT_VALIDATORS_SETTINGS);
    setProperty(mojo, "validatorsSettings", toValidatorsSettings(validatorSettingsMap));

    mojo.execute();
  }

  private void setProperty(Object object, String propertyName, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    setProperty(object, object.getClass(), propertyName, value);
  }

  @SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "java:S3011" })
  private void setProperty(Object object, Class<?> clazz, String propertyName, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    try {
      Field field = clazz.getDeclaredField(propertyName);
      field.setAccessible(true);
      field.set(object, value);
    }
    catch (NoSuchFieldException ex) {
      // check super class
      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
        setProperty(object, superClass, propertyName, value);
      }
    }
  }

  private Map<String, ValidatorSettings> toValidatorsSettings(Map<String, Object> map)
      throws IllegalArgumentException, IllegalAccessException {
    Map<String, ValidatorSettings> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      Map<String, Object> validatorSettingsMap = toMap(entry.getValue());

      boolean isDisabled = toBoolean(validatorSettingsMap.get("isDisabled"));
      ValidatorSettings validatorSettings = new ValidatorSettings();
      setProperty(validatorSettings, "isDisabled", isDisabled);

      String defaultSeverity = toString(validatorSettingsMap.get("defaultSeverity"));
      if (defaultSeverity != null) {
        validatorSettings.setDefaultSeverity(defaultSeverity);
      }

      Map<String, Object> options = toMap(validatorSettingsMap.get("options"));
      for (Map.Entry<String, Object> option : options.entrySet()) {
        validatorSettings.getOptions().put(option.getKey(), toString(option.getValue()));
      }

      result.put(entry.getKey(), validatorSettings);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMap(Object value) {
    if (value instanceof Map) {
      return (Map)value;
    }
    return Collections.emptyMap();
  }

  private boolean toBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean)value;
    }
    if (value instanceof String) {
      return BooleanUtils.toBoolean((String)value);
    }
    return false;
  }

  private String toString(Object value) {
    if (value instanceof String) {
      return (String)value;
    }
    else if (value != null) {
      return value.toString();
    }
    return null;
  }

}
