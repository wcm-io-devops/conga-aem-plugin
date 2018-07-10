/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.devops.conga.plugins.aem.tooling.crypto.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.plugins.ansible.util.AnsibleVaultPassword;

public class CryptoKeysTest {

  private File targetFolder;

  @BeforeEach
  public void setUp() throws Exception {
    // create temp directory path
    targetFolder = File.createTempFile(getClass().getName(), null);
    targetFolder.delete();

    System.setProperty(AnsibleVaultPassword.SYSTEM_PROPERTY_PASSWORD, "test123");
  }

  @AfterEach
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(targetFolder);
  }

  @Test
  public void testGenerate() throws Exception {
    Stream<File> files = CryptoKeys.generate(targetFolder, false);
    assetFiles(files);
  }

  @Test
  public void testGenerateAnsibleVaultEncrypt() throws Exception {
    Stream<File> files = CryptoKeys.generate(targetFolder, true);
    assetFiles(files);
  }

  private void assetFiles(Stream<File> filesStream) {
    List<File> files = filesStream.collect(Collectors.toList());
    assertEquals(2, files.size());
    assertTrue(files.get(0).exists());
    assertEquals("master", files.get(0).getName());
    assertTrue(files.get(1).exists());
    assertEquals("hmac", files.get(1).getName());
  }

}
