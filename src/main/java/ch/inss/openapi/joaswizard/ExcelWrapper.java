package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.source.ExcelWorkbookReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delegating shim, the implementation lives in {@link ExcelWorkbookReader}.
 */
public class ExcelWrapper {

    private final ExcelWorkbookReader reader = new ExcelWorkbookReader();

    public ExcelWrapper() {
    }

    public HashMap<String, List<Map<String, String>>> readExcelfile(String file) {
        return reader.readExcelfile(file);
    }

    public HashMap<String, List<Map<String, String>>> readExcelStream(InputStream fileStream) {
        return reader.readExcelStream(fileStream);
    }
}
