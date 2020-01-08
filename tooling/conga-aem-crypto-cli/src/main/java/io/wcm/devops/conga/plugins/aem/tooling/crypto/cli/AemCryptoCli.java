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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

/**
 * CONGA command line interface.
 */
public final class AemCryptoCli {

  static final String CRYPTO_KEYS_GENERATE = "cryptoKeysGenerate";
  static final String CRYPTO_KEYS_ANSIBLE_VAULT_ENCRYPT = "cryptoKeysAnsibleVaultEncrypt";
  static final String TARGET = "target";
  static final String ANSIBLE_VAULT_ENCRYPT = "ansibleVaultEncrypt";
  static final String ANSIBLE_VAULT_DECRYPT = "ansibleVaultDecrypt";
  static final String AEM_CRYPTO_ENCRYPT = "aemCryptoEncrypt";
  static final String AEM_CRYPTO_DECRYPT = "aemCryptoDecrypt";
  static final String CRYPTO_AES_KEY = "cryptoAesKey";

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
    CLI_OPTIONS.addOption(AEM_CRYPTO_ENCRYPT, true, "Encrypts the given value with AEM crypto support.");
    CLI_OPTIONS.addOption(AEM_CRYPTO_DECRYPT, true, "Decrypts the given value with AEM crypto support.");
    CLI_OPTIONS.addOption(CRYPTO_AES_KEY, true, "Path to 'master' file from AEM crypto keys for "
        + "'" + AEM_CRYPTO_ENCRYPT + "' and '" + AEM_CRYPTO_DECRYPT + "'.");
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
  @SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.SystemPrintln" })
  public static void main(String[] args) throws Exception {
    //CHECKSTYLE:ON
    CommandLine commandLine = new DefaultParser().parse(CLI_OPTIONS, args, true);

    boolean generateCryptoKeys = commandLine.hasOption(CRYPTO_KEYS_GENERATE);
    boolean ansibleVaultEncrypt = commandLine.hasOption(CRYPTO_KEYS_ANSIBLE_VAULT_ENCRYPT);
    File targetDir = new File(commandLine.getOptionValue(TARGET, "target"));
    String ansibleVaultEncryptPath = commandLine.getOptionValue(ANSIBLE_VAULT_ENCRYPT);
    String ansibleVaultDecryptPath = commandLine.getOptionValue(ANSIBLE_VAULT_DECRYPT);
    String aemCryptoEncrypt = commandLine.getOptionValue(AEM_CRYPTO_ENCRYPT);
    String aemCryptoDecrypt = commandLine.getOptionValue(AEM_CRYPTO_DECRYPT);
    String cryptoAesKey = commandLine.getOptionValue(CRYPTO_AES_KEY);

    if (generateCryptoKeys) {
      CryptoKeys.generate(targetDir, ansibleVaultEncrypt)
        .forEach(file -> System.out.println("Generated: " + file.getPath()));
      return;
    }
    else if (StringUtils.isNotBlank(ansibleVaultEncryptPath)) {
      AnsibleVault.encrypt(new File(ansibleVaultEncryptPath));
      return;
    }
    else if (StringUtils.isNotBlank(ansibleVaultDecryptPath)) {
      AnsibleVault.decrypt(new File(ansibleVaultDecryptPath));
      return;
    }
    else if (StringUtils.isNotBlank(aemCryptoEncrypt)) {
      String result = AemCrypto.encryptString(aemCryptoEncrypt, cryptoAesKey);
      System.out.println(result);
      return;
    }
    else if (StringUtils.isNotBlank(aemCryptoDecrypt)) {
      String result = AemCrypto.decryptString(aemCryptoDecrypt, cryptoAesKey);
      System.out.println(result);
      return;
    }

    // print usage help
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(150);
    formatter.printHelp("java -jar conga-aem-crypto-cli-<version>.jar <arguments>", CLI_OPTIONS);
  }

}
