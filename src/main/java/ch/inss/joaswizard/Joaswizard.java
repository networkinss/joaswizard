package ch.inss.joaswizard;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.*;

/**
 * Author: Oliver Glas, https://inss.ch.
 */
public class Joaswizard implements Constants {

    private Logger logger = null;
    private Data data = new Data();

    public Joaswizard() {
        logger = Logger.getLogger(Joaswizard.class.getName());
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(Main.consoleHandler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
    }

    /**
     * Create all CRUD operations for one object and stores it in a file..
     */
    public boolean createCrudFile(InputParameter inputParameter) {
        logger.info("Starting create crud file.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        String resultSchema = this.createCrud(inputParameter);
        
        if (ERROR.equals(resultSchema)) return false;
        boolean ok = Util.writeStringToData(Constants.CURRENT_FOLDER, resultSchema, inputParameter.getOutputFile());
        if (ok == false) {
            logger.severe("Could not write file " + Constants.CURRENT_FOLDER + inputParameter.getOutputFile());
        }
        return ok;
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
//        boolean ok1 = inputParameter.isPathIdQuery();  //TODO
//        boolean ok2 = inputParameter.isAllquery();
        paths = this.fromGetTemplate(inputParameter);
        builder.append(paths).append(nexLine);
        if (paths != null) {
            logger.info("Processed methods.");
        } else {
            logger.severe("No methods found. Please define rest api methods.");
        }
        return builder.toString();
    }

    /**
     * Creates only error model and the start of components schemas.
     */
    public String createComponentsSchemas() {
        return new Util().readFromClasspath(componentsErrorTemplate + ".hbs") + nexLine;
    }

    /**
     * Creates one components schema as defined in input paramter.
     */
    public String createSchemaObjects(InputParameter inputParameter) {
        logger.info("Starting create schema.");
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(schemaTemplate);
            if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING) {
                this.createMustacheDataFromYaml(inputParameter);
            }
            if (this.data.size() == 0) {
                logger.severe("No data. Please define input file or set sample yaml.");
                return ERROR;
            }
            HashMap sampleMap = data.getDataMap(inputParameter.getResource());
            if (sampleMap == null || sampleMap.size() == 0) {
                logger.severe("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");
                return ERROR;
            }
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
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(infoTemplate);
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
    }

    public boolean createFromExcelInputstream(InputParameter inputParameter, InputStream inputStream) {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelStream(inputStream);
        return createExcel(integerListHashMap, inputParameter);
    }

    public boolean createFromExcel(InputParameter input) {
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile(input.getInputFile());
        return createExcel(integerListHashMap, input);
    }

    private boolean createExcel(HashMap<String, List<Map<String, String>>> integerListHashMap, InputParameter input) {
        if (integerListHashMap == null) return false;
        input.setSourceType(InputParameter.Sourcetype.EXCEL);
        List<InputParameter> inputParameterList = this.createInputParameterList(integerListHashMap, input);
        String paths = this.createMethodsFromList(inputParameterList);
        if (paths.startsWith(ERROR)) {
            logger.severe("Could not process data for OAS paths. " + paths);
        }
        StringBuilder resouces = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        for (InputParameter parameter : inputParameterList) {
            resouces.append(parameter.getResource()).append(", ");
            String result = this.createSchemaObjects(parameter);
            if (ERROR.equals(result)) return false;
            objects.append(result).append(nexLine);
        }
        resouces.delete(resouces.length() - 2, resouces.length());
        input.setResource(resouces.toString());

        String info = this.createInfo(input);
        String components = this.createComponentsSchemas();

        String result = info + paths + components + objects;
        boolean ok = Util.writeStringToData(Constants.CURRENT_FOLDER, result, input.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + input.getOutputFile() + ".");
        } else {
            logger.severe("Could not write file " + Constants.CURRENT_FOLDER + input.getOutputFile());
        }
        return ok;
    }

