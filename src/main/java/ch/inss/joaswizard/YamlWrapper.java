package ch.inss.joaswizard;

import java.util.HashMap;

public class YamlWrapper {
    HashMap map;
    String name;
    
    YamlWrapper(String name){
        this.map = new HashMap(30);
        this.name = name;
    }
    YamlWrapper(String name, HashMap hashMap){
        this.map = hashMap;
        this.name = name;
    }

    public HashMap getMap() {
        return map;
    }

    public void setMap(HashMap map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
