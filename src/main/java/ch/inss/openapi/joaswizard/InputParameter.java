package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.domain.ResourceName;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class InputParameter {

    private String resource;
    private String resourceId = "id";
    private String sampleYamlData;
    private String inputFile;
    private InputStream inputStream;
    private String outputFile;
    private String mappingFile;
    private Integer maxobjects = 100;
    private Sourcetype sourceType;
    private Set<Method> methods = new HashSet<>();
    private HashMap<String, Object> schemaData = new HashMap<>();

    private boolean doInfo = true;
    private boolean doPaths = true;
    private boolean doSchemas = true;

    private boolean stopOnError = false;
    private boolean doDefaultSamples = false;

    private OasInfo oasInfo;

    /**
     * This is for the mapping.json used for database types.
     * If true, DB types like CHAR(80) in the DBType column of an Excel file will match to CHAR in the mapping.json.
     * It must match at least three characters at the beginning of the type defined in the mapping.json.
     * If false, DB type CHAR(80) must be defined including the (80) in the mapping.json to work.
     * Unmapped types will be mapped to OAS3 type string.
     */
    private boolean prefixMatch = false;

    private static final Logger logger = Logger.getLogger(InputParameter.class.getName());

    public InputParameter(String inputFile, String outputFile, Sourcetype sourceType, Set<Method> methods, String mappingFile, boolean prefixMatch) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.sourceType = sourceType;
        this.methods = methods;
        this.mappingFile = mappingFile;
        this.prefixMatch = prefixMatch;
        this.initialize();
    }

    public InputParameter(InputParameter in) {
        this.resource = in.getResource();
        this.resourceId = in.getResourceId();
        this.sampleYamlData = in.getSampleYamlData();
        this.inputFile = in.getInputFile();
        this.inputStream = in.getInputStream();
        this.outputFile = in.getOutputFile();
        this.mappingFile = in.getMappingFile();
        this.sourceType = in.getSourceType();
        this.methods = in.getMethodList() == null ? null : new HashSet<>(in.getMethodList());
        this.schemaData = in.getSchemaData() == null ? null : new HashMap<>(in.getSchemaData());
        this.doInfo = in.isDoInfo();
        this.doPaths = in.isDoPaths();
        this.doSchemas = in.isDoSchemas();
        this.stopOnError = in.isStopOnError();
        this.doDefaultSamples = in.isDoDefaultSamples();
        this.prefixMatch = in.isPrefixMatch();
        this.maxobjects = in.getMaxobjects();
        this.oasInfo = in.getOasInfo();
    }

    public InputParameter() {
        this.initialize();
    }

    public InputParameter(String inputFile, String outputFile, Sourcetype sourceType) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.sourceType = sourceType;
        this.initialize();
    }

    private void initialize() {
        this.oasInfo = new OasInfo();
    }


    /**
     * Check if all mandatory parameter have been defined.
     *
     * @return if input parameter are valid.
     */
    public boolean checkValid() {
        List<String> problems = LegacyMapper.structuralProblems(this);
        if (problems.contains(LegacyMapper.RESOURCE_MISSING)) {
            logger.severe(LegacyMapper.RESOURCE_MISSING);
        }
        return problems.isEmpty();
    }

    public Set<Method> getMethodList() {
        return methods;
    }

    /**
     * You can add one or more methods separated by ",".
     *
     * @param m A HTTP method.
     */
    public void addMethods(String m) {
        if (m == null) return;
        String[] arr = m.split(",");
        for (String method : arr) {
            try {
                Method met = Method.valueOf(method.trim().toUpperCase());
                if (met == Method.CRUD) {
                    this.setCrud();
                } else {
                    this.methods.add(met);
                }
            } catch (IllegalArgumentException iae) {
                logger.severe("Not a valid REST API method: " + method);
            }
        }
    }

    public void setMethods(Set<Method> methods) {
        if (methods == null) return;
        for (Method s : methods) {
            this.addMethod(s);
        }
    }

    public void setMethods(List<String> methods) {
        if (methods == null) return;
        for (String s : methods) {
            this.addMethods(s);
        }
    }

    public void addMethod(Method method) {
        if (method == Method.CRUD) {
            this.setCrud();
        } else {
            this.methods.add(method);
        }
    }

    public void setCrud() {
        this.methods.add(Method.GET);
        this.methods.add(Method.PUT);
        this.methods.add(Method.POST);
        this.methods.add(Method.DELETE);
    }

    public HashMap<String, Object> getSchemaData() {
        return schemaData;
    }

    public void setSchemaData(HashMap<String, Object> schemaData) {
        this.schemaData = schemaData;
    }

    public boolean isGet() {
        return this.methods.contains(Method.GET);
    }

    public boolean isPost() {
        return this.methods.contains(Method.POST);
    }

    public boolean isPut() {
        return this.methods.contains(Method.PUT);
    }

    public boolean isPatch() {
        return this.methods.contains(Method.PATCH);
    }

    public boolean isDelete() {
        return this.methods.contains(Method.DELETE);
    }

    public String getCapResource() {
        return new ResourceName(resource).cap();
    }

    public String getCapResources() {
        return new ResourceName(resource).capPlural();
    }

    public String getResource() {
        return resource;
    }

    public String getLowerResource() {
        return new ResourceName(resource).lower();
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResources() {
        return new ResourceName(resource).plural();
    }

    public String getLowerResources() {
        return new ResourceName(resource).lowerPlural();
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isDoDefaultSamples() {
        return doDefaultSamples;
    }

    public void setDoDefaultSamples(boolean doDefaultSamples) {
        this.doDefaultSamples = doDefaultSamples;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public boolean isDoInfo() {
        return doInfo;
    }

    public void setDoInfo(boolean doInfo) {
        this.doInfo = doInfo;
    }

    public boolean isDoPaths() {
        return doPaths;
    }

    public void setDoPaths(boolean doPaths) {
        this.doPaths = doPaths;
    }

    public boolean isDoSchemas() {
        return doSchemas;
    }

    public void setDoSchemas(boolean doSchemas) {
        this.doSchemas = doSchemas;
    }

    public boolean isPrefixMatch() {
        return prefixMatch;
    }

    public void setPrefixMatch(boolean prefixMatch) {
        this.prefixMatch = prefixMatch;
    }

    /**
     * Set the Yaml object properties as Base64 encoded string.
     *
     * @param sampleYaml sample data properties in yaml format base64 encoded.
     */
    public void setSampleYamlBase64(String sampleYaml) {
        Base64.Decoder decoder = Base64.getDecoder();
        // Decoding string  
        this.sampleYamlData = new String(decoder.decode(sampleYaml));
    }

    public String getSampleYamlData() {
        return sampleYamlData;
    }

    /**
     * The sample data frm which the output will be generated.
     *
     * @param sampleYamlData Sample properties in Yaml format.
     */
    public void setSampleYamlData(String sampleYamlData) {
        this.sampleYamlData = sampleYamlData;
    }

    public String getInputFile() {
        return inputFile;
    }

    /**
     * The path to the input file. The content must be stored within the sampleYaml field.
     *
     * @param inputFile Path to the input file.
     */
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
        if (inputFile == null) {
            return;
        }
        int pos = inputFile.lastIndexOf(".");
        if (pos < 1) {
            return;
        }
        String suffix = inputFile.substring(pos);
        if (suffix.equalsIgnoreCase(".yml") || suffix.equalsIgnoreCase(".yaml")) {
            this.setSourceType(Sourcetype.YAMLFILE);
        } else if (suffix.equalsIgnoreCase(".xls") || suffix.equalsIgnoreCase(".xlsx")) {
            this.setSourceType(Sourcetype.EXCEL);
        } else if (suffix.equalsIgnoreCase(".json")) {
            this.setSourceType(Sourcetype.JSONFILE);
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public Sourcetype getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = Sourcetype.valueOf(sourceType.toUpperCase());
    }

    public void setSourceType(Sourcetype sourceType) {
        this.sourceType = sourceType;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public void setMaxobjects(Integer maxobjects) {
        this.maxobjects = maxobjects;
    }

    public Integer getMaxobjects() {
        return maxobjects;
    }

    public OasInfo getOasInfo() {
        return oasInfo;
    }

    public void setOasInfo(OasInfo oasInfo) {
        this.oasInfo = oasInfo;
    }

    @Override
    public String toString() {
        return "InputParameter{" +
                "resource='" + resource + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", sampleYamlData='" + sampleYamlData + '\'' +
                ", inputFile='" + inputFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", sourceType=" + sourceType +
                ", methods=" + methods +
                ", doInfo=" + doInfo +
                '}';
    }


    public enum Method {
        POST,
        PUT,
        GET,
        DELETE,
        PATCH,
        QUERY,
        CRUD,
        EMPTY
    }

    public boolean isAllquery() {
        return this.methods.contains(Method.GET) || this.methods.contains(Method.POST);
    }

    /**
     * If no path with path variable is needed.
     * That is the case if only POST is the method.
     *
     * @return If there are any path variables.
     */
    public boolean isPathIdQuery() {
        return !(this.methods.size() == 1 && this.methods.contains(Method.POST));
    }

    public static List<Sourcetype> getSourcetypeList() {
        return new ArrayList<>(Arrays.asList(Sourcetype.values()));
    }

    public static List<Method> getAvalableMethodList() {
        return new ArrayList<>(Arrays.asList(Method.values()));
    }

    public static boolean isValidSourcetype(String type) {
        try {
            Sourcetype.valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if methods are valid. You can put several methods separated by ",".
     *
     * @param m HTTP method as string.
     * @return If it is a valid HTTP method.
     */
    public static boolean isValidMethod(String m) {
        String[] arr = m.split(",");
        try {
            for (String t : arr) {
                Method.valueOf(t.toUpperCase());
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static enum Sourcetype {
        YAMLFILE,
        JSONFILE,
        EXCEL,
        YAMLSTRING,
        JSONSTRING,
        YAMLSTRINGBASE64
    }
}
