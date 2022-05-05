# Jo as wizard

Jo as wizard (short: Jo, coming from J OAS wizard) helps to create a complete OAS3 specification without having to know OpenAPI.      
All you need to do is to define the properties of an object in Yaml format, the name of the object and the field name
used for the ID.  
Only with that information will Jo as wizard create a complete OpenAPi specification which includes:

* CRUD operations (GET all, GET by id, POST, PUT, DELETE).
* Object schemas.
* Default info section

Repository: https://github.com/networkinss/joaswizard.  
This project is inspired by https://github.com/isa-group/oas-wizard.  
It is a complete re-write in Java instead of Node.js and a lot of extensions.  
This is for better integration in Java applications, but also to be more flexible handling mustache engine.

## Use case

This library will help you in case of:
* You need to create OpenAPI specifications programmatically.
* You have to process mass data like from an Excel file.
* You need a head start without knowing OpenAPI.

## Status

It is still in development and some features are still missing.  
Implemented so far:
* Create full OpenAPI document just from object properties.
* Create REST CRUD operations for an object.
* Create schemas from Excel data.

## Build

It is a normal Maven project. You will need Java SDK 11 and Maven to build it.  
`mvn clean install`

## Integration

For integration as a library you only need to instantiate Joaswizard, fill out the Parameter object and execute
joaswizard.createCrudFile(parameter).

## Execution

Jo as wizard needs three arguments.  
<input.yaml> is a file which contains properties in yaml style.    
Sample is in src/test/resources/Contact.yml  
<output.yaml> is the name of the output file.  
<objectname> is the name of the object which you defined in the <input.yaml> file.  
<idfieldname> must be a fieldname used in <input.yaml> file to define which field shall be used as an ID.

`java -jar joaswizard-*.jar <input.yaml> <output.yaml> <objectname> <idfieldname>  `

Example:  
`java -jar joaswizard-*.jar pet-oas.yaml petSample.yaml pet name`

## Template adjustment
To fill in your own details into the info section just adjust the file src/main/resources/crud.yaml accordingly.