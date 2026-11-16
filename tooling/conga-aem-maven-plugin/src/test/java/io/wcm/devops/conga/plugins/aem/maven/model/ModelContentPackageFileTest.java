/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
package io.wcm.devops.conga.plugins.aem.maven.model;

import static io.wcm.devops.conga.plugins.aem.postprocessor.ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.vault.packaging.DependencyHandling;
import org.junit.jupiter.api.Test;

class ModelContentPackageFileTest {

  private static final File FILE = new File("package.zip");

  @Test
  void testDependencyHandling_UpperCase() {
    ModelContentPackageFile underTest = new ModelContentPackageFile(FILE, fileData("STRICT"), List.of());
    assertEquals(DependencyHandling.STRICT, underTest.getDependencyHandling());
  }

  @Test
  void testDependencyHandling_Invalid() {
    Map<String, Object> fileData = fileData("invalid");
    List<String> variants = List.of();
    assertThrows(IllegalArgumentException.class, () -> new ModelContentPackageFile(FILE, fileData, variants));
  }

  private static Map<String, Object> fileData(String dependencyHandling) {
    return Map.of(
        "dependencyHandling", dependencyHandling,
        MODEL_OPTIONS_PROPERTY, Map.of());
  }

}
