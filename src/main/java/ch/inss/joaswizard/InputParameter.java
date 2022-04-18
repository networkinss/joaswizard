package ch.inss.joaswizard;

import org.apache.commons.lang3.StringUtils;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class InputParameter {

    private String resource;
    private String resourceId = "ID";
    private String sampleYaml;
    private String inputFile;
    private String outputFile;
    private Sourcetype sourceType;
    private final String openCurlyBrace = "{";
    private final String closeCurlyBrace = "}";
    private final List<Method> methods = new ArrayList<>();
    
    public List<Method> getMethodList() {
        return methods;
    }
    public void addMethod(String method){
        Method m = Method.valueOf(method.toUpperCase());
        this.methods.add(m);
    }

    public String getCapResource() {
        return StringUtils.capitalize(resource);
    }

    public String getCapResources() {
        return StringUtils.capitalize(resource) + "s";
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResources() {
        return resource + "s";
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
        this.sampleYaml = new String(decoder.decode(sampleYaml));
    }

    public String getSampleYaml() {
        return sampleYaml;
    }

    /** The sample data frm which the output will be generated.*/
    public void setSampleYaml(String sampleYaml) {
        this.sampleYaml = sampleYaml;
    }

    public String getInputFile() {
        return inputFile;
    }

    /** The path to the input file. The content must be stored within the sampleYaml field. */
    public void setInputFile(String inputFile) {
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

    @Override
    public String toString() {
        return "Parameter{" +
                "resource='" + resource + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", sampleYaml='" + sampleYaml + '\'' +
                ", inputFile='" + inputFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", source='" + sourceType + '\'' +
                '}';
    }
    
    public static enum Method{
        POST,
        PUT,
        GET,
        DELETE,
        PATCH
    }
    
    public  static enum Sourcetype{
        YAML,
        EXCEL
    }
}
