package ch.inss.joaswizard;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Properties of a components/schemas object.
 */
public class PropertyData {

    private String key;
    private String examplevalue;
    private String type;
    private Integer minlength;
    private Integer maxLength;
    private String format;
    private String pattern;
    private String enumvalues;
    private Boolean required;
    private String description;
    private Boolean isArray;
    private String typeArray;

    public PropertyData(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMinlength() {
        return minlength;
    }

    public void setMinlength(Integer minlength) {
        this.minlength = minlength;
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

    public Boolean getRequired() {
        return required;
    }

    public String getEnumvalues() {
        return enumvalues;
    }

    public void setEnumvalues(String enumvalues) {
        this.enumvalues = enumvalues;
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

    public void setDescriptionApostrophe(String description) {
        this.description = "'" + description + "'";
    }

    public String getType() {
        return type;
    }

    public Boolean getArray() {
        return isArray;
    }

    public void setArray(Boolean array) {
        isArray = array;
    }

    public String getTypeArray() {
        return typeArray;
    }

    public void setTypeArray(String typeArray) {
        this.typeArray = typeArray;
    }
//    public void setType(String type) {
//        this.type = type;
//    }

    public String getKey() {
        return key;
    }

    public String getCapKey() {
        return StringUtils.capitalize(key.toLowerCase());
    }
    public String getLowKey(){
        return key.toLowerCase();
    }

//    public void setKey(String key) {
//        this.key = key;
//    }

    public String getExamplevalue() {
        return examplevalue;
    }

    public void setExamplevalue(String examplevalue) {
        this.examplevalue = examplevalue;
    }

    @Override
    public String toString() {
        return "PropertyData{" +
                "key='" + key + '\'' +
                ", exampleValue='" + examplevalue + '\'' +
                ", type='" + type + '\'' +
                ", minlength=" + minlength +
                ", maxLength=" + maxLength +
                ", format='" + format + '\'' +
                ", pattern='" + pattern + '\'' +
                ", enumValues='" + enumvalues + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                '}';
    }
}
