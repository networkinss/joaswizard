package ch.inss.openapi.joaswizard;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Oliver Glas.
 */
public class Joaswizard implements Constants {

    private static final Logger logger = Logger.getLogger(Joaswizard.class.getName());

    /** Legacy state, only filled by the deprecated methods. {@link #generate(InputParameter)} does not touch it. */
    private volatile String errorMessage;
    private volatile String warningMessage;
    private volatile String okMessage;

    /**
     * Collects the diagnostics of one call. Never shared between calls, so a Joaswizard instance has no per-call state.
     */
    private static final class Collector {
        private final List<Diagnostic> list = new ArrayList<>();

        void error(String message) {
            logger.severe(message);
            list.add(new Diagnostic(Diagnostic.Severity.ERROR, message));
        }

        void warning(String message) {
            logger.warning(message);
            list.add(new Diagnostic(Diagnostic.Severity.WARNING, message));
        }

        boolean hasErrors() {
            return list.stream().anyMatch(d -> d.getSeverity() == Diagnostic.Severity.ERROR);
        }

        String last(Diagnostic.Severity severity) {
            String result = null;
            for (Diagnostic d : list) {
                if (d.getSeverity() == severity) result = d.getMessage();
            }
            return result;
        }
    }

    /**
     * Generates a full OpenAPI 3 document as String.
     * The source is chosen by {@link InputParameter#getSourceType()}: Excel (file or input stream, a file wins if both are set)
     * or Yaml/JSON (file or string, including Base64 encoded Yaml set by
     * {@link InputParameter#setSampleYamlBase64(String)}).
     * The method never throws for invalid input, problems are reported as diagnostics of the result.
     * It keeps no state, so one instance can be shared between threads.
     * Like the former methods it may complete the input parameter with default values (output file, methods).
     *
     * @param inputParameter input parameter values.
     * @return result with the specification (null on failure) and all errors and warnings.
     */
    public GenerationResult generate(InputParameter inputParameter) {
        Collector c = new Collector();
        String spec = this.run(inputParameter, c);
        if (spec == null && !c.hasErrors()) {
            c.error("No OpenAPI specification could be generated, no usable input data found.");
        }
        return GenerationResult.of(spec, c.list);
    }

    /**
     * Generates the OpenAPI 3 document like {@link #generate(InputParameter)} and writes it to
     * {@link InputParameter#getOutputFile()} (default openapi.yaml). Missing parent folders are created.
     * A relative output file is resolved against the current folder.
     * If writing fails, the result contains an ERROR diagnostic; the spec is still available in the result.
     *
     * @param inputParameter input parameter values.
     * @return result, {@link GenerationResult#getOutputFile()} is set if the file was written.
     */
    public GenerationResult generateToFile(InputParameter inputParameter) {
        GenerationResult generated = this.generate(inputParameter);
        if (generated.getSpec() == null) return generated;
        Collector c = new Collector();
        String written = this.write(generated.getSpec(), inputParameter, c, false);
        List<Diagnostic> all = new ArrayList<>(generated.getDiagnostics());
        all.addAll(c.list);
        return GenerationResult.of(generated.getSpec(), all, written);
    }

    /** Dispatches on the source type. */
    private String run(InputParameter in, Collector c) {
        if (in == null) {
            c.error("Input parameter is missing.");
            return null;
        }
        boolean excel = in.getSourceType() == InputParameter.Sourcetype.EXCEL
                || (in.getSourceType() == null && in.getInputFile() == null && in.getInputStream() != null);
        if (!excel) return this.yamlToString(in, c);
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        if (in.getInputFile() == null && in.getInputStream() != null) return this.excelStreamToString(in, c);
        return this.excelFileToString(in, c);
    }

    /**
     * Writes the spec. A legacy call resolves every output file below the current folder (also absolute ones), as before.
     *
     * @return the written file or null if writing failed (error added).
     */
    private String write(String spec, InputParameter in, Collector c, boolean legacy) {
        String file = in.getOutputFile();
        if (file == null || file.isEmpty()) {
            file = DEFAULT_OUTPUT_FILE;
            in.setOutputFile(file);
        }
        boolean ok;
        if (!legacy && new java.io.File(file).isAbsolute()) {
            ok = Util.writeToPath(java.nio.file.Paths.get(file), spec);
        } else {
            ok = Util.writeStringToData(CURRENT_FOLDER, spec, file);
        }
        if (ok) {
            logger.fine("OpenAPI content written to " + in.getOutputFile() + ".");
            return in.getOutputFile();
        }
        c.error("Could not write file " + CURRENT_FOLDER + in.getOutputFile());
        return null;
    }

