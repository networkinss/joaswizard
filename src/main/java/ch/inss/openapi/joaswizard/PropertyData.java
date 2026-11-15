package ch.inss.openapi.joaswizard;

import ch.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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
    private List<EnumItem> enumitems;
    private Boolean required;
    private String description;
    private Boolean isArray;
    private String typeArray;

    public PropertyData(String key, String type) {
        this.key = key;
        this.type = type;
    }

    private String ref;
    private String itemsRef;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getItemsRef() {
        return itemsRef;
    }

    public void setItemsRef(String itemsRef) {
        this.itemsRef = itemsRef;
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

    public List<EnumItem> getEnumitems() {
        return enumitems;
    }

    public void setEnumitems(List<EnumItem> enumitems) {
        this.enumitems = enumitems;
    }

    /**
     * One enum value; asString means number-like values are quoted (string typed property).
     */
    public static class EnumItem {
        private final String value;
        private final boolean asString;

        public EnumItem(String value, boolean asString) {
            this.value = value;
            this.asString = asString;
        }

        public String getValue() {
            return value;
        }

        public boolean getAsString() {
            return asString;
        }
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

    /**
     * Example for the template: a list (rendered as flow sequence) for array properties, otherwise the text.
     */
    public Object getExample() {
        if (Boolean.TRUE.equals(isArray) && examplevalue != null
                && examplevalue.startsWith("[") && examplevalue.endsWith("]")) {
            String inner = examplevalue.substring(1, examplevalue.length() - 1);
            List<String> items = new ArrayList<>();
            for (String item : inner.split(",")) {
                if (!item.trim().isEmpty()) items.add(item.trim());
            }
            return items;
        }
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
                ", enumItems='" + enumitems + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                '}';
    }
}
