# Jo as wizard

Jo as wizard (short: Jo, coming from J OAS wizard) helps to create a complete OAS3 specification without having to know OpenAPI.      
All you need to do is to define the properties of an object in Yaml format, the name of the object and the field name
used for the ID.  
Only with that information will Jo as wizard create a complete OpenAPi specification which includes:

* CRUD operations (GET all, GET by id, POST, PUT, DELETE).
* Object schemas.
* Default info section

First aim is to create a complete OpenAPI specification with a minimum of data. 
All you need is a file with a content like:
```
name: Underdog  
price: 12.05  
status: "available"  
tags: [ dog,4paws ]  
```
and you will be able to create a full OAS3 document with one execution and a few parameters.  
Second aim is to generate mass data from Excel files.  

Repository: https://github.com/networkinss/joaswizard.  
This project is inspired by https://github.com/isa-group/oas-wizard.  
It is a complete re-write in Java instead of Node.js and a lot of extensions.  
This is for better integration in Java applications, but also to be more flexible handling mustache engine.

## Use case

This library will help you in case of:
* You want to create OpenAPI specifications programmatically.
* You have to process mass object data like from an Excel file.
* You want a head start without knowing OpenAPI.
* You want to use your database structure to generate the OAS3 objects.


## Features

It is still in development.  
Implemented features:
* Create OpenAPI document with all CRUD operations just from object properties.
* Create OpenAPI document with defined list of methods.
* Create OpenAPI document from a Yaml object file.
* Create OpenAPI document from a Yaml object string.
* Create OpenAPI document from an Excel workbook with several sheets, each representing one object.

Future extensions:
Parameter to use custom Handlebar template files.
Include tags for generation.

## Build

It is a normal Java Maven project. You will need Java SDK 11 and Maven to build it.  
`mvn clean install`
I forked Handlebars to remove logging from it. The logging binding from Handlebars can cause issues with your own
application.

## Maven Integration

Add dependency to pom.xml.
```
<dependency>     
    <groupId>ch.inss.joaswizard</groupId>     
    <artifactId>joaswizard</artifactId>   
    <version>0.9.2</version>   
</dependency>  
```

## Usage

You can use Joaswizard either as a command line tool or as a library.

### as command line tool

Jo as wizard needs several arguments.  
They have to be provided in the right sequence. If you skip one, Jo will ask for it.   
Since the sequence is important, you can skip arguments only from right to left.
```
java -jar joaswizard-*.jar <input.yaml> <output.yaml> <objectname> <idfieldname> <sourcetype> <methods>
```
* `<input.yaml>` is a file which contains properties in yaml style. A simple sample is in Samples/pet.yml.    
* `<output.yaml>` is the name of the output file.  
* `<objectname>` is the name of the object which you defined in the <input.yaml> file.  
* `<idfieldname>` must be a fieldname used in <input.yaml> file to define which field shall be used as an ID. Will be the path variable for ID requests.
* `<sourcetype>` is one of yamlfile, excelfile or yamlstring.
* `<methods>` are all REST methods that shall be used. You can use crud or post, put, get, delete and patch.  


You can execute it without parameter. Jo will ask for the parameter.  
`java -jar joaswizard-*.jar`

### Samples for commandline 

With all parameters. Jo would ask for skipped parameters, if any.  
`java -jar joaswizard-*.jar Samples/pet.yml openapipet.yaml pet name yamlfile delete,post,patch`


### as a library

Simply define an object of the class InputParameter.  
Use any of the public methods of the class Joaswizard.

### Samples for library

You find samples how to use Jo as a library in the folder Samples.

### Input data

You can define an object either in Yaml format (file or string).  
Or in an Excel workbook with many sheets, one for each object.

The approach with both formats is quite different, and you would get different results if you were using the same input
data for both.  
In Excel you define precisely what OAS type shall be used.  
In Yaml you give sample data and Jo will guess the OAS type based on that.

#### Yaml

Yaml is easy.  
You can either point to the file which contains some Yaml style properties or put direcly a string.
A minimum may not even contain the object, but it will be asked for using the command line.  
Example for a minimal file content:

```
name: Underdog  
price: 12.05  
status: "available"  
tags: [ dog,4paws ]  
```

However, usually you will define more objects, including object names.  
In the below example 'Underdog' will be a string, amount an integer, price a number, tags an array of string, and
.possiblerabatt an array of integers.

```
PET:
  name: Underdog
  amount: 3
  price: 12.05
  status: "available"
  tags: [ dog,4paws ]
  possiblerabatt: [10,15,20]
Customer:
  firstname: John
  lastname: Doe
  age: 22
  city: Zurich
invoice:
  product: Pet
  amount: 1
  price: 12.05
```

Sample file for Yaml:
`src/test/resources/sample.yaml`

Or you can put the Yaml formatted string directly.  
You can check the JUnit test `testCreateFromString()`.

#### Excel

Excel is very useful for mass conversion.  
Every sheet is an component schema object. The sheetname is the name of the object.    
First line can contain the following header:

* Name -> Required. Name of the property of the object.
* DbType -> Database type. This can be mapped as defined in the mapping.json (src/main/resources/mapping.json). Will be
  used only if OasType is empty.
* OasType -> The actual OAS type. A list of OAS3 types is in doc/oas3datatypes.txt.
* OasFormat -> The format of the type. Possible values also in the file doc/oas3datatypes.txt.
* OASPattern -> Can be a pattern like e.g. for a date: yyyy-mm-dd.
* OasMin -> The OAS3 minimum length of a value for that property.
* OasMax -> The OAS3 maximum length of a value for that property.
* OasExample -> The OAS3 example value. If not set, Jo will guess a bit.
* OasEnum -> OAS3 Enum values.
* OasDescription -> Text description for the OAS3 property.
* OasRequired -> If required or not. Values are true (required) or false (optional).
* DbNullable -> If required or not (true/false), but with opposite value like OasRequired. True means field value is optional. False means it is required.

Excel headers are not case-sensitive.  
the `mapping.json` is the mapping file for the field `DbType`.
There is a default mapping.json in the application.  
However, it will first look if there is one mapping.json in the same folder and take
that one if available.  
Have a look into `src/main/resources/mapping.json` to get to know the structure.  
A custom mapping can be defined like for MySQL dataabase types like here:
`Samples/mysql/mysqlMapping.json`  
DbType contains the database type. This field can be use instead of OasType if you take the structure from a database
DDL.  
OasType, OasFormat, OasPattern can be filled with desired mapping for the generated OAS3 document.  
All field names must be lowercase in the json file.

Sample Excel file:
`src/test/resources/objectimport.xlsx`

### Sample code

Samples are in the folder Samples.  
There are sample methods how to use Yaml properties as input or Excel files.
There is also a method to show how to use custom mappings DBTypes to OASTypes for MySQL database types.  
With the correct mapping you don't have to define each OASType manually.  
Also the JUnit tests of Samples
Samples/src/test/java/ch/inss/joaswizardsamples/SampleTest.java
and from
src/test/java/ch/inss/joaswizard/JoaswizardTest.java
can be used to see how it works.

## Template adjustment

To fill in your own details into the info section just adjust the files ending with .hbs (Handlebars)
in src/main/resources/ accordingly.