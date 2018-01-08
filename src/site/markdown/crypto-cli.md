## CONGA AEM Crypto CLI tool

The CONGA AEM plugin also provides a command-line interface tool for generating new AEM crypto keys.

The generated keys are supported by AEM 6.3 and upwards.


### Download

Download it from Maven Central:

|---|---|---|
| [CONGA AEM Crypto CLI tool](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/conga-aem-crypto-cli) |


### Usage

Generate a set of Crypto Keys:

```
java -jar conga-aem-crypto-cli-<version>.jar -generateCryptoKeys 
```


Generate a set of Crypto Keys and encrypt them using Ansible Vault with the given password:

```
java -Dansible.password=mypassword -jar conga-aem-crypto-cli-<version>.jar \
    -generateCryptoKeys -ansibleVaultEncrypt
```
