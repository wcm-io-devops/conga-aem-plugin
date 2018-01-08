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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import io.wcm.devops.conga.generator.spi.PostProcessorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.PluginContextOptions;
import io.wcm.devops.conga.generator.spi.context.PostProcessorContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;
import io.wcm.devops.conga.plugins.sling.postprocessor.ProvisioningOsgiConfigPostProcessor;

public class ContentPackagePropertiesPostProcessorTest {

  private PostProcessorPlugin underTest;

  @Before
  public void setUp() {
    underTest = new PluginManagerImpl().get(ContentPackagePropertiesPostProcessor.NAME, PostProcessorPlugin.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContentPackage() throws Exception {

    FileContext fileContext = new FileContext()
        .file(new File("src/test/resources/package/example.zip"));

    // post-process
    applyPlugin(fileContext);

    // validate
    Map<String, Object> props = (Map<String, Object>)fileContext.getModelOptions().get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY);
    assertEquals("mapping-sample", props.get("name"));
    assertEquals(false, props.get("requiresRoot"));
    assertEquals(2, props.get("packageFormatVersion"));
  }

  @Test
  public void testNonContentPackage() throws Exception {

    FileContext fileContext = new FileContext()
        .file(new File("src/test/resources/package/no-content-package.zip"));

    // post-process
    applyPlugin(fileContext);

    // validate
    assertNull(fileContext.getModelOptions().get(ContentPackagePropertiesPostProcessor.MODEL_OPTIONS_PROPERTY));
  }

  private void applyPlugin(FileContext fileContext) {
    PluginContextOptions pluginContextOptions = new PluginContextOptions()
        .pluginManager(new PluginManagerImpl())
        .logger(LoggerFactory.getLogger(ProvisioningOsgiConfigPostProcessor.class));
    PostProcessorContext context = new PostProcessorContext()
        .pluginContextOptions(pluginContextOptions);

    assertTrue(underTest.accepts(fileContext, context));
    underTest.apply(fileContext, context);
  }

}
