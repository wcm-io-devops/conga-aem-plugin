## CONGA AEM Plugin - Usage

For basic CONGA usage see [CONGA documentation][conga-usage].


### CONGA AEM Plugin

An example using the CONGA AEM plugin-specific features:<br/>
https://github.com/wcm-io-devops/conga-aem-plugin/tree/develop/tooling/conga-aem-maven-plugin/src/it/example


### CONGA Maven AEM Plugin

This is an AEM-specific CONGA plugin for Maven, not to be mixed up with the generic CONGA plugin for Maven which is used to generate the configuration.

The CONGA AEM Maven plugin allows to deploy a bunch of AEM packages processed by CONGA to an AEM instance. It requires the CONGA configuration to be generated before, and a `model.yaml` needs to be located in each node's root folder (this is activated by default).

Example for configuration the plugin in your POM:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>io.wcm.devops.conga.plugins</groupId>
      <artifactId>conga-aem-maven-plugin</artifactId>
      <configuration>
        <nodeDirectory>target/configuration/env1/node1</nodeDirectory>
        <serviceURL>http://localhost:4502/crx/packmgr/service</serviceURL>
        <userId>admin</userId>
        <password>admin</password>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This looks for a file `target/configuration/env1/node1/model.yaml`, identifies all ZIP files that are AEM content packages and uploads them to an AEM instance when you execute `mvn conga-aem:package-install`.

The plugin uses the same resilience logic for package uploading as the [wcm.io Content Package Maven Plugin][wcmio-content-package-maven-plugin].


[conga-usage]: https://devops.wcm.io/conga/usage.html
[wcmio-content-package-maven-plugin]: https://wcm.io/tooling/maven/plugins/wcmio-content-package-maven-plugin/
