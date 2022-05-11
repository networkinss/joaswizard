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
* You want to create OpenAPI specifications programmatically.
* You have to process mass object data like from an Excel file.
* You want a head start without knowing OpenAPI.

## Status

It is still in development.  
Implemented features:
* Create OpenAPI document with all CRUD operations just from object properties.
* Create OpenAPI document for several objects and defined methods from Excel data.
* Create OpenAPI document from a Yaml object and defined methods.

Missing:  
Parameter to use custom Handlebar template files.

## Build

It is a normal Maven project. You will need Java SDK 11 and Maven to build it.  
`mvn clean install`

## Maven Integration

Add dependency to pom.xml.  
`<dependency>     
    <groupId>ch.inss.joaswizard</groupId>     
    <artifactId>joaswizard</artifactId>   
    <version>0.2.3</version>   
</dependency>`  

## Usage

You can use Joaswizard either as a command line tool or as a library.

### as command line tool

Jo as wizard needs three arguments.  
<input.yaml> is a file which contains properties in yaml style.    
Sample is in src/test/resources/Contact.yml  
<output.yaml> is the name of the output file.  
<objectname> is the name of the object which you defined in the <input.yaml> file.  
<idfieldname> must be a fieldname used in <input.yaml> file to define which field shall be used as an ID.

`java -jar joaswizard-*.jar <input.yaml> <output.yaml> <objectname> <idfieldname>  `

Example:  
`java -jar joaswizard-*.jar pet-oas.yaml petSample.yaml pet name`

### as a library

Simply define an object of the class InputParameter.  
Use any of the public methods of the class Joaswizard.

### samples for usage

Samples are in the folder Samples.

## Template adjustment
To fill in your own details into the info section just adjust the files ending with .hbs (Handlebars) 
in src/main/resources/ accordingly.