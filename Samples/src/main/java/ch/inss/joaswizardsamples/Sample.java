package ch.inss.joaswizardsamples;

import ch.inss.openapi.joaswizard.InputParameter;
import ch.inss.openapi.joaswizard.Joaswizard;

import java.io.InputStream;
import java.io.InputStreamReader;

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
        boolean ok = joaswizard.createCrudFromYamlToFile(inputParameter);
        System.out.println();
        if(ok){
            System.out.println("OpenAPI document created: openapi_fromyaml.yaml");
        }else{
            System.out.println("An error occurred, document creation failed.");
        }
    }

    public void createOpenApiFromMultipleYamlObjects() {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile("./YamlObjects.yml");
        inputParameter.setOutputFile("openapi_frommultipleyamlobjects.yaml");
        inputParameter.setResource("pet");
        inputParameter.setResourceId("id");
        inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        Joaswizard joaswizard = new Joaswizard();
        boolean ok = joaswizard.createCrudFromYamlToFile(inputParameter);
        System.out.println();
        if(ok){
            System.out.println("OpenAPI document created: openapi_frommultipleyamlobjects.yaml");
        }else{
            System.out.println("An error occurred, document creation failed.");
        }
    }

    public void createOpenApiFromExcel(String file) {
        this.createOpenApiFromExcel(file, null);
    }
    public void createOpenApiFromExcel(String file, String mappingFile) {
        this.createOpenApiFromExcel(file, null, null);
    }
    public void createOpenApiFromExcel(String file, String mappingFile, String outputFile) {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(file);
        if(outputFile == null){
            inputParameter.setOutputFile("openapi_fromexcel.yaml");
        }else{
            inputParameter.setOutputFile(outputFile);
        }
        if(mappingFile != null){
            inputParameter.setMappingFile(mappingFile);
            inputParameter.setPrefixMatch(true);
        }
        inputParameter.setResourceId("id");
        /* Define the input to come from an Excel file. */
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        /* This is not the CRUD method. So define the rest methods you want to get in the OpenAPI document. */
        inputParameter.addMethods("get");
        /* Process file and generate OpenAPI specification which will be in the output folder. */
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createFromExcelToFile(inputParameter);
        System.out.println();
        System.out.println("OpenAPI document created: " + inputParameter.getOutputFile());
    }

    public String createOpenApiFromExcelInputStream(InputStream reader) {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setResourceId("id");
        inputParameter.addMethods("get");
        inputParameter.setInputStream(reader);
        /* Process file and generate OpenAPI specification which will be in the output folder. */
        Joaswizard joaswizard = new Joaswizard();
        String result = joaswizard.createFromExcelInputstreamToString(inputParameter, reader);
        return result;
    }

    public void createOpenApiFromArguments(String[] args) {
        System.out.println("Start to create OpenAPi from arguments.");
        if (args.length < 3) {
            System.out.println("Need four parameter.");
            System.out.println("Parameters are <input.yaml> <output.yaml> <objectname> <idfieldname> <sourcetype> <methods>");
            System.out.println("Check README.md for more details.");
            System.exit(1);
        }
        ch.inss.openapi.joaswizard.Main.main(args);

    }
}
