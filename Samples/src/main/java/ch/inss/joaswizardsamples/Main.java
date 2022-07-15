package ch.inss.joaswizardsamples;

public class Main {

    /** Create OAS3 document with all CRUD operations for one object. */
    public static void mainx(String[] args) {
        if (args.length == 0) {
            help();
            System.exit(0);
        }
        Sample sample = new Sample();
        if (args[0].equals("1") || args[0].equalsIgnoreCase("yaml")) {
            sample.createOpenApiFromYaml();
        }else if (args[0].equals("2") || args[0].equalsIgnoreCase("yamlobjects")){
                sample.createOpenApiFromMultipleYamlObjects();
        }else if (args[0].equals("3") || args[0].equalsIgnoreCase("excel")) {
            sample.createOpenApiFromExcel("objectimport.xlsx");
        } else if (args[0].equals("4") || args[0].equalsIgnoreCase("mysqlexcel")) {
            sample.createOpenApiFromExcel("mysql/mySQLObjectimport.xlsx", "mysql/mysqlMapping.json", "openapi_fromMySQLExcel.yaml");
        } else {
            sample.createOpenApiFromArguments(args);
        }
    }

    private static void help() {
        System.out.println("This generates sample openapi specification.");
        System.out.println("Provide one parameter of these:");
        System.out.println("1 - Using the file pet.yml to define properties for an object. Jo will create an OpenAPI document with CRUD operations.");
        System.out.println("2 - Using the file YamlObjects.yml to define properties for several objects. Jo will create an OpenAPI document with CRUD operations.");
        System.out.println("3 - Using the MS Excel file objectimport.xlsx to define properties for a number of objects. Jo will create an OpenAPI document with GET (only) operations.");
        System.out.println("4 - Using the MS Excel file mysql/mySQLObjectimport.xlsx to define properties for a number of objects. It uses a custom mapping for MySQL database types (mysql/mysqlMapping.json).");
        System.out.println("Output will be a yaml file containing the OpenAPI specification with all paths and objects as defined from Yaml samples or Excel files.");
    }
}
