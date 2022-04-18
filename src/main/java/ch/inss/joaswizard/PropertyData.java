package ch.inss.joaswizard;

import java.util.List;

/** Properties of a components/schemas object. */
public class PropertyData {

    private String key;
    private String exampleValue;
    private String type;
    private Boolean minlength;
    private Boolean maxLength;
    private String format;
    private String pattern;
    private List<String> enumValues;
    private Boolean required;
    private String description;

    public Boolean getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Boolean maxLength) {
        this.maxLength = maxLength;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PropertyData(String key, String exampleValue) {
        this.key = key;
        this.exampleValue = exampleValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getExampleValue() {
        return exampleValue;
    }

    public void setExampleValue(String exampleValue) {
        this.exampleValue = exampleValue;
    }

    public Boolean getMinlength() {
        return minlength;
    }

    public void setMinlength(Boolean minlength) {
        this.minlength = minlength;
    }
}
