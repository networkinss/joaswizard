# Jo as wizard

Jo as wizard (short: Jo, coming from J OAS wizard) helps to create a complete OAS3 specification without having to know
OpenAPI.      
All you need to do is to define the properties of an object in Yaml format or an Excel file, the name of the object and
the field name
used for the ID.  
Only with that information will Jo as wizard create a complete OpenAPi specification within 5 minutes which includes:

* CRUD operations (GET all, GET by id, POST, PUT, DELETE).
* Object schemas.
* Default info section

It is possible to create a complete OpenAPI specification with a minimum of effort and data.
All you need is a Yaml formatted file with a content like:
```
name: Underdog  
price: 12.05  
status: "available"  
tags: [ dog,4paws ]  
```

and you will be able to create a full OAS3 document with one execution and a few parameters.  
Second option is to generate mass data from Excel files.

Repository: https://github.com/networkinss/joaswizard.  
This project was initially inspired by https://github.com/isa-group/oas-wizard.  
It is a complete re-write in Java instead of Node.js and a lot of dynamic parameters and extensions.  
This is for better integration in Java applications, but also to be more flexible handling Handlebars engine.

## Use case

This library will help you in case of:
* You want to create OpenAPI specifications programmatically.
* You have to process mass object data like from an Excel file.
* You want a head start without knowing OpenAPI.
* You want to use your database structure to generate the OAS3 objects.

## License

The code is licensed under the faircode license:
https://faircode.io/

See LICENSE.md.

## Features

Implemented features:
* Create OpenAPI document with all CRUD operations just from object properties.
* Create OpenAPI document with defined list of methods.
* Create OpenAPI document from a Yaml object file.
* Create OpenAPI document from a Yaml object string.
* Create OpenAPI document from an Excel workbook with several sheets, each representing one object.
* Define your own mapping as needed for database to OAS3 types.

Possible future extensions:  
Parameter to use custom Handlebar template files.  
Include tags for generation.
Take JSON as input.

## Build

It is a normal Java Maven project. You will need Java SDK 17 and Maven to build it.

