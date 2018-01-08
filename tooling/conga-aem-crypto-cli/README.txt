CONGA AEM Crypto Command Line Interface
=======================================

Command line tool to generate Crypto keys for AEM.


Usage
-----

Generate a set of Crypto Keys for AEM 6.3 and up:

java -jar conga-aem-crypto-cli-${project.version}.jar -generateCryptoKeys 



Generate a set of Crypto Keys for AEM 6.3 and up and encrypt them using Ansible Vault with the given password:

java -Dansible.password=mypassword -jar conga-aem-crypto-cli-${project.version}.jar -generateCryptoKeys -ansibleVaultEncrypt
