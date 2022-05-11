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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.generator.spi.ValidationException;
import io.wcm.devops.conga.generator.spi.ValidatorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.util.PluginManagerImpl;

class AnyValidatorTest {

  private ValidatorPlugin underTest;

  @BeforeEach
  void setUp() {
    underTest = new PluginManagerImpl().get(AnyValidator.NAME, ValidatorPlugin.class);
  }

  @Test
  void testValidPublish() throws Exception {
    File file = new File(getClass().getResource("/any/dispatcher.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    underTest.apply(fileContext, null);
  }

  @Test
  void testValidAuthor() throws Exception {
    File file = new File(getClass().getResource("/any/author_dispatcher.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    underTest.apply(fileContext, null);
  }

  @Test
  void testInvalid() throws Exception {
    File file = new File(getClass().getResource("/any/invalidAny.any").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertTrue(underTest.accepts(fileContext, null));
    assertThrows(ValidationException.class, () -> {
      underTest.apply(fileContext, null);
    });
  }

  @Test
  void testInvalidFileExtension() throws Exception {
    File file = new File(getClass().getResource("/any/noAny.txt").toURI());
    FileContext fileContext = new FileContext().file(file).charset(StandardCharsets.ISO_8859_1);
    assertFalse(underTest.accepts(fileContext, null));
  }

  @Test
  void testReplaceTicks() {
    assertEquals("/prop1 \"value1\"", AnyValidator.replaceTicks("/prop1 'value1'"));
    assertEquals("/group { /p1 \"v1\"\n/p2 \"v2\" /p3 \"v3\" }", AnyValidator.replaceTicks("/group { /p1 'v1'\n/p2 \"v2\" /p3 'v3' }"));
  }

}
