package ch.inss.joaswizardsamples;

import ch.inss.joaswizard.InputParameter;
import ch.inss.joaswizard.Joaswizard;

public class Sample {

    /**
     * Create OAS3 document with all CRUD operations for one object.
     */
    public void createOpenApiFromYaml() {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile("./pet.yml");
        inputParameter.setOutputFile("output/openapi_fromyaml.yaml");
        inputParameter.setResource("pet");
        inputParameter.setResourceId("id");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
        System.out.println("OpenAPI document created: output/openapi_fromyaml.yaml");
        
    }
    public void createOpenApiFromExcel() {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile("./objectimport.xlsx");
        inputParameter.setOutputFile("output/openapi_fromexcel.yaml");
//        inputParameter.setResource("pet");
        inputParameter.setResourceId("id");
        /* Define the input to come from an Excel file. */
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        /* This is not the CRUD method. So define the rest methods you want to get in the OpenAPI document. */
        inputParameter.addMethod("get");
        /* Process file and generate OpenAPI specification which will be in the output folder. */
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createFromExcel(inputParameter);
        System.out.println("OpenAPI document created: output/openapi_fromexcel.yaml");
    }

    public void createOpenApiFromArguments(String[] args) {
        System.out.println("Start to create OpenAPi from arguments.");
        if (args.length < 3) {
            System.out.println("Need four parameter.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield>");
            System.exit(1);
        }
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(args[0]);
        inputParameter.setOutputFile(args[1]);
        inputParameter.setResource(args[2]);
        if (args.length >= 4) {
            inputParameter.setResourceId(args[3]);
        } else {
            inputParameter.setResourceId("id");
        }

        if (args.length >= 5) {
            inputParameter.setSourceType(args[4]);
        } else {
            inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }
}
