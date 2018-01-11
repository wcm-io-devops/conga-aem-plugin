CONGA AEM Crypto Command Line Interface
=======================================

Command line tool to generate Crypto keys for AEM.

The generated keys are supported by AEM 6.3 and upwards.

Please note: You need to install the Java Cryptography Extension (JCE) Unlimited Strength policy files from Oracle, because Ansible uses 256 bit keys to handle encryption and decryption of the vault files. If you are using Java 8u162 or higher they are already active by default.
http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html


Generate AEM crypto keys
------------------------

Generate a set of crypto keys:

java -jar conga-aem-crypto-cli-${project.version}.jar -cryptoKeysGenerate 


Generate a set of crypto keys and encrypt them using Ansible Vault with the given password:

java -Dansible.password=mypassword -jar conga-aem-crypto-cli-${project.version}.jar -cryptoKeysGenerate -cryptoKeysAnsibleVaultEncrypt



Encypt and decrypt files with Ansible Vault
--------------------------------------------

Encrypt a file with Ansible Vault:

java -Dansible.password=mypassword -jar conga-aem-crypto-cli-${project.version}.jar -ansibleVaultEncrypt <file>


Decrypt a file with Ansible Vault:

java -Dansible.password=mypassword -jar conga-aem-crypto-cli-${project.version}.jar -ansibleVaultDecrypt <file>
