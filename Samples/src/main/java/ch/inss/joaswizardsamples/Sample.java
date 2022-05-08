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
        inputParameter.setOutputFile("openapi_fromyaml.yaml");
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
        inputParameter.setOutputFile("openapi_fromexcel.yaml");
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
}