Joaswizard depends on a forked Handlebars (`com.github.jknack:handlebars`, version `4.3.2-FORK`), which is not on Maven Central.
The original Handlebars ships with logging (slf4j) that can interfere with applications using it, so the fork removes the logging from the `handlebars` module.
Install the fork into your local Maven repository first (https://github.com/networkinss/handlebars.java):

```
git clone https://github.com/networkinss/handlebars.java.git
```

```
cd handlebars.java
```

```
mvn install -pl handlebars -am -DskipTests
```

Only the `handlebars` module and its parents are built. If the full reactor of the fork fails (for example in the `handlebars-maven-plugin` modules), this command is all you need.
The CI build (`.github/workflows/build.yml`) uses the same command on the fork commit `cee2cc3ceeb8222da39c6932c765cebc3de5e241`.

Then build Joaswizard:

```
mvn clean install
```

The build produces two jars:

* `target/joaswizard-<version>.jar` is the library. It bundles the Handlebars fork relocated to `ch.inss.openapi.joaswizard.internal.handlebars`, so users of the library need nothing extra (the fork is not a dependency in the published pom).
* `target/joaswizard-<version>-cli.jar` is the command line fat jar with all dependencies.

The fork is only needed when building Joaswizard from source.

To check the API style of an output:

```
npx @stoplight/spectral-cli lint src/test/resources/testReferenceCrudMultipleYamlObject.yml -r .spectral.json
```

Optionally write the result as JUnit XML:

```
npx @stoplight/spectral-cli lint src/test/resources/testReferenceCrudMultipleYamlObject.yml -r .spectral.json -o test-results/spectral-results.xml -f junit
```

## Maven Integration

Add dependency to pom.xml.
```
<dependency>     
    <groupId>ch.inss.joaswizard</groupId>     
    <artifactId>joaswizard</artifactId>   
    <version>0.9.4</version>   
</dependency>  
```

## Usage

You can use Joaswizard either as a command line tool or as a library.  
Its main purpose is to work as a library.  
However, the command line can be used to quickly create OAS3 documents within some minutes.

### as command line tool

Jo as wizard needs several arguments.  
They have to be provided in the right sequence. If you skip one, Jo will ask for it.   
Since the sequence is important, you can skip arguments only from right to left.
```
java -jar joaswizard-*-cli.jar <input.yaml> <output.yaml> <objectname> <idfieldname> <sourcetype> <methods>
```
* `<input.yaml>` is a file which contains properties in yaml style. A simple sample is in Samples/pet.yml.    
* `<output.yaml>` is the name of the output file.  
* `<objectname>` is the name of the object which you defined in the <input.yaml> file.  
* `<idfieldname>` must be a fieldname used in <input.yaml> file to define which field shall be used as an ID. Will be the path variable for ID requests.
* `<sourcetype>` is one of yamlfile, excelfile or yamlstring.
* `<methods>` are all REST methods that shall be used (comma separated). You can use crud or post, put, get, delete and
  patch.

You can execute it without parameter. Jo will ask for the parameter.  
`java -jar joaswizard-*-cli.jar`

### Sample for commandline

With all parameters. Jo would ask for skipped parameters, if any.  
`java -jar joaswizard-*-cli.jar Samples/pet.yml openapipet.yaml pet name yamlfile delete,post,patch`


### as a library

Define an object of the class InputParameter and call `generate` (returns the OpenAPI document as string) or
`generateToFile` (also writes it to `InputParameter.getOutputFile()`) of the class Joaswizard.  
Both return a `GenerationResult`. They do not throw for invalid input and keep no state, so one `Joaswizard` instance can
be shared between threads.

```java
InputParameter in = new InputParameter();
in.setInputFile("Samples/pet.yml");
in.setOutputFile("openapipet.yaml");
in.setResource("pet");
in.setResourceId("name");
in.setCrud();

GenerationResult result = new Joaswizard().generateToFile(in);
for (Diagnostic warning : result.getWarnings()) {
    System.out.println(warning);
}
if (result.isOk()) {
    System.out.println("Written to " + result.getOutputFile());
} else {
    result.getErrors().forEach(System.err::println);
}
```

* `getSpec()` is the OpenAPI document, `null` if the generation failed.
* `getDiagnostics()` lists all errors and warnings in the order of occurrence (`getErrors()` and `getWarnings()` filter
  them). Each `Diagnostic` has a `getSeverity()` (`ERROR` or `WARNING`) and a `getMessage()`.
* `isOk()` is true if a spec was generated and there is no error.
* `getOutputFile()` is set by `generateToFile` if the file was written. If writing fails, the result contains an error.

The older methods (`createFromYamlToString`, `createFromExcelToFile`, `createCrudFrom...`, the `getErrorMessage()`
getters and so on) are deprecated and still work as before. They are wrappers of the new methods, and the messages
(`getErrorMessage()`, `getWarningMessage()`) now only hold the last error/warning of the latest call instead of
leaking from earlier calls.

### Samples for library

You find samples how to use Jo as a library in the folder Samples.

### Input data

You can define an object either in Yaml format (file or string).  
Or in an Excel workbook with many sheets, one for each object.

The approach with both formats is quite different, and you would get different results if you were using the same input
data for both.  
In Excel you define precisely what OAS type shall be used (maybe with mapping).  
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
You can check the JUnit test `testCreateFromString()` for that.  

##### Nested objects

- If every top-level value is an object, each key is a resource (as above). Otherwise the whole document is one object (named by the resource parameter).
- An object value becomes a schema of its own, named after its key (`address` gives `Address`), and the property is a `$ref` to it. This works to any depth.
- A list of objects becomes an array whose `items` is a `$ref` to a schema named after the list key, with the union of all element keys.
- A name already used by a different shape gets the parent schema name as prefix (`CustomerAddress`); an identical shape is reused.
- An object with the single key `$ref` is passed through unchanged. A `$ref` property has no siblings (no type, example, ...) but is still `required`.
- Only top-level resources get CRUD paths and count for `maxobjects` and the title. Nested schemas follow their resource in `components/schemas`.

```yaml
Customer:
  firstname: John
  address:
    street: Main Street
    nr: 48
```

generates `Customer` with `address: $ref: '#/components/schemas/Address'` and a schema `Address` (no paths). Samples: `src/test/resources/nested/`.

#### Excel

Excel is very useful for mass conversion.  
Every sheet is an component schema object. The sheetname is the name of the object.    
First line (headers) can contain the following header names:

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
The `mapping.json` is the mapping file for the field `DbType`.
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
There are sample methods how to use Yaml properties or Excel files as input.  
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