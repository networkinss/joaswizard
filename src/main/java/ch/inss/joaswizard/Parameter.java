package ch.inss.joaswizard;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

public class Parameter {

    private String resource;
    private String resourceId;
    private String sampleYaml;
    private String inputFile;
    private String outputFile;
    private String sourceType;
    private final String openCurlyBrace = "{";
    private final String closeCurlyBrace = "}";


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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
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
}
