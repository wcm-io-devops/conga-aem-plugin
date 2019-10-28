/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AemCryptoTest {

  private File targetFolder;
  private String cryptoAesKey;

  @BeforeEach
  public void setUp() throws Exception {
    // create temp directory path
    targetFolder = File.createTempFile(getClass().getName(), null);
    targetFolder.delete();

    // generate crypto keys
    CryptoKeys.generate(targetFolder, false).forEach(item -> { /* generate */ });
    cryptoAesKey = targetFolder.getPath() + "/master";
  }

  @AfterEach
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(targetFolder);
  }

  @Test
  public void testEncryptDecryptString() throws Exception {
    String value = "myTestValue-äöüß€";

    String encryptedValue = AemCrypto.encryptString(value, cryptoAesKey);
    assertFalse(StringUtils.equals(value, encryptedValue));

    String decryptedValue = AemCrypto.decryptString(encryptedValue, cryptoAesKey);
    assertEquals(value, decryptedValue);
  }

}
