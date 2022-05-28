package ch.inss.joaswizard;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.*;

/**
 * Author: Oliver Glas, <a href="https://inss.ch">...</a>.
 */
public class Joaswizard implements Constants {

    private final Logger logger;
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
     * Create all methods for the paths as defined in input parameter object list.
     *
     * @param list list of input parameter values.
     * @return String of OAS3 path section.
     */
    public String createMethodsFromList(List<InputParameter> list) {
        logger.info("Starting to create methods.");
        if (list == null || list.isEmpty()) return "{}";
        String result = new String();
//        if (list == null || list.isEmpty()) return "Error: no data.";
        for (InputParameter inputParameter : list) {
            result = result + fromPathsTemplate(inputParameter);
        }
        return result;
    }

    /**
     * Creates only error model and the start of components schemas.
     *
     * @return String of beginning of components schemas including a standard error model.
     */
    public String createComponentsSchemas() {
        return new Util().readFromClasspath(componentsErrorTemplate + ".hbs") + nexLine;
    }

    /**
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of components schemas object.
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
    }

    /**
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of OAS3 info section.
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

    /**
     * @param inputParameter
     * @param inputStream
     * @return null if an error occured.
     */
    public String createFromExcelInputstreamToString(InputParameter inputParameter, InputStream inputStream) {
        if (inputStream == null) {
            logger.severe("File was empty.");
            return null;
        }
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelStream(inputStream);
        if (integerListHashMap == null) return null;
        List<InputParameter> inputParameterList = this.createInputParameterList(integerListHashMap, inputParameter);
        return fullMultipleObjects(inputParameterList, inputParameter);
    }

