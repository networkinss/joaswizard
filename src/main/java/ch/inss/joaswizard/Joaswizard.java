package ch.inss.joaswizard;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Author: Oliver Glas, https://inss.ch.
 */
public class Joaswizard implements Constants {

    private static Logger logger = null;
    private Data data = new Data();

    public Joaswizard() {
        FileHandler fileHandler = null;
        try {
            InputStream stream = Joaswizard.class.getClassLoader().getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
            logger = Logger.getLogger(Joaswizard.class.getName());

            fileHandler = new FileHandler("joaswizard.log");
            
            //adding custom handler
            
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        logger.addHandler(new ConsoleHandler());
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
    public String createMethodsFile(List<InputParameter> list) {
        logger.info("Starting create methods.");
        int count = 0;
        StringBuilder result = new StringBuilder();
        if (list == null || list.isEmpty()) return "Error";
        for (InputParameter inputParameter : list) {
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
            result.append(pathGet).append(nexLine);
        }
        if (count > 0) {

            logger.info("Processed " + count + " methods for " + list.size() + " objects.");
        } else {
            logger.warning("No methods found. Please define rest api methods.");
        }
        return result.toString();
    }

    private String createComponentsSchemas() {
        return Util.readFromFile("src/main/resources/componentsError.yaml") + nexLine;
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createSchemaObjects(InputParameter inputParameter) {
        logger.info("Starting create schema.");
        logger.info("Input parameter: \n" + inputParameter);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mSchema = mf.compile(schemaTemplate);
        StringWriter writerSchema = new StringWriter();
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
            this.createMustacheDataFromYaml(inputParameter);
        }
        if (this.data.size() == 0) {
            logger.severe("No data. Please define input file or set sample yaml.");
            return "Error";
        }
        HashMap sampleMap = data.getDataMap(inputParameter.getResource());
        if (sampleMap == null || sampleMap.size() == 0) {
            logger.severe("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");
            return "Error";
        }

//        if (yamlWrapper.getName().equals("") == false) {
//            inputParameter.setResource(yamlWrapper.getName());
//        }
        sampleMap.put(OBJECTNAME, inputParameter.getCapResource());
        try {
            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        return writerSchema.toString();
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createInfo(InputParameter inputParameter) {
        logger.info("Start creating info.");
        logger.info("Input parameter: \n" + inputParameter);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mSchema = mf.compile(infoTemplate);
        StringWriter writerSchema = new StringWriter();

        if (inputParameter.getResource() == null || inputParameter.getResource().length() == 0) {
            logger.severe("No resource defined.");
            return "Error";
        }
        try {
            mSchema.execute(writerSchema, inputParameter).flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        return writerSchema.toString();
    }

    public void createFromExcel(InputParameter input) {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcel(input.getInputFile());
        input.setSourceType(InputParameter.Sourcetype.EXCEL);
        List<InputParameter> inputParameterList = this.createInputParameterList(integerListHashMap, input);

        String paths = this.createMethodsFile(inputParameterList);
        StringBuilder resouces = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        for (InputParameter parameter : inputParameterList) {
            resouces.append(parameter.getResource()).append(", ");
            objects.append(this.createSchemaObjects(parameter)).append(nexLine);
        }
        resouces.delete(resouces.length() - 2, resouces.length());
        input.setResource(resouces.toString());

        String info = this.createInfo(input);
        String components = this.createComponentsSchemas();

        String result = info + paths + components + objects;
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, result, input.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + input.getOutputFile() + ".");
        } else {
            logger.severe("Could not write file " + Constants.DATA_FOLDER + input.getOutputFile());
        }
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
//        HashMap sampleMap = null;
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL)) {
            if (this.data == null || this.data.size() == 0) {
                logger.severe("No data. Please define input file or set sample yaml.");
                return "Error";
            }
//            sampleMap = data.getDataMap(inputParameter.getResource());
//            if (sampleMap == null || sampleMap.size() == 0) {
//                logger.severe("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");
//                return "Error";
//            }

        }
        logger.info(inputParameter.toString());

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mBasic = mf.compile(getTemplate);
//        Mustache mSchema = mf.compile(schemaTemplate);
//        Mustache mInfo = mf.compile(infoTemplate);

        StringWriter writerPaths = new StringWriter();
//        StringWriter writerSchema = new StringWriter();
//        StringWriter writerInfo = new StringWriter();
        /** Read input data sample. */
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
            this.createMustacheDataFromYaml(inputParameter);
        }
//        if (sampleMap == null || sampleMap.isEmpty()) {
//            logger.severe("No data to process.");
//            return "Error";
//        }

//        sampleMap.put("objectName", inputParameter.getCapResource());
        try {
            mBasic.execute(writerPaths, inputParameter).flush();
//            mInfo.execute(writerInfo, inputParameter).flush();
//            mSchema.execute(writerSchema, sampleMap).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nexLine + writerPaths;
    }


    public String fromCrudTemplate(InputParameter inputParameter) {
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
        return writer + nexLine + this.createSchemaObjects(inputParameter);
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
     * Providing HashMap this.data with input data for the Mustache engine.
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
            PropertyData sampleData = new PropertyData(key, Util.isNumber(value) ? "number" : "string");
            if (!Util.isNumber(value)) sampleData.setMinlength(1);
            sampleData.setExamplevalue(value);
            sampleData.setRequired(true);
            list.add(sampleData);
        }
        resultMap.put("data", list);
        this.data.addDataMap(inputParameter.getResource(), resultMap);
//        inputParameter.setDataMap(resultMap);
    }

