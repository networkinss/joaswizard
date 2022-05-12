package ch.inss.joaswizard;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class InputParameter {

    private String resource;
    private String resourceId = "ID";
    private String sampleYamlData;
    private String inputFile;
    private String outputFile;
    private String internalid;
    private Sourcetype sourceType;
    private Set<Method> methods = new HashSet<>();

    private boolean doInfo = true;
    private boolean doPaths = true;
    private boolean doSchemas = true;
    private final String openCurlyBrace = "{";
    private final String closeCurlyBrace = "}";

    public InputParameter(String inputFile, String outputFile, Sourcetype sourceType, Set<Method> methods) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.sourceType = sourceType;
        this.methods = methods;
    }

    public InputParameter() {

    }

    public InputParameter(String inputFile, String outputFile, Sourcetype sourceType) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.sourceType = sourceType;
    }

    /**
     * Check if all mandatory parameter have been defined.
     */
    public boolean checkValid() {
        boolean valid = false;
        valid = this.resource != null && (this.inputFile != null || this.sampleYamlData != null) && this.sourceType != null;
        if (this.sourceType == Sourcetype.EXCEL) valid = valid && this.inputFile != null && this.methods.size() > 0;
        return valid;
    }

    public Set<Method> getMethodList() {
        return methods;
    }

    /**
     * You can add one or more methods separated by ",".
     */
    public void addMethod(String m) {
        String[] arr = m.split(",");
        for (String method : arr) {
            Method met = Method.valueOf(method.toUpperCase());
            this.methods.add(met);
        }
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public void setCrud() {
        this.addMethod(Method.GET);
        this.addMethod(Method.PUT);
        this.addMethod(Method.POST);
        this.addMethod(Method.DELETE);
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

    public String getOpenCurlyBrace() {
        return openCurlyBrace;
    }


    public String getCloseCurlyBrace() {
        return closeCurlyBrace;
    }

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
     */
    public void setSampleYamlData(String sampleYamlData) {
        this.sampleYamlData = sampleYamlData;
    }

    public String getInputFile() {
        return inputFile;
    }

    /**
     * The path to the input file. The content must be stored within the sampleYaml field.
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

    @Override
    public String toString() {
        return "InputParameter{" +
                "resource='" + resource + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", sampleYamlData='" + sampleYamlData + '\'' +
                ", inputFile='" + inputFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", internalid='" + internalid + '\'' +
                ", sourceType=" + sourceType +
                ", methods=" + methods +
                '}';
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

    public static enum Method {
        POST,
        PUT,
        GET,
        DELETE,
        PATCH,
        CRUD
    }
    
    public boolean isAllquery(){
        return ! (this.methods.contains(Method.GET) == false && this.methods.contains(Method.POST) == false);
    }
    
    /** If no path with path variable is needed. 
     *  That is the case if only POST is the method. */
    public boolean isPathIdQuery(){
        return ! (this.methods.size() == 1 && this.methods.contains(Method.POST));
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
        YAMLSTRING
    }
}
