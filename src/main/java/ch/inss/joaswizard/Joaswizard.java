package ch.inss.joaswizard;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Author: Oliver Glas, https://inss.ch.
 */
public class Joaswizard implements Constants {

    private static Logger logger = null;

    public Joaswizard() {
        FileHandler fileHandler = null;
        try {
            InputStream stream = Joaswizard.class.getClassLoader().getResourceAsStream("logging.properties");
            try {
                LogManager.getLogManager().readConfiguration(stream);
                logger = Logger.getLogger(Joaswizard.class.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler("joaswizard.log");
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        logger.addHandler(fileHandler);
    }

    /**
     * Create all CRUD operations for one object.
     */
    public void createCrudFile(InputParameter inputParameter) {
        logger.info("Starting create crud file.");
        String resultSchema = this.fromCrudTemplate(inputParameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            logger.severe("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    /**
     * Create all methods as defined in input parameter object.
     */
    public int createMethodsFile(InputParameter inputParameter) {
        logger.info("Starting create methods.");
        int count = 0;
        int exitCode = 0;
        String pathGet = "";
        String pathPost = "";
        String pathDelete = "";
        String pathPut = "";
        String pathPatch = "";
        if (inputParameter.getMethodList().contains(InputParameter.Method.GET)) {
            pathGet = this.fromGetTemplate(inputParameter);
            count++;
        }
        if (inputParameter.getMethodList().contains(InputParameter.Method.POST)) {
            System.out.println("POST not implemented (yet).");
            count++;
        }
        if (inputParameter.getMethodList().contains(InputParameter.Method.PUT)) {
            System.out.println("PUT not implemented (yet).");
            count++;
        }
        if (inputParameter.getMethodList().contains(InputParameter.Method.DELETE)) {
            System.out.println("DELETE not implemented (yet).");
            count++;
        }
        if (inputParameter.getMethodList().contains(InputParameter.Method.PATCH)) {
            System.out.println("PATCH not implemented (yet).");
            count++;
        }
        String result = pathGet;
        if (count > 0 && result != null) {
            boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, result, inputParameter.getOutputFile());
            if (ok) {
                logger.info("Found " + count + " methods. Create method finished.");
            } else {
                exitCode = 1;
                logger.severe("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());

            }
        } else {
            if (count == 0) {
                logger.warning("No methods found. Please define rest methods.");
            } else {
                logger.warning("No data could be processed. Check input data.");
            }
            exitCode = 1;
        }
        return exitCode;
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createSchema(InputParameter inputParameter) {
        logger.info("Starting create schema.");
        logger.info("Input parameter: \n" + inputParameter);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mSchema = mf.compile(schemaTemplate);
        StringWriter writerSchema = new StringWriter();
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
            this.createMustacheDataFromYaml(inputParameter);
        }
        HashMap sampleMap = inputParameter.getDataMap();
//        if (yamlWrapper.getName().equals("") == false) {
//            inputParameter.setResource(yamlWrapper.getName());
//        }
        sampleMap.put("objectName", inputParameter.getCapResource());
        try {
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        return writerSchema.toString();
    }

    private String fromGetTemplate(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile("get_" + Constants.DEFAULT_OUTPUT_FILE);
        }
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals(InputParameter.Sourcetype.YAMLFILE)) {
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
            if (inputParameter.getSampleYamlData() == null || inputParameter.getSampleYamlData().length() < 3) {
                logger.severe("No sample yaml with data. Please define input file or set sample yaml.");
                return null;
            }
        }
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL)) {
            if (inputParameter.getDataMap() == null || inputParameter.getDataMap().size() == 0) {
                logger.severe("No data. Please define input file or set sample yaml.");
                return null;
            }
        }
        logger.info(inputParameter.toString());

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(getTemplate);
        Mustache mSchema = mf.compile(schemaTemplate);
        Mustache mInfo = mf.compile(infoTemplate);

        StringWriter writerPaths = new StringWriter();
        StringWriter writerSchema = new StringWriter();
        StringWriter writerInfo = new StringWriter();
        /** Read input data sample. */
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
            this.createMustacheDataFromYaml(inputParameter);
        }
        HashMap sampleMap = inputParameter.getDataMap();

        sampleMap.put("objectName", inputParameter.getCapResource());
        try {
            mBasic.execute(writerPaths, inputParameter).flush();
            mInfo.execute(writerInfo, inputParameter).flush();
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerInfo + nexLine + writerPaths + nexLine + writerSchema;
    }


    private String fromCrudTemplate(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(Constants.DEFAULT_OUTPUT_FILE);
        }
        if ((inputParameter.getInputFile() == null || inputParameter.getInputFile().equals(""))
                && inputParameter.getSampleYamlData() == null && inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
            inputParameter.setInputFile("src/test/resources/Pet.yml");
        }
        /* Read in the sample data from a file. */
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals(InputParameter.Sourcetype.YAMLFILE)) {
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
            if (inputParameter.getSampleYamlData() == null || inputParameter.getSampleYamlData().length() < 3) {
                logger.severe("No sample yaml with data. Please define input file or set sample yaml.");
                System.exit(1);
            }
        }

        logger.info(inputParameter.toString());

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(fullCrudTemplate);

        StringWriter writer = new StringWriter();
        /** Read input data sample. */
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
            this.createMustacheDataFromYaml(inputParameter);
        }

        try {
            mBasic.execute(writer, inputParameter).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer + nexLine + this.createSchema(inputParameter);
    }

    private void createMustacheDataFromYaml(InputParameter inputParameter) {
        /** if STRING the data are already there. */
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
        }
        LinkedHashMap<String, Object> map = Util.readYamlFromString(inputParameter.getSampleYamlData());
        if (map == null || map.isEmpty()) {
            return;
        }
        this.createMustacheData(inputParameter, map);
    }

