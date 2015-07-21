## CONGA - Extensions

The CONGA AEM Plugin extends CONGA using its [extensibility model][conga-extensibility].


### Provided Plugins

File plugins:

| Plugin name                     | File name(s)        | File Header | Validator | Escaping | Post Processor |
|---------------------------------|---------------------|:-----------:|:---------:|:--------:|:--------------:|
| `any`                           | .any                | X           | X         | X        |                |
| `aem-contentpackage`            | .json               |             |           |          | X              |
| `aem-contentpackage-osgiconfig` | .provisioning, .txt |             |           |          | X              |


[conga-extensibility]: http://devops.wcm.io/conga/extensibility.html
