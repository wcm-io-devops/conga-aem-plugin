CONGA AEM Crypto Command Line Interface
=======================================

Command line tool to generate Crypto keys for AEM.

The generated keys are supported by AEM 6.3 and upwards.


Usage
-----

Generate a set of Crypto Keys:

java -jar conga-aem-crypto-cli-${project.version}.jar -generateCryptoKeys 



Generate a set of Crypto Keys and encrypt them using Ansible Vault with the given password:

java -Dansible.password=mypassword -jar conga-aem-crypto-cli-${project.version}.jar -generateCryptoKeys -ansibleVaultEncrypt
