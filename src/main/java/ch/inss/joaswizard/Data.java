package ch.inss.joaswizard;

import java.util.HashMap;

public class Data {
    private HashMap<String, HashMap<String, Object>> allDataMaps;

    public Data() {
        this.allDataMaps = new HashMap<String, HashMap<String, Object>>();
    }

    public HashMap<String, Object> getDataMap(String resourceKey) {
        if ( this.allDataMaps.containsKey(resourceKey)){
            return this.allDataMaps.get(resourceKey);
        }
        return null;
    }

    public void addDataMap(String resourceKey, HashMap<String, Object> dataMap) {
        this.allDataMaps.put(resourceKey,dataMap);
    }
    public int size(){
        return this.allDataMaps.size();
    }
}
