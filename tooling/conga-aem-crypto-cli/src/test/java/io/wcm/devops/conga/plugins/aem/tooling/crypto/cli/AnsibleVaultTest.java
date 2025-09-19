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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.devops.conga.plugins.ansible.util.AnsibleVaultPassword;

class AnsibleVaultTest {

  private static final String TEST_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

  private File testFile;

  @BeforeEach
  void setUp() throws Exception {
    testFile = File.createTempFile(getClass().getName(), null);

    System.setProperty(AnsibleVaultPassword.SYSTEM_PROPERTY_PASSWORD, "test123");
  }

  @AfterEach
  void tearDown() {
    testFile.delete();
  }

  @Test
  void testEncryptDecrypt() throws Exception {
    FileUtils.write(testFile, TEST_CONTENT, StandardCharsets.UTF_8);

    // encrypt file
    AnsibleVault.encrypt(testFile);
    String content = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
    assertNotEquals(TEST_CONTENT, content);

    // decrypt file
    AnsibleVault.decrypt(testFile);
    content = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, content);
  }

  @Test
  void testEncryptDecryptWithCarriageReturns() throws Exception {
    FileUtils.write(testFile, TEST_CONTENT, StandardCharsets.UTF_8);

    // encrypt file
    AnsibleVault.encrypt(testFile);
    String content = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
    assertNotEquals(TEST_CONTENT, content);

    // replace \n with \r\n to simulate new lines on windows file systems
    content = Strings.CS.replace(content, "\n", "\r\n");
    FileUtils.write(testFile, content, StandardCharsets.UTF_8);

    // decrypt file
    AnsibleVault.decrypt(testFile);
    content = FileUtils.readFileToString(testFile, StandardCharsets.UTF_8);
    assertEquals(TEST_CONTENT, content);
  }

}
