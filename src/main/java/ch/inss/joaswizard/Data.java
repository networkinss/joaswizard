package ch.inss.joaswizard;

import java.util.HashMap;

public class Data {
    private HashMap<String, Object> dataMap;

    public Data(HashMap<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public Data() {
        
    }

    public HashMap<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(HashMap<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
}
