## CONGA - Extensions

The CONGA AEM Plugin extends CONGA using its [extensibility model][conga-extensibility].


### Provided Plugins

File plugins:

| Plugin name                     | File name(s)        | File Header | Validator | Escaping | Post Processor |
|---------------------------------|---------------------|:-----------:|:---------:|:--------:|:--------------:|
| `any`                           | .any                | X           | X         | X        |                |
| `aem-contentpackage`            | .json               |             |           |          | X              |
| `aem-contentpackage-osgiconfig` | .provisioning, .txt |             |           |          | X              |
| `aem-contentpackage-properties` | .zip                |             |           |          | X              |

Please note: Files with the extension `txt` are only managed as provisioning files if the contain the string `[feature `.


### Generating AEM Content Packages

The CONGA AEM Plugin provides two post processor plugins that generate AEM Content Packages (ZIP files with JCR XML files describing a JCR content structure). These content packages can be imported into a AEM instance using the CRX Package Manager.

The plugin `aem-contentpackage` generates a content package out of a JSON file. This JSON files contains JCR content as produced by the Sling GET Servlet.

The plugin `aem-contentpackage-osgiconfig` generates a content package containing OSGI configurations out of a Sling Provisioning file.

The Sling Provisioning Model file format is described on the [Sling Website][sling-provisioning]. It is a compact format that allows to define features with bundles and configurations for a Sling-based distribution. The CONGA AEM Plugin uses only the configurations and ignores all other parts of the file.

Both post processor plugins support a set of options that allow further configuration of the generated content package:

| Property                     | Description
|------------------------------|-------------
| `contentPackage.group`       | Group name for content package
| `contentPackage.name`        | Package name for content package
| `contentPackage.description` | Description for content package
| `contentPackage.version`     | Version for content package
| `contentPackage.rootPath`    | Root path for content package (simplified version for setting just one filter)
| `contentPackage.filters`     | Contains list with filter definitions, optionally with include/exclude rules


Example for defining package properties with a set of filters:

```
# Define a AEM content package with some JCR content (Sling Mapping Example)
- file: sling-mapping.json
  dir: packages
  template: sling-mapping.json.hbs
  # Post-processors apply further actions after generation the file
  postProcessors:
  - aem-contentpackage
  postProcessorOptions:
    contentPackage:
      name: mapping-sample
      rootPath: /etc/map/http
      filters:
      - filter: /etc/map/http
        rules:
        - rule: exclude
          pattern: /etc/map/http
        - rule: include
          pattern: /etc/map/http/.*
        - rule: exclude
          pattern: /etc/map/http/AppMeasurementBridge
```


### Post-processing AEM Content Packages

With the post-processor plugin `aem-contentpackage-properties` is applied automatically to all ZIP files manged by CONGA. The properties contained in the AEM package are extracted and included per file in a property `aemContentPackageProperties` in the model YAML.

The model YAML file can be exported during CONGA generation and provides the necessary runtime information for deployment tools like Ansible.


[conga-extensibility]: http://devops.wcm.io/conga/extensibility.html
[sling-provisioning]: https://sling.apache.org/documentation/development/slingstart.html
