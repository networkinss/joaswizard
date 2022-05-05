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

The generated file will be in the folder "output".  
Copy the entire text content from the file and put into the Swagger edtior:  
https://editor.swagger.io

## Example 1 with a yaml file
This will generate an OpenAPI file with all CRUD operations (POST, PUT, DELETE, GEt one object by id, GET all objects).  
It will use the defined properties from the pet.yml file.  
Execute the Jar with yaml or just 1.
```
java -jar target/samples.jar yaml
```
Output will be in the same folder in the yaml file openapi_fromyaml.yaml.

## Example 2 with a yaml file
This will generate an OpenAPI file with GET operations (GEt one object by id, GET all objects).  
It will use the defined object properties from the objectimport.xlsx file.  
Execute the Jar with parameter exel or just 2.
```
java -jar target/samples.jar excel
```
Output will be in the same folder in the yaml file openapi_fromexcel.yaml.  