    /**
     * Creates defined methods from a yaml string for a single object.
     * Returns if an error ocrrued or not.
     */
    public boolean createMethodsFromSingleYamlObject(InputParameter input) {
        String result = fullDocument(input);
        if (result == null) return false;
        boolean ok = Util.writeStringToData(Constants.CURRENT_FOLDER, result, input.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + input.getOutputFile() + ".");
        } else {
            logger.severe("Could not write file " + Constants.CURRENT_FOLDER + input.getOutputFile());
        }
        return ok;
    }

    private String fullDocument(InputParameter input) {
        String oasPaths = this.createMethods(input);
        StringBuilder objects = new StringBuilder();
        String schema = this.createSchemaObjects(input);
        if (ERROR.equals(schema)) return null;
        objects.append(schema).append(nexLine);
        String info = this.createInfo(input);
        String components = this.createComponentsSchemas();
        String result = info + oasPaths + components + objects;
        return result;
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
        logger.fine(inputParameter.toString());
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
    
    /** Gives back an Inputparameter instance with pre-filled default values. */
    public InputParameter inputMandatory(String input, String sourcetype, String resource){
        InputParameter in = new InputParameter();
        in.setCrud();
        in.setOutputFile(DEFAULT_OUTPUT_FILE);
        in.setResource(resource);
        in.setResourceId("ID");
        if(sourcetype.equals(InputParameter.Sourcetype.YAMLFILE.toString()) || sourcetype.equals(InputParameter.Sourcetype.EXCEL.toString())){
            in.setInputFile(input);
        }else if (sourcetype.equals(InputParameter.Sourcetype.YAMLSTRING.toString())  ){
            in.setSampleYamlData(input);
        }else if (sourcetype.equals(InputParameter.Sourcetype.YAMLSTRINGBASE64)){
            in.setSampleYamlBase64(input);
        }
        return in;
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
        
        logger.fine(inputParameter.toString());
        Handlebars mf = new Handlebars();
        String result = null;
//        return this.fullDocument(inputParameter);  //TODO use other templates.
        
        try {
            Template template = mf.compile(fullCrudTemplate);
            /** Read input data sample. */
            if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
                this.createMustacheDataFromYaml(inputParameter);
            }
            result = template.apply(inputParameter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result + nexLine + this.createSchemaObjects(inputParameter);
    }

    private void createMustacheDataFromYaml(InputParameter inputParameter) {
        /** if STRING the data are already there. */
        LinkedHashMap<String, Object> map = null;
        try {
            if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
                inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
            }
            map = Util.readYamlFromString(inputParameter.getSampleYamlData());
            if (map == null || map.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            logger.severe("Could not read Yaml file: " + inputParameter.getInputFile() + ", check if it in Yaml format.");
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
     * OAS type, format and pattern is defined in this sequence:
     * 1. Read values from Excel in columns with name: Datatype, Format, Pattern.
     * 2. If not defined there, it will check mapping.
     * 3. If no mapping is defined, it will guess. If it can be parsed to a number, it will be type number. All other is a string.
     */
    private void createMustacheDataFromExcel(InputParameter inputParameter, List<Map<String, String>> mapList) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        List<PropertyData> list = new ArrayList<>();
        int idx = 0;
        /* If not in OasType field defined, it will try to take the type from the column DbType. */
        CaseInsensitiveMap<String, HashMap<String, String>> jsonMappingMap = Util.getJsonAsMap(DEFAULT_MAPPING);
        if (jsonMappingMap == null) {
            jsonMappingMap = new CaseInsensitiveMap<>();
        }
        /** Define each property of one object. */
        for (Map<String, String> sheetMap : mapList) {
            CaseInsensitiveMap<String, String> sheetCIMap = new CaseInsensitiveMap(sheetMap);
            idx++;
            String key = sheetCIMap.get(Header.NAME);
            if (key == null || key.equals("")) key = UNDEFINED;
            else key = key.trim();
            String sampleValue = sheetCIMap.get(Header.OASEXAMPLE);
            String type = null;
            if (sheetCIMap.containsKey(Header.OASTYPE)) type = sheetCIMap.get(Header.OASTYPE);
            if (key.equals(UNDEFINED) && (sampleValue == null || sampleValue.equals("") && (type == null || type.equals("")))) {
                logger.warning("Not enough data to build an OAS3 schema object (at line: " + idx + ").");
                continue;
            }

            HashMap<String, String> mappingMap = new HashMap<>();
            if (type == null || type.equals("")) {
                String dbtype = sheetCIMap.get(Header.DBTYPE);
                if (dbtype != null && "".equals(dbtype) == false) {
                    /* Get the mapping hashMap from the mapping map.*/
                    mappingMap = jsonMappingMap.get(dbtype);
                    if (mappingMap == null) mappingMap = new HashMap<>();
                    type = mappingMap.get(Header.OASTYPE.toString());
                }
            }
            if ((type == null || type.equals("")) ) {
                if(sampleValue != null){
                    type = (Util.isNumber(sampleValue) ? "number" : "string");
                }else{
                    type = "string";
                }
            } else if(type != null){
                type = type.trim().toLowerCase();
                if (Arrays.asList(DATATYPELIST).contains(type) == false) {
                    logger.warning("Type not valid: " + type + ". Change type to string.");
                    type = "string";
                }
            }
            /* Object containing all the properties for an object. */
            PropertyData propertyData = new PropertyData(key, type);

            /* Define example values even if not defined. */
            if ((sampleValue == null || "".equals(sampleValue))) {
                if(inputParameter.isDoDefaultSamples()) {
                    if (type.equals("string")) sampleValue = "string";
                    else if (type.equals("integer")) sampleValue = "1";
                    else if (type.equals("number")) sampleValue = "1.0";
                }
            } else {
                sampleValue = sampleValue.trim();
            }
            if (sampleValue!=null) {
                if (sampleValue.equals("")) propertyData.setExamplevalue(null);
                else propertyData.setExamplevalue(sampleValue);
            }
            /* Define formatting. */
            String format = sheetCIMap.get(Header.OASFORMAT);
            if (format == null || "".equals(format)) {
                /* Try to get mapped format from mapping.json if the DbType had been defined. */
                format = mappingMap.get(Header.OASFORMAT.toString());
                // If datatype had to be change and format is not defined, define format as former datatype.
//                if (format == null && sheetCIMap.containsKey(Header.OASTYPE) && sheetCIMap.get(Header.OASTYPE).equalsIgnoreCase(type) == false) {
//                    propertyData.setFormat(sheetCIMap.get(Header.OASTYPE));
//                }
            }
            if (format != null) {
                if (((type.equalsIgnoreCase("number") || type.equalsIgnoreCase("integer")) && format.equalsIgnoreCase("string"))
                        || (type.equalsIgnoreCase("number") && (format.equalsIgnoreCase("int32") || format.equalsIgnoreCase("int64")))
                        || (type.equalsIgnoreCase("integer") && (format.equalsIgnoreCase("flaot") || format.equalsIgnoreCase("double")))) {
                    logger.warning("Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
                    propertyData.setFormat(null);
                } else propertyData.setFormat(format.trim());
            }

            if (sheetCIMap.containsKey(Header.OASMIN)) {
                if (Util.isNumber(sheetCIMap.get(Header.OASMIN))) {
                    propertyData.setMinlength(Integer.parseInt(sheetCIMap.get(Header.OASMIN)));
                }
            } else if (!Util.isNumber(sampleValue)) {
                propertyData.setMinlength(1);
            }
            String oasDescription = sheetCIMap.get(Header.OASDESCRIPTION);
            oasDescription = "Keine Description wit ' oder \" .";
            if (oasDescription != null && oasDescription.isEmpty() == false && oasDescription.isBlank() == false) {
                //Do some cleanup otherwise it would invalidate the OAS3.
                oasDescription = oasDescription.replace('\'', ' ');
                oasDescription = oasDescription.replace('"', ' ');

                propertyData.setDescriptionApostrophe(oasDescription);
            }


            propertyData.setPattern(sheetCIMap.get(Header.OASPATTERN.toString()));
            if (sheetCIMap.containsKey(Header.OASREQUIRED)) {
                propertyData.setRequired(Boolean.parseBoolean(sheetCIMap.get(Header.OASREQUIRED)));
            } else if (sheetCIMap.containsKey(Header.DBNULLABLE)) {
                propertyData.setRequired(!Boolean.parseBoolean(sheetCIMap.get(Header.DBNULLABLE)));
            } else {
                propertyData.setRequired(true);
            }

            propertyData.setEnumvalues(this.getOasEnum(sheetCIMap.get(Header.OASENUM), propertyData.getType()));

            String max = sheetCIMap.get(Header.OASMAX);
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
    
    