    /** Resets the legacy fields before a deprecated call and fills them from its diagnostics afterwards. */
    private <T> T legacy(java.util.function.Function<Collector, T> call) {
        this.errorMessage = null;
        this.warningMessage = null;
        Collector c = new Collector();
        try {
            return call.apply(c);
        } finally {
            this.errorMessage = c.last(Diagnostic.Severity.ERROR);
            this.warningMessage = c.last(Diagnostic.Severity.WARNING);
        }
    }

    /**
     * @return the last error message of the latest call of a deprecated method, null if that call had no error.
     * The value is reset at the start of each deprecated call, so it no longer leaks from a previous call.
     * @deprecated not thread-safe and keeps only the last message, use {@link #generate(InputParameter)}.
     */
    @Deprecated
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /** @deprecated use {@link #generate(InputParameter)}. */
    @Deprecated
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the last warning message of the latest call of a deprecated method, null if that call had no warning.
     * The value is reset at the start of each deprecated call, so it no longer leaks from a previous call.
     * @deprecated not thread-safe and keeps only the last message, use {@link #generate(InputParameter)}.
     */
    @Deprecated
    public String getWarningMessage() {
        return warningMessage;
    }

    /** @deprecated use {@link #generate(InputParameter)}. */
    @Deprecated
    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    /**
     * @return the ok message, which is never set by Joaswizard itself.
     * @deprecated use {@link GenerationResult#isOk()}.
     */
    @Deprecated
    public String getOkMessage() {
        return okMessage;
    }

    /** @deprecated use {@link GenerationResult#isOk()}. */
    @Deprecated
    public void setOkMessage(String okMessage) {
        this.okMessage = okMessage;
    }

    /**
     * OAS3 part: paths
     * Create all methods for the paths as defined in input parameter object list.
     *
     * @param list list of input parameter values.
     * @return String of OAS3 path section.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createMethodsFromList(List<InputParameter> list) {
        return this.legacy(c -> this.methodsFromList(list, c));
    }

    private String methodsFromList(List<InputParameter> list, Collector c) {
        logger.fine("Start creating methods.");
        if (list == null || list.isEmpty()) {
            logger.fine("Skipping methods, list is empty.");
            return "{}";
        }
        StringBuilder result = new StringBuilder();
        for (InputParameter inputParameter : list) {
            if (inputParameter.isDoPaths() == false) {
                logger.fine("Skipping creating methods (DoPaths = false) for " + inputParameter.getResourceId());
                continue;
            }
            result.append(fromPathsTemplate(inputParameter, c));
        }
        if (result.toString().startsWith("\n\n")) {
            result.deleteCharAt(0);
        }
        logger.fine("End creating methods.");
        return result.toString();
    }

    /**
     * OAS3 part: start tag and error model for components schemas.
     * Creates only error model and the start of components schemas.
     *
     * @param doSchemas false skip this method.
     * @return String of beginning of components schemas including a standard error model.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createComponentsSchemas(boolean doSchemas) {
        if (doSchemas) {
            logger.fine("Adding beginning of components schemas section and default error model.");
            return new Util().readFromClasspath(componentsErrorTemplate + ".hbs") + nextLine;
        }
        logger.fine("Skipping beginning of components schemas section (doSchemaas = false).");
        return "";
    }

    /**
     * OAS3 part: objects in components schemas.
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of components schemas object.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createSchemaObjects(InputParameter inputParameter) {
        return this.legacy(c -> this.schemaObjects(inputParameter, c));
    }

    private String schemaObjects(InputParameter inputParameter, Collector c) {
        if (inputParameter.isDoSchemas() == false) {
            logger.fine("Skipping create schema (doSchemas = false).");
            return "";
        }
        logger.fine("Start creating schema objects.");
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = YamlScalar.newHandlebars();
        String result = null;
        try {
            Template template = mf.compile(schemaTemplate);
            if (inputParameter.getSchemaData() == null || inputParameter.getSchemaData().isEmpty()) {
                c.error("No data. Please define input file or set sample yaml.");
                return ERROR;
            }
            HashMap sampleMap = inputParameter.getSchemaData();
//            HashMap sampleMap = this.data.getDataMap(inputParameter.getResource());
            if (sampleMap == null || sampleMap.size() == 0) {
                c.error("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");  //TODO check
                return ERROR;
            }
            sampleMap.put(OBJECTNAME, inputParameter.getCapResource());
            result = template.apply(sampleMap);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Creating OpenAPI section failed.", e);
            c.error(e.getLocalizedMessage());
        }
        logger.fine("End creating schema objects.");
        return result;
    }

    /**
     * OAS3 part: info
     * Creates one components schema as defined in input paramter.
     *
     * @param inputParameter input parameter values.
     * @return String of OAS3 info section.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createInfo(InputParameter inputParameter) {
        return this.legacy(c -> this.info(inputParameter, c));
    }

    private String info(InputParameter inputParameter, Collector c) {
        if (inputParameter.isDoInfo() == false) {
            logger.fine("Skipping info section (doInfo = false).");
            return "";
        }
        logger.fine("Start creating info.");
        logger.fine("Input parameter: \n" + inputParameter);
        Handlebars mf = YamlScalar.newHandlebars();
        String result = null;
        try {
            Template template = mf.compile(infoTemplate);
            if (inputParameter.getResource() == null || inputParameter.getResource().length() == 0) {
                c.error("No resource defined.");
                return "Error";
            }
            OasInfo oasInfo = inputParameter.getOasInfo();
            if (oasInfo.getTitle() == null) {
                oasInfo.setTitle(inputParameter.getCapResource() + " API");
            }
            result = template.apply(oasInfo);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Creating OpenAPI section failed.", e);
            c.error(e.getLocalizedMessage());
        }
        logger.fine("End creating info.");
        return result;
    }

    /**
     * @param inputParameter parameter.
     * @return null if an error occured.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createFromExcelInputstreamToString(InputParameter inputParameter) {
        return this.legacy(c -> this.excelStreamToString(inputParameter, c));
    }

    private String excelStreamToString(InputParameter inputParameter, Collector c) {
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        if (this.validateInput(inputParameter, c) == false) {
            return null;
        }
        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelStream(inputParameter.getInputStream());
        if (integerListHashMap == null) {
            c.error("Could not read Excel input stream.");
            return null;
        }
        List<InputParameter> inputParameterList = this.createExcelInputParameterList(integerListHashMap, inputParameter, c);
        return fullMultipleObjects(inputParameterList, c);
    }

    /**
     * Get input data from Excel and return OAS3 as String.
     *
     * @param inputParameter
     * @return OAS3.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createFromExcelfileToString(InputParameter inputParameter) {
        return this.legacy(c -> this.excelFileToString(inputParameter, c));
    }

    private String excelFileToString(InputParameter inputParameter, Collector c) {
        inputParameter.setSourceType(InputParameter.Sourcetype.EXCEL);
        if (this.validateInput(inputParameter, c) == false) return null;

        ExcelWrapper excelWrapper = new ExcelWrapper();
        HashMap<String, List<Map<String, String>>> integerListHashMap = excelWrapper.readExcelfile(inputParameter.getInputFile());
        if (integerListHashMap == null) {
            c.error("Could not read Excel file " + inputParameter.getInputFile() + ".");
            return null;
        }
        List<InputParameter> inputParameterList = this.createExcelInputParameterList(integerListHashMap, inputParameter, c);
        return fullMultipleObjects(inputParameterList, c);
    }

    /**
     * Get input data from Excel and store OAS3 in a file.
     *
     * @param inputParameter
     * @return true if file was successfully written to filesystem.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public boolean createFromExcelToFile(InputParameter inputParameter) {
        return this.legacy(c -> {
            String result = this.excelFileToString(inputParameter, c);
            if (result == null) return false;
            return this.write(result, inputParameter, c, true) != null;
        });
    }

    /**
     * OAS3 full document.
     * Create full OAS3 document from multiple objects.
     *
     * @param inputParameterList
     * @return Full OAS3 document or null if an error occurred.
     */
    private String fullMultipleObjects(List<InputParameter> inputParameterList, Collector c) {
        if (inputParameterList == null || inputParameterList.isEmpty()) return null;
        String paths = this.methodsFromList(inputParameterList, c);
        if (paths.startsWith(ERROR)) {
            c.error("Could not process data for OAS paths. " + paths);
        }
        StringBuilder resources = new StringBuilder();
        StringBuilder objects = new StringBuilder();
        int max = 0;
        for (InputParameter input : inputParameterList) {
            if (max >= input.getMaxobjects()) {
                c.warning(maxObjectsMessage(input.getMaxobjects()));
                break;
            }
            resources.append(input.getResource()).append(", ");
            String result = this.schemaObjects(input, c);
            if (ERROR.equals(result)) return null;
            objects.append(result).append(nextLine);
            max++;
        }
        if (resources.length() >= 2) resources.delete(resources.length() - 2, resources.length());
        InputParameter infoParameter = new InputParameter(inputParameterList.get(0));
        infoParameter.setResource(resources.toString());
        String info = this.info(infoParameter, c);
        String components = this.createComponentsSchemas(inputParameterList.get(0).isDoSchemas());
        return info + paths + nextLine + components + objects;
    }

    /**
     * Create all CRUD operations for one object and stores it in a file..
     *
     * @param inputParameter input parameter values.
     * @return true if file was created.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public boolean createCrudFromYamlToFile(InputParameter inputParameter) {
        logger.fine("Jo starts to create crud file from Yaml input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        return this.createFromYamlToFile(inputParameter);
    }

    /**
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public boolean createCrudFromJSONToFile(InputParameter inputParameter) {
        logger.fine("Jo starts to create crud file from JSON input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        return this.createFromJSONToFile(inputParameter);
    }


    /**
     * Creates defined methods from a Yaml string with objects.
     * Returns if an error ocrrued or not.
     *
     * @param inputParameter input parameter values.
     * @return true if the file was created.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public boolean createFromYamlToFile(InputParameter inputParameter) {
        return this.legacy(c -> {
            String result = this.yamlToString(inputParameter, c);
            if (result == null) return false;
            return this.write(result, inputParameter, c, true) != null;
        });
    }

    /**
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public boolean createFromJSONToFile(InputParameter inputParameter) {
        return this.createFromYamlToFile(inputParameter);
    }

    /**
     * For convenience takes Yaml format as input and gives back full OAS3 document a string
     * with all CRUD operations as string.
     *
     * @param inputParameter parameter.
     * @return String with full OAS3 document with CRUD operations. Can be null in case an error occured. Like e.g. input was not in Yaml format.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createCrudFromYamlToString(InputParameter inputParameter) {
        logger.fine("Jo starts to create crud file from Yaml input.");
        inputParameter.addMethod(InputParameter.Method.CRUD);
        return this.legacy(c -> {
            String result = this.yamlToString(inputParameter, c);
            if (result == null) {
                String error = c.last(Diagnostic.Severity.ERROR);
                return error == null ? ERROR : error;
            }
            return result;
        });
    }

    /**
     * Returns a full OAS3 document.
     *
     * @param inputParameter parameter.
     * @return Full OAS3 document as string.
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createFromYamlToString(InputParameter inputParameter) {
        return this.legacy(c -> this.yamlToString(inputParameter, c));
    }

    /**
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createFromJSONToString(InputParameter inputParameter) {
        return this.legacy(c -> this.yamlToString(inputParameter, c));
    }

    /** Yaml and JSON (valid Yaml) take the same way. */
    private String yamlToString(InputParameter inputParameter, Collector c) {
        if (this.validateInput(inputParameter, c) == false) return null;
        List<InputParameter> inputParameterList = this.mustacheDataFromYaml(inputParameter, c);
        return this.fullMultipleObjects(inputParameterList, c);
    }

    /**
     * Validates InputParameter class and sets default values
     * if needed for outputfile (openapi.yaml) and method (CRUD).
     *
     * @param inputParameter
     * @return true if valid.
     */
    private boolean validateInput(InputParameter inputParameter, Collector c) {
        if (inputParameter == null) return false;
        /* DEFAULT values */
        if (inputParameter.getOutputFile() == null || inputParameter.getOutputFile().equals("")) {
            inputParameter.setOutputFile(DEFAULT_OUTPUT_FILE);
            logger.fine("No output file defined, setting to default " + DEFAULT_OUTPUT_FILE + ".");
        }
        if (inputParameter.getMethodList().isEmpty()) {
            inputParameter.addMethod(InputParameter.Method.CRUD);
            c.warning("No REST API methods defined, setting to default CRUD.");
        }
        boolean valid = false;
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.EXCEL) {
            valid = (inputParameter.getInputFile() != null || inputParameter.getInputStream() != null) && inputParameter.getMethodList().isEmpty() == false;
            if (valid == false) {
                c.error("Input parameter are not sufficient. Please define input file or input stream.");
                return false;
            }
        } else if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.JSONFILE) {
            valid = inputParameter.getInputFile() != null && inputParameter.getResource() != null && inputParameter.getResourceId() != null;
            if (valid == false) {
                c.error("Input parameter for a " + formatName(inputParameter) + " file are not sufficient. Please define input file, resourceId and resource.");
                return false;
            }
        } else if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLSTRING || inputParameter.getSourceType() == InputParameter.Sourcetype.JSONSTRING) {
            valid = inputParameter.getSampleYamlData() != null && inputParameter.getResource() != null && inputParameter.getResourceId() != null;
            if (valid == false) {
                c.error("Input parameter for a " + formatName(inputParameter) + " String are not sufficient. Please define sample data, resourceId and resource.");
                return false;
            }
        }
        if (inputParameter.getResource() == null && inputParameter.getSourceType() != InputParameter.Sourcetype.EXCEL) {
            c.error("Resource must be set. Define resouce (object name).");
            return false;
        }
        if (inputParameter.getSourceType() == InputParameter.Sourcetype.YAMLFILE || inputParameter.getSourceType() == InputParameter.Sourcetype.JSONFILE) {
            if (Util.fileExists(inputParameter.getInputFile()) == false) {
                c.error(formatName(inputParameter) + " file not found: " + inputParameter.getInputFile());
                return false;
            }
            inputParameter.setSampleYamlData(Util.readFromFile(inputParameter.getInputFile()));
            if (inputParameter.getSampleYamlData() == null) {
                c.error("No " + formatName(inputParameter) + " data provided.");
                return false;
            }
        }
        return true;
    }

    /**
     * @param inputParameter
     * @return
     */
    private String fromPathsTemplate(InputParameter inputParameter, Collector c) {
        logger.fine(inputParameter.toString());
        Handlebars mf = YamlScalar.newHandlebars();
        String result = null;
        try {
            Template template = mf.compile(pathComponentCrudTemplate);
            result = template.apply(inputParameter);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Creating CRUD path component failed.", e);
            c.error("Creating CRUD path component failed: " + e.getLocalizedMessage());
        }
        return nextLine + result;
    }

    /**
     * Gives back an Inputparameter instance with pre-filled default values.
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
        if (sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.YAMLFILE.toString()) || sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            in.setInputFile(input);
        } else if (sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.YAMLSTRING.toString())) {
            in.setSampleYamlData(input);
        } else if (sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.YAMLSTRINGBASE64.toString())) {
            in.setSampleYamlBase64(input);
        }
        return in;
    }

    /**
     * @deprecated internal step of the generation, use {@link #generate(InputParameter)}.
     */
    @Deprecated
    public List<InputParameter> createMustacheDataFromJSON(InputParameter mainInputParameter) {
        // JSON is valid Yaml.
        return this.createMustacheDataFromYaml(mainInputParameter);
    }

    /**
     * @deprecated internal step of the generation, use {@link #generate(InputParameter)}.
     */
    @Deprecated
    public List<InputParameter> createMustacheDataFromYaml(InputParameter mainInputParameter) {
        return this.legacy(c -> this.mustacheDataFromYaml(mainInputParameter, c));
    }

    private List<InputParameter> mustacheDataFromYaml(InputParameter mainInputParameter, Collector c) {
        /** if STRING the data are already there. */
        LinkedHashMap<String, Object> allObjectsMap = null;

        List<InputParameter> inputParameterList = new ArrayList<>();
        try {
            allObjectsMap = Util.readYamlFromString(mainInputParameter.getSampleYamlData());
            if (allObjectsMap == null || allObjectsMap.isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            String source = mainInputParameter.getInputFile() != null ? "file " + mainInputParameter.getInputFile() : "string";
            c.error("Could not parse input (" + source + "): not valid Yaml/JSON.");
            return null;
        }

        /** Loop for objects. */
        int max = 0;
        for (String key : allObjectsMap.keySet()) {
            InputParameter inputParameter = new InputParameter(mainInputParameter);
            max++;
            if (max > inputParameter.getMaxobjects()) {
                c.warning(maxObjectsMessage(inputParameter.getMaxobjects()));
                break;
            }
            HashMap<String, Object> resultMap = new HashMap<>();
            boolean breakLoop = false;
            LinkedHashMap<String, Object> propMap = null;
//        String firstKey = map.keySet().iterator().next();
            /** Read input data sample. */
            Object ob = allObjectsMap.get(key);
            String cl = ob == null ? "" : ob.getClass().toString();
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
                Object rawValue = propMap.get(propKey);
                String value = rawValue == null ? "" : rawValue.toString().trim();
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
    private HashMap<String, Object> createMustacheDataFromExcel(InputParameter inputParameter, List<Map<String, String>> mapList, Collector c) {
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
        checkHeaderValidity(mapList.get(0), inputParameter.getResource(), c);
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
                    logger.fine(logPrefix + "Found 3 or more empty lines beginning row " + (idx - 1) + ", assuming no more data and stop reading lines here..");
                    break;
                }
                continue;
            }
            if (key == null || key.equals("")) {
                key = UNDEFINED;
                c.warning(logPrefix + "Excel header '" + Header.NAME + "' is missing'. Name of object will be 'undefined'.");
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
                    c.error("Type mapping error in line " + idx + ", key: " + key);
                    c.error("Type cannot be mapped or is not valid. Breaking because stopOnError is set to true.");
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
                    c.warning(logPrefix + "row " + (idx + 1) + ", property '" + key + "': type '" + type + "' is not valid, using string.");
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
                        || (type.equalsIgnoreCase("integer") && (format.equalsIgnoreCase("float") || format.equalsIgnoreCase("double")))) {
                    c.warning(logPrefix + "Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
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
                propertyData.setDescription(oasDescription);
            }

            String pattern = sheetCIMap.get(Header.OASPATTERN.toString());
            /* Older mappings contained the pattern already wrapped in apostrophes (raw insertion). */
            if (pattern != null && pattern.length() > 2 && pattern.startsWith("'") && pattern.endsWith("'")) {
                pattern = pattern.substring(1, pattern.length() - 1);
            }
            propertyData.setPattern(pattern);

            if (sheetCIMap.containsKey(Header.OASREQUIRED)) {
                propertyData.setRequired(Boolean.parseBoolean(sheetCIMap.get(Header.OASREQUIRED)));
            } else if (sheetCIMap.containsKey(Header.DBNULLABLE)) {
                propertyData.setRequired(!Boolean.parseBoolean(sheetCIMap.get(Header.DBNULLABLE)));
            } else {
                propertyData.setRequired(true);
            }

            propertyData.setEnumitems(this.getOasEnum(sheetCIMap.get(Header.OASENUM), propertyData.getType()));

            String max = sheetCIMap.get(Header.OASMAX);
            if (Util.isNumber(max)) {
                propertyData.setMaxLength(Integer.parseInt(max));
            } else if (max != null && max.equals("") == false) {
                c.warning(logPrefix + "Value for maxLength is not a number: " + max);
            }
            list.add(propertyData);
        }
        resultMap.put("data", list);
        inputParameter.setSchemaData(resultMap);
        return resultMap;
    }

    private void checkHeaderValidity(Map<String, String> map, String sheetName, Collector c) {
        if (map == null) return;
        for (String key : map.keySet()) {
            if (key == null || key.isEmpty()) continue;
            try {
                Header header = Header.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException iae) {
                c.warning("Unknown Excel header: '" + key + "' in sheet " + sheetName + ".");
            }
        }
    }

    private List<PropertyData.EnumItem> getOasEnum(String e, String type) {
        if (e == null || e.equals("")) return null;
        boolean string = type != null && type.equalsIgnoreCase("string");
        List<PropertyData.EnumItem> items = new ArrayList<>();
        for (String item : e.split(",")) {
            items.add(new PropertyData.EnumItem(item.trim(), string));
        }
        return items;
    }

    //TODO set resource ?
    private List<InputParameter> createExcelInputParameterList(HashMap<String, List<Map<String, String>>> map, InputParameter in, Collector c) {
        List<InputParameter> result = new ArrayList<>();
        for (String index : map.keySet()) {
            List<Map<String, String>> mapList = map.get(index);
            StringBuilder yaml = new StringBuilder(index + ": " + "´\n");
            InputParameter inputParameter = new InputParameter(in);
            inputParameter.setResource(index);
            inputParameter.setSchemaData(this.createMustacheDataFromExcel(inputParameter, mapList, c));
            result.add(inputParameter);
        }
        return result;
    }

    private static String maxObjectsMessage(int max) {
        return "Maximum number of objects (" + max + ") reached, further objects are skipped.";
    }

    private static String formatName(InputParameter inputParameter) {
        InputParameter.Sourcetype type = inputParameter.getSourceType();
        return (type == InputParameter.Sourcetype.JSONFILE || type == InputParameter.Sourcetype.JSONSTRING) ? "JSON" : "Yaml";
    }
}