    public boolean createFromExcelToFile(InputParameter inputParameter) {
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(Constants.DEFAULT_OUTPUT_FILE);
        }
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile(inputParameter.getInputFile());
        if (integerListHashMap == null) return false;
        List<InputParameter> inputParameterList = this.createInputParameterList(integerListHashMap, inputParameter);
        String result = fullMultipleObjects(inputParameterList, inputParameter);
        boolean ok = Util.writeStringToData(Constants.CURRENT_FOLDER, result, inputParameter.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + inputParameter.getOutputFile() + ".");
        } else {
            logger.severe("Could not write file " + Constants.CURRENT_FOLDER + inputParameter.getOutputFile());
        }
        return ok;
    }

    /**
     * Create full OAS3 document from multiple objects.
     *
     * @param inputParameterList
     * @param inputParameter
     * @return
     */
    private String fullMultipleObjects(List<InputParameter> inputParameterList, InputParameter inputParameter) {
        String paths = this.createMethodsFromList(inputParameterList);
        if (paths.startsWith(ERROR)) {
            logger.severe("Could not process data for OAS paths. " + paths);
        }
        StringBuilder resouces = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        for (InputParameter parameter : inputParameterList) {
            resouces.append(parameter.getResource()).append(", ");
            String result = this.createSchemaObjects(parameter);
            if (ERROR.equals(result)) return "false";
            objects.append(result).append(nexLine);
        }
        resouces.delete(resouces.length() - 2, resouces.length());
        inputParameter.setResource(resouces.toString());

        String info = this.createInfo(inputParameter);
        String components = this.createComponentsSchemas();

        return info + paths + nexLine + components + objects;
    }

    /**
     * Creates defined methods from a yaml string for a single object.
     * Returns if an error ocrrued or not.
     *
     * @param inputParameter input parameter values.
     * @return true if the file was created.
     */
    public boolean createFromSingleYamlToFile(InputParameter inputParameter) {
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE) {
            if (Util.fileExists(inputParameter.getInputFile()) == false) {
                logger.severe("Yaml file not found: " + inputParameter.getInputFile());
                return false;
            }
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
        }
        if (inputParameter.getSampleYamlData() == null) {
            logger.severe("No Yaml data provided.");
            return false;
        }
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(Constants.DEFAULT_OUTPUT_FILE);
        }
        String result = this.fullDocument(inputParameter);
        if (result == null) return false;
        boolean ok = Util.writeStringToData(Constants.CURRENT_FOLDER, result, inputParameter.getOutputFile());
        if (ok) {
            logger.info("OpenAPI content written to " + inputParameter.getOutputFile() + ".");
        } else {
            logger.severe("Could not write file " + Constants.CURRENT_FOLDER + inputParameter.getOutputFile());
        }
        return ok;
    }

    /**
     * Creates full document from inputParameter object.
     * Returns OAS3 document as String object.
     *
     * @param inputParameter input parameter values.
     * @return OAS3 as string.
     */
    public String fullDocument(InputParameter inputParameter) {
        StringBuilder document = new StringBuilder();
        String info = this.createInfo(inputParameter);
        String oasPaths = this.fromPathsTemplate(inputParameter);
        String schemaObjects = this.createSchemaObjects(inputParameter);
        if (ERROR.equals(schemaObjects)) return null;
        String componentsSection = this.createComponentsSchemas();
        document.append(info);
        document.append(oasPaths).append(nexLine);
        document.append(componentsSection);
        document.append(schemaObjects);
//        String result = info + oasPaths + components + document;
        return document.toString();
    }

    //#4
    private String fromPathsTemplate(InputParameter inputParameter) {
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
        boolean allquery = inputParameter.isAllquery();
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
     * Gives back an Inputparameter instance with pre-filled default values.
     *
     * @param input  input parameter values.
     * @param sourcetype Sourc type.
     * @param resource resource name.
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

    /**
     * Create all CRUD operations for one object and stores it in a file..
     *
     * @param inputParameter input parameter values.
     * @return true if file was created.
     */
    public boolean createCrudFileFromYaml(InputParameter inputParameter) {
        logger.info("Jo starts to create crud file from Yaml input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        return this.createFromSingleYamlToFile(inputParameter);
    }

    //#2
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
            logger.severe("Could not read Yaml file: " + inputParameter.getInputFile() + ". Check if it has Yaml format.");
        }

        /** Read input data sample. */
        HashMap resultMap = new HashMap<>();

        String firstKey = map.keySet().iterator().next();
        Object ob = map.get(firstKey);
        String cl = ob.getClass().toString();
        if (cl.equals("class java.util.LinkedHashMap")) {
            map = (LinkedHashMap<String, Object>) ob;
            inputParameter.setResource(firstKey); //TODO
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

        /* If not in OasType field defined, it will try to take the type from the column DbType. */
        CaseInsensitiveMap<String, HashMap<String, String>> jsonMappingMap = Util.getJsonAsMap(inputParameter.getMappingFile() == null ? DEFAULT_MAPPING : inputParameter.getMappingFile());
        if (jsonMappingMap == null) {
            jsonMappingMap = new CaseInsensitiveMap<>();
        }
        String prefix = "Sheet " + inputParameter.getResource() + ": ";
        int idx = 0;
        int countEmpty = 0;
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
            logger.fine("Line " + idx + ", key: " + key);
            if (key.equals(UNDEFINED) && (sampleValue == null || sampleValue.equals("") && (type == null || type.equals("")))) {
                logger.fine(prefix + "Empty row or not enough data to build an OAS3 schema object (at line: " + idx + ").");
                countEmpty++;
                if (countEmpty > 2) {
                    logger.info("Found 3 or more empty lines beginning row " + (idx - 1) + ", assuming no more data and stop reading lines here..");
                    break;
                }
                continue;
            }

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
                if (sampleValue != null) {
                    type = (Util.isNumber(sampleValue) ? "integer" : "string");
                } else {
                    type = "string";
                }
            } else if (type != null) {
                type = type.trim().toLowerCase();
                if (Arrays.asList(DATATYPELIST).contains(type) == false) {
                    logger.warning(prefix + "Type not valid: " + type + ". Jo changes type to string.");
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
                    logger.warning(prefix + "Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
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
                logger.warning(prefix + "Value for maxLength is not a number: " + max);
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
            InputParameter inputParameter = new InputParameter(in.getInputFile(), in.getOutputFile(), in.getSourceType(), in.getMethodList(), in.getMappingFile(), in.isPrefixMatch());
            inputParameter.setResource(index);
            this.createMustacheDataFromExcel(inputParameter, mapList);
            result.add(inputParameter);
        }
        return result;
    }
}
    
    

