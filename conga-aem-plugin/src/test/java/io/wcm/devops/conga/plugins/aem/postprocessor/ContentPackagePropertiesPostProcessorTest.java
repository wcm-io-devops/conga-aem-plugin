/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.devops.conga.plugins.aem.postprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.plugins.sling.postprocessor.ProvisioningOsgiConfigPostProcessor;

class ContentPackagePropertiesPostProcessorTest {

  private PostProcessorPlugin underTest;

  @BeforeEach
  void setUp() {
    underTest = new PluginManagerImpl().get(ContentPackagePropertiesPostProcessor.NAME, PostProcessorPlugin.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testContentPackage() throws Exception {

    FileContext fileContext = new FileContext()
        .file(new File("src/test/resources/package/example.zip"));

    // post-process
    applyPlugin(fileContext, ImmutableMap.of());

    // validate
    Map<String, Object> props = (Map<String, Object>)fileContext.getModelOptions().get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY);
    assertEquals("mapping-sample", props.get("name"));
    assertEquals(false, props.get("requiresRoot"));
    assertEquals(2, props.get("packageFormatVersion"));
    assertNull(props.get("packageType"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testContentPackageOverridePackageType() throws Exception {

    FileContext fileContext = new FileContext()
        .file(new File("src/test/resources/package/example.zip"));

    // post-process
    applyPlugin(fileContext, ImmutableMap.of("contentPackage", ImmutableMap.of("packageType", "mytype")));

    // validate
    Map<String, Object> props = (Map<String, Object>)fileContext.getModelOptions().get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY);
    assertEquals("mytype", props.get("packageType"));
  }

  @Test
  void testNonContentPackage() throws Exception {

    FileContext fileContext = new FileContext()
        .file(new File("src/test/resources/package/no-content-package.zip"));

    // post-process
    applyPlugin(fileContext, ImmutableMap.of());

    // validate
    assertNull(fileContext.getModelOptions().get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY));
  }

  private void applyPlugin(FileContext fileContext, Map<String, Object> options) {
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(new PluginManagerImpl())
        .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));
    PostProcessorContext context = new PostProcessorContext()
        .pluginContextOptions(pluginContextOptions)
        .options(options);

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);
  }

}
