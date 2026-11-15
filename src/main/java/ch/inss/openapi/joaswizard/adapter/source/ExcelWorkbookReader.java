package ch.inss.openapi.joaswizard.adapter.source;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads the sheets of an .xlsx workbook as lists of rows (header name to cell text).
 * The sheets are kept in a HashMap, so their order is the HashMap order of the sheet names.
 * Logs under the logger and source class of the former ExcelWrapper.
 */
public final class ExcelWorkbookReader {

    private static final String LOGGER_NAME = "ch.inss.openapi.joaswizard.ExcelWrapper";
    private static final Logger logger = Logger.getLogger(LOGGER_NAME);

    public HashMap<String, List<Map<String, String>>> readExcelfile(String file) {
        if (file == null) return null;
        try (FileInputStream fileStream = new FileInputStream(file)) {
            return this.readExcelStream(fileStream);
        } catch (IOException e) {
            logger.logp(Level.SEVERE, LOGGER_NAME, "readExcelfile", e.getLocalizedMessage());
            logger.logp(Level.SEVERE, LOGGER_NAME, "readExcelfile", "Parsing Excel failed for file: " + new File(file).getAbsolutePath());
            return null;
        }
    }

    public HashMap<String, List<Map<String, String>>> readExcelStream(InputStream fileStream) {
        if (fileStream == null) return null;
        HashMap<String, List<Map<String, String>>> map = new HashMap<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(fileStream)) {
            Iterator<?> it = workbook.iterator();
            while (it.hasNext()) {
                final XSSFSheet sheet = (XSSFSheet) it.next();
                List<Map<String, String>> list = sheetContent(sheet);
                map.put(sheet.getSheetName(), list);
            }
        } catch (IOException e) {
            logger.logp(Level.SEVERE, LOGGER_NAME, "readExcelStream", "Parsing Excel failed.");
            return null;
        } catch (RuntimeException e) {
            // POI throws e.g. NotOfficeXmlFileException / OLE2NotOfficeXmlFileException for .xls or other formats.
            logger.logp(Level.SEVERE, LOGGER_NAME, "readExcelStream", "Parsing Excel failed. Only .xlsx is supported: " + e.getLocalizedMessage());
            return null;
        }
        return map;
    }

    private List<Map<String, String>> sheetContent(XSSFSheet sheet) {
        final List<Map<String, String>> data = new ArrayList<>();
        final List<Row> rows = new ArrayList<>();

        Iterator<Row> it = sheet.iterator();
        while (it.hasNext()) {
            rows.add(it.next());
        }
        if (rows.isEmpty()) return data;
        final List<String> header = readHeader(rows.get(0));
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            final Row row = rows.get(rowIndex);
            final Map<String, String> rowMap = new HashMap<>();
            for (int cellIndex = 0; cellIndex < header.size(); cellIndex++) {
                rowMap.put(header.get(cellIndex), readCellAsString(row.getCell(cellIndex)));
            }
            data.add(rowMap);
        }
        return data;
    }

    private List<String> readHeader(final Row row) {
        final List<String> header = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            header.add(readCellAsString(row.getCell(i)));
        }
        return header;
    }

    private String readCellAsString(final Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
            case FORMULA:
            case ERROR:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                final DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            default:
                throw new RuntimeException("unknown cell type: " + cell.getCellType());
        }
    }
}
