package ch.inss.joaswizard;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputParameter {

    private String resource;
    private String resourceId = "ID";
    private String sampleYamlData;
    private String inputFile;
    private String outputFile;
    private String internalid;
    private String mappingFile;
    private Integer maxobjects = 100;
    private String[] tags;
    private String[] arrayItems;
    private Sourcetype sourceType;
    private Set<Method> methods = new HashSet<>();
    private HashMap<String, Object> schemaData = new HashMap<>();

    private boolean doInfo = true;
    private boolean doPaths = true;
    private boolean doSchemas = true;

    private boolean stopOnError = false;
    private boolean doDefaultSamples = false;

    /**
     * This is for the mapping.json used for database types.
     * If true, DB types like CHAR(80) in the DBType column of an Excel file will match to CHAR in the mapping.json.
     * It must match at least three characters at the beginning of the type defined in the mapping.json.
     * If false, DB type CHAR(80) must be defined including the (80) in the mapping.json to work.
     * Unmapped types will be mapped to OAS3 type string.
     */
    private boolean prefixMatch = false;

    /* Static fields. */
    private final String openCurlyBrace = "{";
    private final String closeCurlyBrace = "}";

    private Logger logger = null;

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
        this.outputFile = in.getOutputFile();
        this.internalid = in.getInternalid();
        this.mappingFile = in.getMappingFile();
        this.tags = in.getTags();
        this.arrayItems = in.getArrayItems();
        this.sourceType = in.getSourceType();
        this.methods = in.getMethodList();
        this.schemaData = in.getSchemaData();
        this.stopOnError = in.isStopOnError();
        this.doDefaultSamples = in.isDoDefaultSamples();
        this.prefixMatch = in.isPrefixMatch();
        this.maxobjects = in.getMaxobjects();
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
        logger = Logger.getLogger(InputParameter.class.getName());
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(Main.consoleHandler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
    }

    /**
     * Check if all mandatory parameter have been defined.
     *
     * @return if input parameter are valid.
     */
    public boolean checkValid() {
        boolean valid = false;
        if (this.sourceType == Sourcetype.EXCEL) {
            valid = this.inputFile != null && this.methods != null;
        } else if (this.sourceType == Sourcetype.YAMLFILE) {
            valid = this.inputFile != null && this.resource != null && this.resourceId != null;
        } else if (this.sourceType == Sourcetype.YAMLSTRING) {
            valid = this.sampleYamlData != null && this.resource != null && this.resourceId != null;
        }
        return valid;
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
        for (Method s : methods) {
            this.addMethod(s);
        }
    }

    public void setMethods(List<String> methods) {
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
        return StringUtils.capitalize(resource.toLowerCase());
    }

    public String getCapResources() {
        return StringUtils.capitalize(resource) + "s";
    }

    public String getResource() {
        return resource;
    }

    public String getLowerResource() {
        return this.resource.toLowerCase(Locale.ROOT);
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResources() {
        return resource + "s";
    }

    public String getLowerResources() {
        return resource.toLowerCase() + "s";
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

    public String getOpenCurlyBrace() {
        return openCurlyBrace;
    }

    public String getCloseCurlyBrace() {
        return closeCurlyBrace;
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
        int pos = inputFile.lastIndexOf(".");
        String suffix = inputFile.substring(pos);
        if (suffix.equalsIgnoreCase(".yml") || suffix.equalsIgnoreCase(".yaml")) {
            this.setSourceType(Sourcetype.YAMLFILE);
        } else if (suffix.equalsIgnoreCase(".xls") || suffix.equalsIgnoreCase(".xlsx")) {
            this.setSourceType(Sourcetype.EXCEL);
        } else {
            this.setSourceType(Sourcetype.YAMLSTRING);
        }
        this.inputFile = inputFile;
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

    public String getInternalid() {
        return internalid;
    }

    public void setInternalid(String internalid) {
        this.internalid = internalid;
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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getArrayItems() {
        return arrayItems;
    }

    public void setArrayItems(String[] arrayItems) {
        this.arrayItems = arrayItems;
    }

    @Override
    public String toString() {
        return "InputParameter{" +
                "resource='" + resource + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", sampleYamlData='" + sampleYamlData + '\'' +
                ", inputFile='" + inputFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", internalid='" + internalid + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", sourceType=" + sourceType +
                ", methods=" + methods +
                ", doInfo=" + doInfo +
//                ", doPaths=" + doPaths +
//                ", doSchemas=" + doSchemas +
                ", openCurlyBrace='" + openCurlyBrace + '\'' +
                ", closeCurlyBrace='" + closeCurlyBrace + '\'' +
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
        return !(this.methods.contains(Method.GET) == false && this.methods.contains(Method.POST) == false);
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

    public static List getSourcetypeList() {
        return new ArrayList<Sourcetype>(Arrays.asList(Sourcetype.values()));
    }

    public static List getAvalableMethodList() {
        return new ArrayList<Method>(Arrays.asList(Method.values()));
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
        EXCEL,
        YAMLSTRING,
        YAMLSTRINGBASE64
    }
}
