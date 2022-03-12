# Jo as wizard

Jo as wizard (short: Jo, coming from J OAS wizard) shall help to create a complete OAS3 specification without knowing
OpenAPI.    
All you need to do is to define the properties of an object in Yaml format, the name of the object and the fieldname
used for the ID.  
Only with that informations Jo as wizard will create a complete OpenAPi specification which includes:

* CRUD operations (GET all, GET by id, POST, PUT, DELETE).
* Object defintion
* Default info section

This project is inspired by https://github.com/isa-group/oas-wizard.  
It is a complete re-write in Java instead of Node.js.  
This is for better integration in Java applications, but also to be more flexible handling mustache engine.

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