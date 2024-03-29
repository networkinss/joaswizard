package ch.inss.openapi.joaswizard;

public interface Constants {
    String sep = System.getProperty("file.separator");
    String nextLine = "\n";
    //    String nextLine = System.getProperty("line.separator");
    String CURRENT_FOLDER = "." + sep;
    String DEFAULT_OUTPUT_FILE = "openapi.yaml";
    String UNDEFINED = "undefined";
    //    String FROMFILE = "file";
//    String fullCrudTemplate = "fullCrud.yaml";
    String pathComponentCrudTemplate = "pathsCrud.yaml";
    //    String getTemplate = "getIdgetAll.yaml";
    String schemaTemplate = "schema.yaml";
    String componentsErrorTemplate = "componentsErrorModel.yaml";
    String infoTemplate = "info.yaml";
    String DEFAULT_MAPPING = "mapping.json";

    String OBJECTNAME = "objectName";
    String[] DATATYPELIST = {"integer", "number", "string", "boolean"};

    String WRITETOFILE = "writeFile";
    String WRITETOSTRING = "writeToString";

    String OK = "OK";
    String ERROR = "Error";
    String WARNIUNG = "Warning";

    String version = "0.9.9-SNAPSHOT";
}
