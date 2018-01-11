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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

/**
 * CONGA command line interface.
 */
public final class AemCryptoCli {

  private static final String CRYPTO_KEYS_GENERATE = "cryptoKeysGenerate";
  private static final String CRYPTO_KEYS_ANSIBLE_VAULT_ENCRYPT = "cryptoKeysAnsibleVaultEncrypt";
  private static final String TARGET = "target";
  private static final String ANSIBLE_VAULT_ENCRYPT = "ansibleVaultEncrypt";
  private static final String ANSIBLE_VAULT_DECRYPT = "ansibleVaultDecrypt";

  /**
   * Command line options
   */
  public static final Options CLI_OPTIONS = new Options();
  static {
    CLI_OPTIONS.addOption(CRYPTO_KEYS_GENERATE, false, "Generates Crypto keys for AEM 6.3 and up.");
    CLI_OPTIONS.addOption(CRYPTO_KEYS_ANSIBLE_VAULT_ENCRYPT, false, "Encrypts the keys with Ansible Vault after generation.");
    CLI_OPTIONS.addOption(TARGET, true, "Target path for the generated keys.");
    CLI_OPTIONS.addOption(ANSIBLE_VAULT_ENCRYPT, true, "Encrypts the given file with Ansible Vault.");
    CLI_OPTIONS.addOption(ANSIBLE_VAULT_DECRYPT, true, "Decrypts the given file with Ansible Vault.");
    CLI_OPTIONS.addOption("?", false, "Print usage help.");
  }

  private static final List<String> MANDATORY_OPTIONS = Arrays.asList(new String[] {
      CRYPTO_KEYS_GENERATE,
      ANSIBLE_VAULT_ENCRYPT,
      ANSIBLE_VAULT_DECRYPT
  });

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

    boolean generateCryptoKeys = commandLine.hasOption(CRYPTO_KEYS_GENERATE);
    boolean ansibleVaultEncrypt = commandLine.hasOption(CRYPTO_KEYS_ANSIBLE_VAULT_ENCRYPT);
    File targetDir = new File(commandLine.getOptionValue(TARGET, "target"));
    String ansibleVaultEncryptPath = commandLine.getOptionValue(ANSIBLE_VAULT_ENCRYPT);
    String ansibleVaultDecryptPath = commandLine.getOptionValue(ANSIBLE_VAULT_DECRYPT);

    if (generateCryptoKeys) {
      CryptoKeys.generate(targetDir, ansibleVaultEncrypt)
        .forEach(file -> System.out.println("Generated: " + file.getPath()));
    }
    else if (StringUtils.isNotBlank(ansibleVaultEncryptPath)) {
      AnsibleVault.encrypt(new File(ansibleVaultEncryptPath));
    }
    else if (StringUtils.isNotBlank(ansibleVaultDecryptPath)) {
      AnsibleVault.decrypt(new File(ansibleVaultDecryptPath));
    }
    else {
      throw new IllegalArgumentException("Mandatory parameter missing - one of " + MANDATORY_OPTIONS + " expected.");
    }

  }

}
