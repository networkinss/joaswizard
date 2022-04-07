package ch.inss.joaswizard;

public class inputData {

    private String key;
    private String value;
    private String type;
    private Boolean minlength;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public inputData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getMinlength() {
        return minlength;
    }

    public void setMinlength(Boolean minlength) {
        this.minlength = minlength;
    }
}
