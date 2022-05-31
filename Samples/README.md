# Samples for Jo as wizard

You can generate the sample code with Maven:
```
mvn clean package
```
Now you can execute Jo samples with:
```
java -jar target/samples.jar <sample>
```
or without parameter to get the help message.  
`<sample>` is one of a number 1 to 4 or yaml, yamlobjects, excel or mysqlexcel.

The generated file will be in the same folder.  
Copy the entire text content from the file and put it into the Swagger editor:  
https://editor.swagger.io

## Example 1 with a yaml file

This will generate an OpenAPI file with all CRUD operations (POST, PUT, DELETE, GET one object by id, GET all objects)
.  
It will use the defined properties from the pet.yml file.  
Execute the Jar with parameter yaml or just 1.

```
java -jar target/samples.jar yaml
```

Output will be in the same folder in the yaml file openapi_fromyaml.yaml.

## Example 2 with several objects in Yaml format

This will generate an OpenAPI file with CRUD operations for all defined objects.  
It will use the object properties from the YamlObjects.yml file.  
Execute the Jar with parameter yamlobjects or just 2.

```
java -jar target/samples.jar yamlobjects
```

Output will be in the same folder in the yaml file openapi_frommultipleyamlobjects.yaml

## Example 3 with several objects from an Excel file

This will generate an OpenAPI file with GET operations (GEt one object by id, GET all objects).  
It will use the defined object properties from the objectimport.xlsx file.  
The OAS types are defined together with other properties in the Excel file.
Execute the Jar with parameter excel or just 3.

```
java -jar target/samples.jar excel
```

Output will be in the same folder in the yaml file openapi_fromexcel.yaml.

## Example 4 with several objects from an Excel file with MySQL mappings

This will generate an OpenAPI file with GET operations (GEt one object by id, GET all objects).  
It will use the defined object properties from the mysql/mySQLObjectimport.xlsx file.  
It has not defined the OAS types. Instead, it uses just the MySQL database types like from a
MySQL database DDL description.  
It will map it to OAS3 types as defined in the file mysql/mysqlMapping.json.
Execute the Jar with parameter yamlobjects or just 2.

```
java -jar target/samples.jar mysqlexcel
```

Output will be in the same folder in the yaml file openapi_fromMySQLExcel.yaml.  
