package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.render.PropertyView;
import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.application.SpecGenerator;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.port.SourceInput;

import java.io.UncheckedIOException;
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
    private final Wiring wiring = new Wiring();

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
        DiagnosticCollector c = new DiagnosticCollector();
        String spec = this.run(inputParameter, LegacyMapper.Kind.AUTO, c);
        if (spec == null && !c.hasErrors()) {
            c.error("No OpenAPI specification could be generated, no usable input data found.");
        }
        return GenerationResult.of(spec, c.list());
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
        DiagnosticCollector c = new DiagnosticCollector();
        String written = this.write(generated.getSpec(), inputParameter, c, false);
        List<Diagnostic> all = new ArrayList<>(generated.getDiagnostics());
        all.addAll(c.list());
        return GenerationResult.of(generated.getSpec(), all, written);
    }

    /** Validates (prepare), generates and writes the title back into the caller's info. Null if not generated. */
    private String run(InputParameter in, LegacyMapper.Kind kind, DiagnosticCollector c) {
        Optional<LegacyMapper.Request> request = LegacyMapper.prepare(in, kind, c);
        if (request.isEmpty()) return null;
        SpecGenerator.Generated generated = wiring.generator().generate(request.get().source(), request.get().options(), c);
        if (generated.title() != null && in.getOasInfo() != null) in.getOasInfo().setTitle(generated.title());
        return generated.spec();
    }

    /** Deprecated Yaml methods return null for a null input without a message. */
    private String runYaml(InputParameter in, DiagnosticCollector c) {
        return in == null ? null : this.run(in, LegacyMapper.Kind.YAML, c);
    }

    /**
     * Writes the spec. A legacy call resolves every output file below the current folder (also absolute ones), as before.
     *
     * @return the written file or null if writing failed (error added).
     */
    private String write(String spec, InputParameter in, DiagnosticCollector c, boolean legacy) {
        return wiring.writer().write(spec, in.getOutputFile(), legacy, c);
    }

    /** Resets the legacy fields before a deprecated call and fills them from its diagnostics afterwards. */
    private <T> T legacy(java.util.function.Function<DiagnosticCollector, T> call) {
        this.errorMessage = null;
        this.warningMessage = null;
        DiagnosticCollector c = new DiagnosticCollector();
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

    private String methodsFromList(List<InputParameter> list, DiagnosticCollector c) {
        if (list == null || list.isEmpty()) {
            logger.fine("Skipping methods, list is empty.");
            return "{}";
        }
        List<ResourceDefinition> resources = new ArrayList<>();
        for (InputParameter inputParameter : list) {
            if (inputParameter.isDoPaths()) {
                resources.add(LegacyMapper.toResource(inputParameter));
            } else {
                logger.fine("Skipping creating methods (DoPaths = false) for " + inputParameter.getResourceId());
            }
        }
        return wiring.generator().paths(resources, true, c);
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
            return wiring.renderer().componentsHeader() + nextLine;
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

    /**
     * Template context for the schema: the schema data with PropertyData converted to view models.
     */
    private static Map<String, Object> schemaContext(Map<String, Object> schemaData) {
        Map<String, Object> context = new HashMap<>(schemaData);
        Object data = context.get("data");
        if (data instanceof List) {
            List<Object> views = new ArrayList<>();
            for (Object o : (List<?>) data) {
                views.add(o instanceof PropertyData ? new PropertyView(LegacyMapper.toProperty((PropertyData) o)) : o);
            }
            context.put("data", views);
        }
        return context;
    }

    private String schemaObjects(InputParameter inputParameter, DiagnosticCollector c) {
        if (inputParameter.isDoSchemas() == false) {
            logger.fine("Skipping create schema (doSchemas = false).");
            return "";
        }
        logger.fine("Start creating schema objects.");
        logger.fine("Input parameter: \n" + inputParameter);
        String result = null;
        try {
            if (inputParameter.getSchemaData() == null || inputParameter.getSchemaData().isEmpty()) {
                c.error("No data. Please define input file or set sample yaml.");
                return ERROR;
            }
            HashMap sampleMap = inputParameter.getSchemaData();
            if (sampleMap == null || sampleMap.size() == 0) {
                c.error("No data for " + inputParameter.getResource() + ". Please define input file or set sample yaml.");  //TODO check
                return ERROR;
            }
            sampleMap.put(OBJECTNAME, inputParameter.getCapResource());
            result = wiring.renderer().renderSchemaContext(schemaContext(sampleMap));
        } catch (UncheckedIOException e) {
            logger.log(Level.SEVERE, "Creating OpenAPI section failed.", e.getCause());
            c.error(e.getCause().getLocalizedMessage());
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

    private String info(InputParameter inputParameter, DiagnosticCollector c) {
        if (inputParameter.isDoInfo() == false) {
            logger.fine("Skipping info section (doInfo = false).");
            return "";
        }
        logger.fine("Start creating info.");
        logger.fine("Input parameter: \n" + inputParameter);
        String result = null;
        try {
            if (inputParameter.getResource() == null || inputParameter.getResource().length() == 0) {
                c.error("No resource defined.");
                return "Error";
            }
            OasInfo oasInfo = inputParameter.getOasInfo();
            if (oasInfo.getTitle() == null) {
                oasInfo.setTitle(inputParameter.getCapResource() + " API");
            }
            result = wiring.renderer().info(LegacyMapper.toApiInfo(oasInfo));
        } catch (UncheckedIOException e) {
            logger.log(Level.SEVERE, "Creating OpenAPI section failed.", e.getCause());
            c.error(e.getCause().getLocalizedMessage());
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

    private String excelStreamToString(InputParameter inputParameter, DiagnosticCollector c) {
        return this.run(inputParameter, LegacyMapper.Kind.EXCEL_STREAM, c);
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

    private String excelFileToString(InputParameter inputParameter, DiagnosticCollector c) {
        return this.run(inputParameter, LegacyMapper.Kind.EXCEL_FILE, c);
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
            String result = this.runYaml(inputParameter, c);
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
            String result = this.runYaml(inputParameter, c);
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
        return this.legacy(c -> this.runYaml(inputParameter, c));
    }

    /**
     * @deprecated use {@link #generate(InputParameter)} or {@link #generateToFile(InputParameter)}.
     */
    @Deprecated
    public String createFromJSONToString(InputParameter inputParameter) {
        return this.legacy(c -> this.runYaml(inputParameter, c));
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

    private List<InputParameter> mustacheDataFromYaml(InputParameter mainInputParameter, DiagnosticCollector c) {
        InputParameter.Sourcetype type = mainInputParameter.getSourceType();
        boolean json = type == InputParameter.Sourcetype.JSONFILE || type == InputParameter.Sourcetype.JSONSTRING;
        SourceInput source = new SourceInput.YamlText(mainInputParameter.getSampleYamlData(), mainInputParameter.getInputFile(), json);
        List<ResourceDefinition> definitions = new YamlSource().read(source, LegacyMapper.toOptions(mainInputParameter), c);
        return definitions == null ? null : LegacyMapper.toInputParameters(definitions, mainInputParameter);
    }
}
