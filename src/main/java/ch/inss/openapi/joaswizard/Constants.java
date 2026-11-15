package ch.inss.openapi.joaswizard;

/**
 * Shared constants. Use static imports or qualified references.
 */
public final class Constants {
    public static final String sep = System.getProperty("file.separator");
    public static final String nextLine = "\n";
    public static final String DEFAULT_OUTPUT_FILE = "openapi.yaml";
    public static final String UNDEFINED = "undefined";
    public static final String DEFAULT_MAPPING = "mapping.json";

    public static final String OBJECTNAME = "objectName";
    public static final String[] DATATYPELIST = {"integer", "number", "string", "boolean"};

    public static final String ERROR = "Error";

    public static final String version = Util.readVersion();

    private Constants() {
    }
}