    /**
     * Providing HashMap this.data with input data for the Mustache engine.
     */
    private void createMustacheDataFromExcel(InputParameter inputParameter, List<Map<String, String>> mapList) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        List<PropertyData> list = new ArrayList<>();
        int idx = 0;
        /** Define each property of one object. */
        for (Map<String, String> mapSheet : mapList) {
            CaseInsensitiveMap<String, String> propertyMap = new CaseInsensitiveMap(mapSheet);
            idx++;
            String key = propertyMap.get(Header.NAME);
            if (key == null || key.equals("")) key = "undefined";
            else key = key.trim();
            String value = propertyMap.get(Header.SAMPLEVALUE);
            String type = null;
            if (propertyMap.containsKey(Header.DATATYPE)) type = propertyMap.get(Header.DATATYPE);
            if (key.equals("undefined") && (value == null || value.equals("") && (type == null || type.equals("")))) {
                logger.warning("Not enough data to build an OAS3 schema object (index: " + idx + ").");
                continue;
            }
            if (value == null) value = "";
            else value = value.trim();
            PropertyData propertyData = new PropertyData(key, type);
            String format = propertyMap.get("Format");
            if (type == null || type.equals("")) {
                type = (Util.isNumber(value) ? "number" : "string");
                if ( format != null ){
                    logger.warning("Format not set because type is not defined.");
                }
            }else{
                if((type.equalsIgnoreCase("number") || type.equalsIgnoreCase("integer")) && format.equalsIgnoreCase("string")){
                    logger.warning("Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);;
                }
                propertyData.setFormat(format.trim());
            }
            propertyData.setExamplevalue(value);
            if (propertyMap.containsKey(Header.MIN)) {
                if (Util.isNumber(propertyMap.get(Header.MIN))) {
                    propertyData.setMinlength(Integer.parseInt(propertyMap.get(Header.MIN)));
                }
            } else if (!Util.isNumber(value)) {
                propertyData.setMinlength(1);
            }
            propertyData.setDescription(propertyMap.get("Description"));
            
            propertyData.setPattern(propertyMap.get("Pattern"));
            if ( propertyMap.containsKey("Required")){
                propertyData.setRequired(Boolean.parseBoolean(propertyMap.get("Required")));
            }else if ( propertyMap.containsKey("Nullable")){
                propertyData.setRequired(!Boolean.parseBoolean(propertyMap.get("Nullable")));
            }else{
                propertyData.setRequired(true);
            }
            
            propertyData.setEnumvalues(this.getOasEnum(propertyMap.get(Header.ENUMVALUES), propertyData.getType()));
            
            String max = propertyMap.get("Max");
            if (Util.isNumber(max)) {
                propertyData.setMaxLength(Integer.parseInt(max));
            } else if (max != null && max.equals("") == false) {
                logger.warning("Value for maxLength is not a number: " + max);
            }
            list.add(propertyData);
        }
        resultMap.put("data", list);
        this.data.addDataMap(inputParameter.getResource(), resultMap);
    }

    private String getOasEnum(String e, String type) {
        StringBuilder b = null;
        if (e != null && e.equals("") == false) {
            b = new StringBuilder();
            String[] s = e.split(",");

            boolean notfirst = false;
            for (String item : s) {
                if (notfirst) b.append("          ");
                notfirst = true;
                /* Numbers need an enclosing apostrophe. */
                if ( type.equalsIgnoreCase("string")){
                    if (Util.isNumber(item)){
                        item = "'" + item + "'";
                    }
                }
                b.append("- ").append(item).append(nexLine);
            }
            b.deleteCharAt(b.length() - 1);
//            sampleData.setEnumvalues(b.toString());
        }
        if (b == null) return null;
        return b.toString();
    }


    private List<InputParameter> createInputParameterList(HashMap<String, List<Map<String, String>>> map, InputParameter in) {
        List<InputParameter> result = new ArrayList<>();
        for (String index : map.keySet()) {
            List<Map<String, String>> mapList = map.get(index);
            StringBuilder yaml = new StringBuilder(index + ": " + "´\n");
            InputParameter inputParameter = new InputParameter(in.getInputFile(), in.getOutputFile(), in.getSourceType(), in.getMethodList());
            inputParameter.setResource(index);
//            inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
            this.createMustacheDataFromExcel(inputParameter, mapList);
            result.add(inputParameter);
        }
        return result;
    }
}
    
    

