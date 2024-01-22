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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import io.wcm.devops.conga.plugins.aem.crypto.impl.AesCryptoSupport;
import io.wcm.devops.conga.plugins.aem.crypto.impl.HmacCryptoKeySupport;
import io.wcm.devops.conga.plugins.ansible.util.AnsibleVaultPassword;
import net.wedjaa.ansible.vault.crypto.VaultHandler;

/**
 * Generates AES and HMAC crypto keys for AEM - with or without ansible vault encryption.
 */
public final class CryptoKeys {

  private CryptoKeys() {
    // static methods only
  }

  /**
   * Generates AES and HMAC crypto keys for AEM.
   * @param targetDir Target directory
   * @param ansibleVaultEncrypt If true, the crypto keys are encrypted with Ansible Vault.
   * @return Generated files
   * @throws GeneralSecurityException Security exception
   */
  public static Stream<File> generate(File targetDir, boolean ansibleVaultEncrypt) throws GeneralSecurityException {
    String ansibleVaultPassword = null;
    if (ansibleVaultEncrypt) {
      ansibleVaultPassword = AnsibleVaultPassword.get();
    }
    return generate(targetDir, ansibleVaultEncrypt, ansibleVaultPassword);
  }

  /**
   * Generates AES and HMAC crypto keys for AEM.
   * @param targetDir Target directory
   * @param ansibleVaultEncrypt If true, the crypto keys are encrypted with Ansible Vault.
   * @param ansibleVaultPassword Ansible Vault Password
   * @return Generated files
   * @throws GeneralSecurityException Security exception
   */
  public static Stream<File> generate(File targetDir, boolean ansibleVaultEncrypt,
      String ansibleVaultPassword) throws GeneralSecurityException {
    Stream<KeyItem> keys = Stream.of(
        new KeyItem("master", new AesCryptoSupport().generateKey().getEncoded()),
        new KeyItem("hmac", new HmacCryptoKeySupport().generateKey().getEncoded()));
    if (ansibleVaultEncrypt) {
      keys = encryptKeys(keys, ansibleVaultPassword);
    }
    return writeKeys(keys, targetDir);
  }

  @SuppressWarnings("java:S112") // runtime exception
  private static Stream<KeyItem> encryptKeys(Stream<KeyItem> keys, String password) {
    return keys.map(key -> {
      try {
        return new KeyItem(key.getName(), VaultHandler.encrypt(key.getData(), password));
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to encrypt key '" + key.getName() + "'.", ex);
      }
    });
  }

  @SuppressWarnings("java:S112") // runtime exception
  private static Stream<File> writeKeys(Stream<KeyItem> keys, File targetDir) {
    if (!targetDir.exists()) {
      if (!targetDir.mkdirs()) {
        throw new RuntimeException("Unable to create directories: " + targetDir.getPath());
      }
    }
    return keys.map(key -> {
      File outputFile = new File(targetDir, key.getName());
      if (outputFile.exists()) {
        try {
          Files.delete(outputFile.toPath());
        }
        catch (IOException ex) {
          throw new RuntimeException("Unable to delete file: " + outputFile.getPath(), ex);
        }
      }
      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
        os.write(key.getData());
        return outputFile;
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to write key '" + key.getName() + "' to " + targetDir.getPath(), ex);
      }
    });
  }

  private static class KeyItem {

    private final String name;
    private final byte[] data;

    KeyItem(String name, byte[] data) {
      this.name = name;
      this.data = data;
    }

    String getName() {
      return this.name;
    }

    byte[] getData() {
      return this.data;
    }

  }

}
