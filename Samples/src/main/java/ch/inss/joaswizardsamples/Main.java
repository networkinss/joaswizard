package ch.inss.joaswizardsamples;

public class Main {

    /** Create OAS3 document with all CRUD operations for one object. */
    public static void main(String[] args) {
        if (args.length == 0) {
            help();
        }
        Sample sample = new Sample();
        if (args[0].equals("1") || args[0].equalsIgnoreCase("yaml")){
            sample.createOpenApiFromYaml();
        }else if (args[0].equals("2") || args[0].equalsIgnoreCase("excel")){
            sample.createOpenApiFromExcel();
        }else{
            System.out.println("Please choose either \"1\" or \"2\".");
            help();
        }
    }

    private static void help() {
        System.out.println("This generates sample openapi specification.");
        System.out.println("1 - Use the file Pet.yml to define properties for an object. Jo will create an OpenAPI document with CRUD operations.");
        System.out.println("2 - Use the MS Excel file objectimport.xlsx to define properties for a number of objects. Jo will create an OpenAPI document with GET operations.");
        System.out.println("Output will be a yaml file containing the OpenAPI with properties from the user defined properties.");
        System.exit(0);
    }
}
