## About CONGA AEM Plugin

wcm.io DevOps CONGA Plugin for [Adobe Experience Manager (AEM)][aem].

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/io.wcm.devops.conga.plugins.aem/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.devops.conga.plugins/io.wcm.devops.conga.plugins.aem)


### Documentation

* [Usage][usage]
* [CONGA Extensions][extensions]
* [Custom Handlebars expressions for AEM][handlebars-helpers]
* [API documentation][apidocs]
* [CONGA AEM Maven Plugin Documentation][plugindocs]
* [AEM Crypto CLI tool][crypto-cli]
* [Changelog][changelog]


### Overview

This plugin extends [CONGA][conga] with:

* Generate AEM content packages for OSGi configurations and from JSON content fragments
* Extract package properties from AEM content packages
* Manage ANY files for dispatcher configuration

This plugin depends on the [CONGA Sling Plugin][conga-sling].

Additionally the CONGA AEM Maven plugin is provided which allows to deploy a bunch of AEM packages processed by CONGA to an AEM instance. See [Usage][usage] for an example.


### Further Resources

* [wcm.io CONGA training material with exercises](http://training.wcm.io/conga/)
* [adaptTo() 2015 Talk: CONGA - Configuration generation for Sling and AEM](https://adapt.to/2015/en/schedule/conga---configuration-generation-for-sling-and-aem.html)
* [adaptTo() 2017 Talk: Automate AEM Deployment with Ansible and wcm.io CONGA](https://adapt.to/2017/en/schedule/automate-aem-deployment-with-ansible-and-wcm-io-conga.html)



[usage]: usage.html
[extensions]: extensions.html
[apidocs]: conga-aem-plugin/apidocs/
[plugindocs]: conga-aem-maven-plugin/plugin-info.html
[changelog]: changes-report.html
[aem]: http://www.adobe.com/solutions/web-experience-management.html
[conga]: http://devops.wcm.io/conga/
[conga-sling]: http://devops.wcm.io/conga/plugins/sling/
[handlebars-helpers]: handlebars-helpers.html
[crypto-cli]: crypto-cli.html
