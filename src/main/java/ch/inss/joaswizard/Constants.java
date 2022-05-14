package ch.inss.joaswizard;

public interface Constants {
    String sep = System.getProperty("file.separator");
    String nexLine = System.getProperty("line.separator");
    String CURRENT_FOLDER = "." + sep;
    String DEFAULT_OUTPUT_FILE = "openapi.yaml";
    String UNDEFINED = "undefined";
//    String FROMFILE = "file";
    String fullCrudTemplate = "fullCrud.yaml";
    String pathComponentCrudTemplate = "pathsCrud.yaml";
//    String getTemplate = "getIdgetAll.yaml";
    String schemaTemplate = "schema.yaml";
    String componentsErrorTemplate = "componentsErrorModel.yaml";
    String infoTemplate = "info.yaml";
    
    
    String OBJECTNAME = "objectName";
    String[] DATATYPELIST = {"integer","number","string","boolean"};

    
    String ERROR = "Error";
}
