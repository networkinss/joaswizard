package ch.inss.openapi.joaswizard;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.*;

/**
 * Author: Oliver Glas.
 */
public class Joaswizard implements Constants {

    private final Logger logger;

    private String errorMessage;
    private String warningMessage;
    private String okMessage;

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getOkMessage() {
        return okMessage;
    }

    public void setOkMessage(String okMessage) {
        this.okMessage = okMessage;
    }

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
     * OAS3 part: paths
     * Create all methods for the paths as defined in input parameter object list.
     *
     * @param list list of input parameter values.
     * @return String of OAS3 path section.
     */
    public String createMethodsFromList(List<InputParameter> list) {
        logger.info("Start creating methods.");
        if (list == null || list.isEmpty()) {
            logger.info("Skipping methods, list is empty.");
            return "{}";
        }
        String result = new String();
        for (InputParameter inputParameter : list) {
            if (inputParameter.isDoPaths() == false) {
                logger.info("Skipping creating methods (DoPaths = false) for " + inputParameter.getResourceId());
                continue;
            }
            result = result + fromPathsTemplate(inputParameter);
        }
        logger.info("End creating methods.");
        return result;
    }

    /**
     * OAS3 part: start tag and error model for components schemas.
     * Creates only error model and the start of components schemas.
     *
     * @param doSchemas false skip this method.
     * @return String of beginning of components schemas including a standard error model.
     */
    public String createComponentsSchemas(boolean doSchemas) {
        if (doSchemas) {
            logger.info("Adding beginning of components schemas section and default error model.");
            return new Util().readFromClasspath(componentsErrorTemplate + ".hbs") + nextLine;
        }
        logger.info("Skipping beginning of components schemas section (doSchemaas = false).");
        return "";
    }

