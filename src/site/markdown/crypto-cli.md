## CONGA AEM Crypto CLI tool

The CONGA AEM plugin also provides a command-line interface tool for generating new AEM crypto keys.

The generated keys are supported by AEM 6.3 and upwards.

_**Please note:** You need to install the [Java Cryptography Extension (JCE) Unlimited Strength policy files][jce-policy] from Oracle, because Ansible uses 256 bit keys to handle encryption and decryption of the vault files. If you are using Java 8u162 or higher they are already active by default._


### Download

Download it from Maven Central:

|---|---|---|
| [CONGA AEM Crypto CLI tool](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli) |


### Generate AEM crypto keys

Generate a set of crypto keys:

```
java -jar conga-aem-crypto-cli-<version>.jar -cryptoKeysGenerate
```

Generate a set of crypto keys and encrypt them using Ansible Vault with the given password:

```
java -Dansible.vault.password=mypassword -jar conga-aem-crypto-cli-<version>.jar \
    -cryptoKeysGenerate -cryptoKeysAnsibleVaultEncrypt
```


### Encypt and decrypt files with Ansible Vault

Encrypt a file with Ansible Vault:

```
java -Dansible.vault.password=mypassword -jar conga-aem-crypto-cli-<version>.jar \
    -ansibleVaultEncrypt <file>
```


Decrypt a file with Ansible Vault:

```
java -Dansible.vault.password=mypassword -jar conga-aem-crypto-cli-<version>.jar \
    -ansibleVaultDecrypt <file>
```


[jce-policy]: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
