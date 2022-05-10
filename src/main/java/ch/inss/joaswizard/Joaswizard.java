package ch.inss.joaswizard;

//import com.github.mustachejava.DefaultMustacheFactory;
//import com.github.mustachejava.Mustache;
//import com.github.mustachejava.MustacheFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.File;
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
            if (stream == null) {
                File file = new File(".");
                System.out.println("Missing logging.properties file.");
            } else {
                LogManager.getLogManager().readConfiguration(stream);
            }
            logger = Logger.getLogger(Joaswizard.class.getName());
            fileHandler = new FileHandler("joaswizard.log");
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        logger.addHandler(new ConsoleHandler());
        logger.addHandler(fileHandler);
    }

    /**
     * Create all CRUD operations for one object and stores it in a file..
     */
    public void createCrudFile(InputParameter inputParameter) {
        logger.info("Starting create crud file.");
        String resultSchema = this.createCrud(inputParameter);
        boolean ok = Util.writeStringToData(Constants.DATA_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            logger.severe("Could not write file " + Constants.DATA_FOLDER + inputParameter.getOutputFile());
        }
    }

    /**
     * Create all methods for the paths as defined in input parameter object list.
     */
    public String createMethodsFromList(List<InputParameter> list) {
        logger.info("Starting to create methods.");
        String result = new String();
        if (list == null || list.isEmpty()) return "Error: no data.";
        for (InputParameter inputParameter : list) {
            result = result + createMethods(inputParameter);
        }
        return result;
    }

    /**
     * Creates paths for the defined methods.
     */
    public String createMethods(InputParameter inputParameter) {
        String paths = null;

        StringBuilder builder = new StringBuilder();
        if (inputParameter.getMethodList().contains(InputParameter.Method.GET)) {
            paths = this.fromGetTemplate(inputParameter);
        }

        builder.append(paths).append(nexLine);
        if (paths != null) {
            logger.info("Processed methods.");
        } else {
            logger.severe("No methods found. Please define rest api methods.");
        }
        return builder.toString();
    }

    public String createComponentsSchemas() {
//        return Util.readFromFile("src/main/resources/componentsError.yaml") + nexLine;
        return new Util().readFromClasspath("componentsError.yaml.hbs") + nexLine;
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createSchemaObjects(InputParameter inputParameter) {
        logger.info("Starting create schema.");
        logger.info("Input parameter: \n" + inputParameter);
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(schemaTemplate);
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

            result = template.apply(sampleMap);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        return result;
//        return writerSchema.toString();
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createInfo(InputParameter inputParameter) {
        logger.info("Start creating info.");
        logger.info("Input parameter: \n" + inputParameter);
//        MustacheFactory mf = new DefaultMustacheFactory();
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(infoTemplate);
//        Mustache mSchema = mf.compile(infoTemplate);
//        StringWriter writerSchema = new StringWriter();

            if (inputParameter.getResource() == null || inputParameter.getResource().length() == 0) {
                logger.severe("No resource defined.");
                return "Error";
            }
            result = template.apply(inputParameter);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
        return result;
//        return writerSchema.toString();
    }

    public void createFromExcel(InputParameter input) {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcel(input.getInputFile());
        input.setSourceType(InputParameter.Sourcetype.EXCEL);
        List<InputParameter> inputParameterList = this.createInputParameterList(integerListHashMap, input);

        String paths = this.createMethodsFromList(inputParameterList);
        if (paths.startsWith("Error")) {
            logger.severe("Could not process data for OAS paths. " + paths);
        }
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

    /**
     * Creates defined methods from a yaml string for a single object.
     */
    public void createMethodsFromSingleYamlObject(InputParameter input) {
        String oasPaths = this.createMethods(input);
        StringBuilder objects = new StringBuilder();
        objects.append(this.createSchemaObjects(input)).append(nexLine);
        String info = this.createInfo(input);
        String components = this.createComponentsSchemas();
        String result = info + oasPaths + components + objects;
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
        if (inputParameter.getSourceType() != null && inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL)) {
            if (this.data == null || this.data.size() == 0) {
                logger.severe("No data. Please define input file or set sample yaml.");
                return "Error";
            }
        }
        logger.info(inputParameter.toString());
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(pathComponentCrudTemplate);

            /* Read input data sample. */
            if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
                this.createMustacheDataFromYaml(inputParameter);
            }
            result = template.apply(inputParameter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nexLine + result;
    }

    /**
     * Create an OAS3 document string from input parameter which define sample properties for one object.
     */
    public String createCrud(InputParameter inputParameter) {
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
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(fullCrudTemplate);
//        MustacheFactory mf = new DefaultMustacheFactory();
//        Mustache mBasic = mf.compile(fullCrudTemplate);

//        StringWriter writer = new StringWriter();
            /** Read input data sample. */
            if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
                this.createMustacheDataFromYaml(inputParameter);
            }

//        try {
            result = template.apply(inputParameter);
//            mBasic.execute(writer, inputParameter).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result + nexLine + this.createSchemaObjects(inputParameter);
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
        for (Map<String, String> sheetMap : mapList) {
            CaseInsensitiveMap<String, String> sheetCIMap = new CaseInsensitiveMap(sheetMap);
            idx++;
            List<Header> unused = new ArrayList<>();
//            for ( Header col : Header.values() ){
//                if(sheetCIMap.containsKey(col.toString()) == false ){
//                    unused.add(col);
//                }
//            }
//            List<String> unknownFieldlist = new ArrayList<>();
//            for ( String sheetHeader : sheetCIMap.keySet()){
//                boolean found = false;
//                for ( Header h : Header.values() ){
//                    if (h.name().equalsIgnoreCase(sheetHeader))
//                        found = true;
//                }
//                if (found == false){
//                    unknownFieldlist.add(sheetHeader);
//                }
//            }
//            logger.info("Unused possible headerr: " + unused.toString());
//            logger.info("Unknown header in sheet: " + unknownFieldlist.toString());
            String key = sheetCIMap.get(Header.NAME);
            if (key == null || key.equals("")) key = "undefined";
            else key = key.trim();
            String sampleValue = sheetCIMap.get(Header.SAMPLEVALUE);
            String type = null;
            if (sheetCIMap.containsKey(Header.DATATYPE)) type = sheetCIMap.get(Header.DATATYPE);
            if (key.equals("undefined") && (sampleValue == null || sampleValue.equals("") && (type == null || type.equals("")))) {
                logger.warning("Not enough data to build an OAS3 schema object (at index: " + idx + ").");
                continue;
            }

            if (type == null || type.equals("")) {
                type = (Util.isNumber(sampleValue) ? "number" : "string");
            } else {
                type = type.trim().toLowerCase();
                if (Arrays.asList(DATATYPELIST).contains(type) == false) {
                    logger.warning("Type not valid: " + type + ". Type is changed to string.");
                    type = "string";
                }
            }
            if (sampleValue == null) {
                if (type.equals("string")) sampleValue = "string";
                if (type.equals("integer")) sampleValue = "1";
                if (type.equals("number")) sampleValue = "1.0";
            } else {
                sampleValue = sampleValue.trim();
            }

            PropertyData propertyData = new PropertyData(key, type);
            String format = sheetCIMap.get(Header.FORMAT);
            if (format != null) {
                if ((type.equalsIgnoreCase("number") || type.equalsIgnoreCase("integer")) && format.equalsIgnoreCase("string")) {
                    logger.warning("Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
                    ;
                }
                propertyData.setFormat(format.trim());
            } else {
                // If datatype had to be change and format is not defined, define format as former datatype.
                if (sheetCIMap.containsKey(Header.DATATYPE) && sheetCIMap.get(Header.DATATYPE).equalsIgnoreCase(type) == false) {
                    propertyData.setFormat(sheetCIMap.get(Header.DATATYPE).toLowerCase());
                }
            }

            propertyData.setExamplevalue(sampleValue);
            if (sheetCIMap.containsKey(Header.MIN)) {
                if (Util.isNumber(sheetCIMap.get(Header.MIN))) {
                    propertyData.setMinlength(Integer.parseInt(sheetCIMap.get(Header.MIN)));
                }
            } else if (!Util.isNumber(sampleValue)) {
                propertyData.setMinlength(1);
            }
            propertyData.setDescription(sheetCIMap.get("Description"));

            propertyData.setPattern(sheetCIMap.get("Pattern"));
            if (sheetCIMap.containsKey("Required")) {
                propertyData.setRequired(Boolean.parseBoolean(sheetCIMap.get("Required")));
            } else if (sheetCIMap.containsKey("Nullable")) {
                propertyData.setRequired(!Boolean.parseBoolean(sheetCIMap.get("Nullable")));
            } else {
                propertyData.setRequired(true);
            }

            propertyData.setEnumvalues(this.getOasEnum(sheetCIMap.get(Header.ENUMVALUES), propertyData.getType()));

            String max = sheetCIMap.get("Max");
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
                if (type.equalsIgnoreCase("string")) {
                    if (Util.isNumber(item)) {
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
    
    

