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
package io.wcm.devops.conga.plugins.aem.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import io.wcm.devops.conga.generator.spi.ValidationException;
import io.wcm.devops.conga.generator.spi.ValidatorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

public class AnyValidatorTest {

  private ValidatorPlugin underTest;

  @Before
  public void setUp() {
    underTest = new PluginManagerImpl().get(AnyValidator.NAME, ValidatorPlugin.class);
  }

  @Test
  public void testValidPublish() throws Exception {
    File file = new File(getClass().getResource("/any/dispatcher.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    underTest.apply(fileContext, null);
  }

  @Test
  public void testValidAuthor() throws Exception {
    File file = new File(getClass().getResource("/any/author_dispatcher.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    underTest.apply(fileContext, null);
  }

  @Test(expected = ValidationException.class)
  public void testInvalid() throws Exception {
    File file = new File(getClass().getResource("/any/invalidAny.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    underTest.apply(fileContext, null);
  }

  @Test
  public void testInvalidFileExtension() throws Exception {
    File file = new File(getClass().getResource("/any/noAny.txt").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertFalse(underTest.accepts(fileContext, null));
  }

  @Test
  public void testReplaceTicks() {
    assertEquals("/prop1 \"value1\"", AnyValidator.replaceTicks("/prop1 'value1'"));
    assertEquals("/group { /p1 \"v1\"\n/p2 \"v2\" /p3 \"v3\" }", AnyValidator.replaceTicks("/group { /p1 'v1'\n/p2 \"v2\" /p3 'v3' }"));
  }

}