    /**
     * OAS3 part: objects in components schemas.
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of components schemas object.
     */
    public String createSchemaObjects(InputParameter inputParameter) {
        if (inputParameter.isDoSchemas() == false) {
            logger.info("Skipping create schema (doSchemas = false).");
        }
        logger.info("Start creating schema objects.");
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(schemaTemplate);
            if (inputParameter.getSchemaData() == null || inputParameter.getSchemaData().isEmpty()) {
                this.logErrorMessage("No data. Please define input file or set sample yaml.");
                return ERROR;
            }
            HashMap sampleMap = inputParameter.getSchemaData();
//            HashMap sampleMap = this.data.getDataMap(inputParameter.getResource());
            if (sampleMap == null || sampleMap.size() == 0) {
                this.logErrorMessage("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");  //TODO check
                return ERROR;
            }
            sampleMap.put(OBJECTNAME, inputParameter.getCapResource());
            result = template.apply(sampleMap);
        } catch (IOException e) {
            e.printStackTrace();
            this.logErrorMessage(e.getLocalizedMessage());
        }
        logger.info("End creating schema objects.");
        return result;
    }

    /**
     * OAS3 part: info
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of OAS3 info section.
     */
    public String createInfo(InputParameter inputParameter) {
        if (inputParameter.isDoInfo() == false) {
            logger.info("Skipping info section (doInfo = false).");
            return "";
        }
        logger.info("Start creating info.");
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(infoTemplate);
            if (inputParameter.getResource() == null || inputParameter.getResource().length() == 0) {
                this.logErrorMessage("No resource defined.");
                return "Error";
            }
            OasInfo oasInfo = inputParameter.getOasInfo();
            if (oasInfo.getTitle() == null) {
                oasInfo.setTitle(inputParameter.getCapResource() + " API");
            }
            result = template.apply(oasInfo);
        } catch (IOException e) {
            e.printStackTrace();
            this.logErrorMessage(e.getLocalizedMessage());
        }
        logger.info("End creating info.");
        return result;
    }

    /**
     * @param inputParameter parameter.
     * @param inputStream    input Excel file as InputStream.
     * @return null if an error occured.
     */
    public String createFromExcelInputstreamToString(InputParameter inputParameter, InputStream inputStream) {
        if (inputStream == null) {
            this.logErrorMessage("File was empty.");
            return null;
        }
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelStream(inputStream);
        if (integerListHashMap == null) return null;
        List<InputParameter> inputParameterList = this.createExcelInputParameterList(integerListHashMap, inputParameter);

        return fullMultipleObjects(inputParameterList);
    }

    public boolean createFromExcelToFile(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(DEFAULT_OUTPUT_FILE);
        }
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile(inputParameter.getInputFile());
        if (integerListHashMap == null) return false;
        List<InputParameter> inputParameterList = this.createExcelInputParameterList(integerListHashMap, inputParameter);

        String result = fullMultipleObjects(inputParameterList);
        boolean ok = Util.writeStringToData(CURRENT_FOLDER, result, inputParameter.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + inputParameter.getOutputFile() + ".");
        } else {
            this.logErrorMessage("Could not write file " + CURRENT_FOLDER + inputParameter.getOutputFile());
        }
        return ok;
    }

    /**
     * OAS3 full document.
     * Create full OAS3 document from multiple objects.
     *
     * @param inputParameterList
     * @return Full OAS3 document or null if an error occurred.
     */
    private String fullMultipleObjects(List<InputParameter> inputParameterList) {
        if (inputParameterList == null || inputParameterList.isEmpty()) return null;
        String paths = this.createMethodsFromList(inputParameterList);
        if (paths.startsWith(ERROR)) {
            this.logErrorMessage("Could not process data for OAS paths. " + paths);
        }
        StringBuilder resources = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        int max = 0;
        for (InputParameter input : inputParameterList) {
            resources.append(input.getResource()).append(", ");
            String result = this.createSchemaObjects(input);
            if (ERROR.equals(result)) return null;
            objects.append(result).append(nextLine);
            if (max > input.getMaxobjects()) {
                this.logWarningMessage("Maximum number of object (" + input.getMaxobjects() + ") is breached, next objects are skipped. There were some objects skipped because of the limitation of number objects to " + input.getMaxobjects());
                break;
            }
            max++;
        }
        resources.delete(resources.length() - 2, resources.length());
        inputParameterList.get(0).setResource(resources.toString());
        String info = this.createInfo(inputParameterList.get(0));
        String components = this.createComponentsSchemas(inputParameterList.get(0).isDoSchemas());
        return info + paths + nextLine + components + objects;
    }

    /**
     * Create all CRUD operations for one object and stores it in a file..
     *
     * @param inputParameter input parameter values.
     * @return true if file was created.
     */
    public boolean createCrudFromYamlToFile(InputParameter inputParameter) {
        logger.info("Jo starts to create crud file from Yaml input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        return this.createFromYamlToFile(inputParameter);
    }

    /**
     * Creates defined methods from a Yaml string with objects.
     * Returns if an error ocrrued or not.
     *
     * @param inputParameter input parameter values.
     * @return true if the file was created.
     */
    public boolean createFromYamlToFile(InputParameter inputParameter) {
        String result = this.createFromYamlToString(inputParameter);
        if (result == null) return false;
        boolean ok = Util.writeStringToData(CURRENT_FOLDER, result, inputParameter.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + inputParameter.getOutputFile() + ".");
        } else {
            this.logErrorMessage("Could not write file " + CURRENT_FOLDER + inputParameter.getOutputFile());
        }
        return ok;
    }

    /**
     * For convenience takes Yaml format as input and gives back full OAS3 document a string
     * with all CRUD operations as string.
     *
     * @param inputParameter parameter.
     * @return String with full OAS3 document with CRUD operations. Can be null in case an error occured. Like e.g. input was not in Yaml format.
     */
    public String createCrudFromYamlToString(InputParameter inputParameter) {
        logger.info("Jo starts to create crud file from Yaml input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        String result = this.createFromYamlToString(inputParameter);
        if (result == null) {
            if (this.errorMessage == null) this.errorMessage = ERROR;
            return this.errorMessage;
        }
        return result;
    }

    /**
     * Returns a full OAS3 document.
     *
     * @param inputParameter parameter.
     * @return Full OAS3 document as string.
     */
    public String createFromYamlToString(InputParameter inputParameter) {
        if (validateInput(inputParameter)) return null;
        List<InputParameter> inputParameterList = this.createMustacheDataFromYaml(inputParameter);
        return this.fullMultipleObjects(inputParameterList);
    }

    private boolean validateInput(InputParameter inputParameter) {
        if (inputParameter == null) return true;
//        List<InputParameter> inputParameterList = new ArrayList<>();
        //TODO check move to InputParameter class.
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
            if (Util.fileExists(inputParameter.getInputFile()) == false) {
                this.logErrorMessage("Yaml file not found: " + inputParameter.getInputFile());
                return true;
            }
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
        }
        if (inputParameter.getSampleYamlData() == null) {
            this.logErrorMessage("No Yaml data provided.");
            return true;
        }
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(DEFAULT_OUTPUT_FILE);
        }
        if (inputParameter.checkValid() == false) {
            this.logErrorMessage("Input parameter are not consistent.");
            return false;
        }
        return false;
    }

    /**
     * @param inputParameter
     * @return
     */
    private String fromPathsTemplate(InputParameter inputParameter) {

        logger.fine(inputParameter.toString());
        Handlebars mf = new Handlebars();
        String result = null;
        try {
            Template template = mf.compile(pathComponentCrudTemplate);
            result = template.apply(inputParameter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextLine + result;
    }

    /**
     * Gives back an Inputparameter instance with pre-filled default values.
     *
     * @param input      input parameter values.
     * @param sourcetype Sourc type.
     * @param resource   resource name.
     * @return Default input parameter values.
     */
    public InputParameter inputMandatory(String input, String sourcetype, String resource) {
        InputParameter in = new InputParameter();
        in.setCrud();
        in.setOutputFile(DEFAULT_OUTPUT_FILE);
        in.setResource(resource);
        in.setResourceId("ID");
        if (sourcetype.equals(InputParameter.Sourcetype.YAMLFILE.toString()) || sourcetype.equals(InputParameter.Sourcetype.EXCEL.toString())) {
            in.setInputFile(input);
        } else if (sourcetype.equals(InputParameter.Sourcetype.YAMLSTRING.toString())) {
            in.setSampleYamlData(input);
        } else if (sourcetype.equals(InputParameter.Sourcetype.YAMLSTRINGBASE64)) {
            in.setSampleYamlBase64(input);
        }
        return in;
    }


    public List<InputParameter> createMustacheDataFromYaml(InputParameter mainInputParameter) {
        /** if STRING the data are already there. */
        LinkedHashMap<String, Object> allObjectsMap = null;

        List<InputParameter> inputParameterList = new ArrayList<>();
        try {
            allObjectsMap = Util.readYamlFromString(mainInputParameter.getSampleYamlData());
            if (allObjectsMap == null || allObjectsMap.isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            this.logErrorMessage("Could not read Yaml file: " + mainInputParameter.getInputFile() + ". Check if it has Yaml format.");
            this.setErrorMessage("Input is not Yaml format.");
            return null;
        }

        /** Loop for objects. */
        int max = 0;
        for (String key : allObjectsMap.keySet()) {
            InputParameter inputParameter = new InputParameter(mainInputParameter);
            max++;
            if (max > inputParameter.getMaxobjects()) {
                this.warningMessage = this.warningMessage + " There were some objects skipped because of the limitation of number objects to " + inputParameter.getMaxobjects();
                break;
            }
            HashMap<String, Object> resultMap = new HashMap<>();
            boolean breakLoop = false;
            LinkedHashMap<String, Object> propMap = null;
//        String firstKey = map.keySet().iterator().next();
            /** Read input data sample. */
            Object ob = allObjectsMap.get(key);
            String cl = ob.getClass().toString();
            if (cl.equals("class java.util.LinkedHashMap")) {
                propMap = (LinkedHashMap<String, Object>) ob;
                inputParameter.setResource(key); //TODO
            } else {
                propMap = allObjectsMap;
                breakLoop = true;
            }


            /** Loop for the properties of one object. */
            List<PropertyData> list = new ArrayList<>();
            for (String propKey : propMap.keySet()) {
                String value = propMap.get(propKey).toString().trim();  //TODO check null ?
                boolean isNumeric = true;
                boolean isArray = false;
                boolean isNumber = false;
                int minLength = 1;
                String type = null;
                if (value.startsWith("[") && value.endsWith("]")) {
                    String arrValue = value.substring(1, value.length() - 1);
                    String[] arr = arrValue.split(",");
                    isArray = true;
                    for (String a : arr) {
                        isNumeric = isNumeric && Util.isNumber(a);
                        if (isNumeric) {
                            isNumber = isNumber || a.contains(".");
                        }
                        if (a.length() > minLength) minLength = a.trim().length();
                    }
                } else {
                    isNumeric = isNumeric && Util.isNumber(value);
                    if (isNumeric) {
                        isNumber = isNumber || value.contains(".");
                    }
                }
                if (isArray) {
                    type = "array";
                } else if (isNumeric) {
                    if (isNumber) type = "number";
                    else type = "integer";
                } else {
                    type = "string";
                    minLength = value.trim().length();
                }

                PropertyData propertyData = new PropertyData(propKey, type);
                if (isArray) {
                    propertyData.setArray(true);
                    if (isNumber) propertyData.setTypeArray("number");
                    else if (isNumeric) propertyData.setTypeArray("integer");
                    else {
                        propertyData.setTypeArray("string");
                        propertyData.setMinlength(minLength);
                    }
                }
                propertyData.setExamplevalue(value);
                if (type.equals("string")) propertyData.setMinlength(minLength);
                propertyData.setRequired(true);

                list.add(propertyData);
            }
            resultMap.put("data", list);
            inputParameter.setSchemaData(resultMap);
            inputParameterList.add(inputParameter);
            if (breakLoop) break;
        }

        return inputParameterList;
    }

    /**
     * Providing HashMap this.data with input data for the Mustache engine.
     * OAS type, format and pattern is defined in this sequence:
     * 1. Read values from Excel in columns with name: Datatype, Format, Pattern.
     * 2. If not defined there, it will check mapping.
     * 3. If no mapping is defined, it will guess. If it can be parsed to a number, it will be type number. All other is a string.
     *
     * @return
     */
    private HashMap<String, Object> createMustacheDataFromExcel(InputParameter inputParameter, List<Map<String, String>> mapList) {
        HashMap<String, Object> resultMap = new HashMap<>();
        List<PropertyData> list = new ArrayList<>();

        /* If not in OasType field defined, it will try to take the type from the column DbType. */
        CaseInsensitiveMap<String, HashMap<String, String>> jsonMappingMap = Util.getJsonAsMap(inputParameter.getMappingFile() == null ? DEFAULT_MAPPING : inputParameter.getMappingFile());
        if (jsonMappingMap == null) {
            jsonMappingMap = new CaseInsensitiveMap<>();
        }
        String logPrefix = "Sheet " + inputParameter.getResource() + ": ";
        int idx = 0;
        int countEmpty = 0;
        checkHeaderValidity(mapList.get(0), inputParameter.getResource());
        /** Define each property of one object. */
        for (Map<String, String> sheetMap : mapList) {
            CaseInsensitiveMap<String, String> sheetCIMap = new CaseInsensitiveMap(sheetMap);
            idx++;

            String key = sheetCIMap.get(Header.NAME);
            String sampleValue = sheetCIMap.get(Header.OASEXAMPLE);
            String type = null;
            if (sheetCIMap.containsKey(Header.OASTYPE)) type = sheetCIMap.get(Header.OASTYPE);
            if ((key == null || key.equals("")) && (sampleValue == null || sampleValue.equals("") && (type == null || type.equals("")))) {
                logger.fine(logPrefix + "Empty row or not enough data to build an OAS3 schema object (at line: " + idx + ").");
                countEmpty++;
                if (countEmpty > 2) {
                    logger.info(logPrefix + "Found 3 or more empty lines beginning row " + (idx - 1) + ", assuming no more data and stop reading lines here..");
                    break;
                }
                continue;
            }
            if (key == null || key.equals("")) {
                key = UNDEFINED;
                this.logWarningMessage(logPrefix + "Excel header '" + Header.NAME + "' is missing'. Name of object will be 'undefined'.");
            } else key = key.trim();
            logger.fine(logPrefix + "Line " + idx + ", key: " + key);

            HashMap<String, String> mappingMap = new HashMap<>();
            /** Define type (OasType). */
            if (type == null || type.equals("")) {
                /** DB type assignment if Oastype is not defined but DBType is present. */
                String dbtype = sheetCIMap.get(Header.DBTYPE);
                if (dbtype != null && "".equals(dbtype) == false) {
                    /* Get the mapping hashMap from the mapping map.*/
                    mappingMap = jsonMappingMap.get(dbtype);
                    if (mappingMap == null) {
                        if (inputParameter.isPrefixMatch()) {
                            for (String mapType : jsonMappingMap.keySet()) {
                                if (dbtype.toLowerCase().startsWith(mapType.toLowerCase()) && mapType.length() >= 3) {
                                    mappingMap = jsonMappingMap.get(mapType);
                                }
                            }
                        } else {
                            mappingMap = new HashMap<>();
                        }
                    }
                    type = mappingMap.get(Header.OASTYPE.toString());
                }
            }
            if ((type == null || type.equals(""))) {
                if (inputParameter.isStopOnError()) {
                    this.logErrorMessage("Type mapping error in line " + idx + ", key: " + key);
                    this.logErrorMessage("Type cannot be mapped or is not valid. Breaking because stopOnError is set to true.");
                    return null;
                }
                if (sampleValue != null) {
                    type = (Util.isNumber(sampleValue) ? "integer" : "string");
                } else {
                    type = "string";
                }
            } else if (type != null) {
                type = type.trim().toLowerCase();
                if (Arrays.asList(DATATYPELIST).contains(type) == false) {
                    this.logWarningMessage(logPrefix + "Type not valid: " + type + ". Jo changes type to string.");
                    type = "string";
                }
            }
            /* Object containing all the properties for an object. */
            PropertyData propertyData = new PropertyData(key, type);

            /** Define example values (OasExample) and guess if not defined. */
            if ((sampleValue == null || "".equals(sampleValue))) {
                if (inputParameter.isDoDefaultSamples()) {
                    if (type.equals("string")) sampleValue = "string";
                    else if (type.equals("integer")) sampleValue = "1";
                    else if (type.equals("number")) sampleValue = "1.0";
                }
            } else {
                sampleValue = sampleValue.trim();
            }
            if (sampleValue != null) {
                if (sampleValue.equals("")) propertyData.setExamplevalue(null);
                else propertyData.setExamplevalue(sampleValue);
            }
            /** Define formatting (OasFormat). */
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
                    this.logWarningMessage(logPrefix + "Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
                    propertyData.setFormat(null);
                } else propertyData.setFormat(format.trim());
            }

            if (sheetCIMap.containsKey(Header.OASMIN)) {
                if (Util.isNumber(sheetCIMap.get(Header.OASMIN))) {
                    propertyData.setMinlength(Integer.parseInt(sheetCIMap.get(Header.OASMIN)));
                }
            }
//            else if (!Util.isNumber(sampleValue)) {
//                propertyData.setMinlength(1);
//            }
            String oasDescription = sheetCIMap.get(Header.OASDESCRIPTION);
            if (oasDescription != null && oasDescription.isEmpty() == false && oasDescription.isBlank() == false) {
                //Do some cleanup otherwise it would invalidate the OAS3.
                oasDescription = oasDescription.replace('\'', ' ').replace('"', ' ');
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
                this.logWarningMessage(logPrefix + "Value for maxLength is not a number: " + max);
            }
            list.add(propertyData);
        }
        resultMap.put("data", list);
        inputParameter.setSchemaData(resultMap);
        return resultMap;
    }

    private void checkHeaderValidity(Map<String, String> map, String sheetName) {
        if (map == null) return;
        for (String key : map.keySet()) {
            if (key == "") continue;
            try {
                Header header = Header.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException iae) {
                this.logWarningMessage("Unknown Excel header: '" + key + "' in sheet " + sheetName + ".");
            }
        }
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
                b.append("- ").append(item).append(nextLine);
            }
            b.deleteCharAt(b.length() - 1);
//            sampleData.setEnumvalues(b.toString());
        }
        if (b == null) return null;
        return b.toString();
    }

    //TODO set resource ?
    private List<InputParameter> createExcelInputParameterList(HashMap<String, List<Map<String, String>>> map, InputParameter in) {
        List<InputParameter> result = new ArrayList<>();
        for (String index : map.keySet()) {
            List<Map<String, String>> mapList = map.get(index);
            StringBuilder yaml = new StringBuilder(index + ": " + "´\n");
            InputParameter inputParameter = new InputParameter(in);
            inputParameter.setResource(index);
            inputParameter.setSchemaData(this.createMustacheDataFromExcel(inputParameter, mapList));
            result.add(inputParameter);
        }
        return result;
    }

    private void logErrorMessage(String message) {
        logger.severe(message);
        this.errorMessage = message;
    }

    private void logWarningMessage(String message) {
        logger.warning(message);
        this.warningMessage = message;
    }
}
    
    

