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
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import io.wcm.devops.conga.plugins.aem.crypto.impl.AesCryptoSupport;
import io.wcm.devops.conga.plugins.aem.crypto.impl.HmacCryptoKeySupport;
import io.wcm.devops.conga.plugins.ansible.util.AnsibleVaultPassword;
import net.wedjaa.ansible.vault.crypto.VaultHandler;

/**
 * CONGA command line interface.
 */
public final class AemCryptoCli {

  /**
   * Command line options
   */
  public static final Options CLI_OPTIONS = new Options();
  static {
    CLI_OPTIONS.addOption("generateCryptoKeys", false, "Generates Crypto keys for AEM 6.3 and up.");
    CLI_OPTIONS.addOption("ansibleVaultEncrypt", false, "Encrypts the keys with Ansible Vault after generation.");
    CLI_OPTIONS.addOption("target", true, "Target path for the generated keys.");
    CLI_OPTIONS.addOption("?", false, "Print usage help.");
  }

  private AemCryptoCli() {
    // static methods only
  }

  /**
   * CLI entry point
   * @param args Command line arguments
   * @throws Exception Exception
   */
  //CHECKSTYLE:OFF
  public static void main(String[] args) throws Exception {
    //CHECKSTYLE:ON
    CommandLine commandLine = new DefaultParser().parse(CLI_OPTIONS, args, true);

    if (commandLine.hasOption("?")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(150);
      formatter.printHelp("java -jar conga-aem-crypto-cli-<version>.jar <arguments>", CLI_OPTIONS);
      return;
    }

    boolean generateCryptoKeys = commandLine.hasOption("generateCryptoKeys");
    boolean ansibleVaultEncrypt = commandLine.hasOption("ansibleVaultEncrypt");
    File targetDir = new File(commandLine.getOptionValue("target", "target"));

    if (generateCryptoKeys) {
      Stream<CryptoKey> keys = generateCryptoKeys();
      if (ansibleVaultEncrypt) {
        keys = encryptKeys(keys);
      }
      writeKeys(keys, targetDir);
    }
    else {
      throw new IllegalArgumentException("generateCryptoKeys option is mandatory.");
    }

  }

  private static Stream<CryptoKey> generateCryptoKeys() throws GeneralSecurityException {
    return Stream.of(
        new CryptoKey("master", new AesCryptoSupport().generateKey().getEncoded()),
        new CryptoKey("hmac", new HmacCryptoKeySupport().generateKey().getEncoded()));
  }

  private static Stream<CryptoKey> encryptKeys(Stream<CryptoKey> keys) {
    String password = AnsibleVaultPassword.get();
    return keys.map(key -> {
      try {
        return new CryptoKey(key.getName(), VaultHandler.encrypt(key.getData(), password));
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to encrypt key '" + key.getName() + "'.", ex);
      }
    });
  }

  private static void writeKeys(Stream<CryptoKey> keys, File targetDir) {
    if (!targetDir.exists()) {
      targetDir.mkdirs();
    }
    keys.forEach(key -> {
      File outputFile = new File(targetDir, key.getName());
      if (outputFile.exists()) {
        outputFile.delete();
      }
      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
        os.write(key.getData());
        System.out.println("Generated: " + outputFile.getPath());
      }
      catch (IOException ex) {
        throw new RuntimeException("Unable to write key '" + key.getName() + "' to " + targetDir.getPath(), ex);
      }
    });
  }

}
