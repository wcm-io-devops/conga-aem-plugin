## CONGA - Custom Handlebars expressions for AEM

By using CONGA handlebars helper plugins it is possible to extend handlebars by registering custom expressions. Out of the box CONGA AEM plugin ships with a set of built-in custom expressions documented in this chapter.

The basic handlebars expressions are documented in the [Handlebars quickstart][handlebars-quickstart]. CONGA itself also ships with a set of [Custom Handlebars expressions][conga-handlebars-helper].


### oakPasswordHash

Generates a password hash for an Oak JCR user from a plain text password.

```
{{oakPasswordHash passwordVariable}}
```


### oakAuthorizableUuid

Generates a UUID for an authorizable node by deriving it from the authorizable Id.

```
{{oakAuthorizableUuid authorizableId}}
```


### aemHttpdFilter

Generates HTTPd allow from/required rules for a filter expression. Supports both Apache 2.2 and 2.4. See [CONGA AEM Definitions][aem-definitions] for an usage example.

```
# Location filter
{{#each httpd.accessRestriction.locationFilter~}}
{{aemHttpdFilter this allowFromKey="httpd.accessRestriction.adminAccessFromIp" allowFromHostKey="httpd.accessRestriction.adminAccessFromHost"}}
{{/each~}}
```


### aemDispatcherFilter

Generates AEM dispatcher filter rules for a filter expression. See [CONGA AEM Definitions][aem-definitions] for an usage example.

```
  /filter
    {
{{~#each dispatcher.filter}}
      /{{@index}}
        {{{aemDispatcherFilter this}}}
{{~/each}}
    }
```



[handlebars-quickstart]: ../../handlebars-quickstart.html
[conga-handlebars-helper]: ../../handlebars-helper.html
[aem-definitions]: ../../definitions/aem/