    /**
     * Providing HashMap with input data for the Mustache engine.
     */
    private void createMustacheData(InputParameter inputParameter, LinkedHashMap<String, Object> map) {
        /** Read input data sample. */
        HashMap resultMap = new HashMap<>();
        if (map == null || map.isEmpty()) {
            return;
        }
        String firstKey = map.keySet().iterator().next();
        Object ob = map.get(firstKey);
        String cl = ob.getClass().toString();
        if (cl.equals("class java.util.LinkedHashMap") == true) {
            map = (LinkedHashMap<String, Object>) ob;
            inputParameter.setResource(firstKey);
        }

        List<PropertyData> list = new ArrayList<>();
        for (String key : map.keySet()) {
            String value = map.get(key).toString();
            PropertyData sampleData = new PropertyData(key, value);
            if (!Util.isNumber(value))  sampleData.setMinlength(1);
            sampleData.setType(Util.isNumber(value) ? "number" : "string");
            list.add(sampleData);
        }
        resultMap.put("data", list);
        inputParameter.setDataMap(resultMap);
    }


    private void createMustacheDataFromExcel(InputParameter inputParameter, List<Map<String, String>> mapList) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        List<PropertyData> list = new ArrayList<>();
        for (Map<String, String> mapSheet : mapList) {
            String key = mapSheet.get("Name");
            if (key == null || key.equals("")) key = "undefined";
            String value = mapSheet.get("SampleValues");
            PropertyData sampleData = new PropertyData(key, value);
            if ( !Util.isNumber(value) ) sampleData.setMinlength(1);
            if (mapSheet.containsKey("OASDatatype")) sampleData.setType(mapSheet.get("OASDatatype"));
            else sampleData.setType(Util.isNumber(value) ? "number" : "string");
            sampleData.setDescription(mapSheet.get("Beschreibung"));
            if (mapSheet.containsKey("Format")){
                sampleData.setFormat(mapSheet.get("Format"));
            }
            if (mapSheet.containsKey("Max")){
                String max = mapSheet.get("Max");
                if ( Util.isNumber(max)){
                    sampleData.setMaxLength(Integer.parseInt(max));
                }else if (max != null && max.equals("") == false ){
                    logger.warning("Value for maxLength is not a number: " + max);
                }
            }
            list.add(sampleData);
        }
        resultMap.put("data", list);
        inputParameter.setDataMap(resultMap);
    }


    public List<InputParameter> createParameterList(HashMap<String, List<Map<String, String>>> map) {
        List<InputParameter> result = new ArrayList<>();
        for (String index : map.keySet()) {
            List<Map<String, String>> mapList = map.get(index);
            StringBuilder yaml = new StringBuilder(index + ": " + "´\n");
            InputParameter inputParameter = new InputParameter();
            inputParameter.setResource(index);
            inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
            this.createMustacheDataFromExcel(inputParameter, mapList);
            result.add(inputParameter);
        }
        return result;
    }
}
    
    

