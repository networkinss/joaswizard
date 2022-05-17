package ch.inss.joaswizard;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class ExcelWrapper {
    
    private static Logger logger = null;

    public ExcelWrapper() {
        logger = Logger.getLogger(ExcelWrapper.class.getName());
        for (Handler handler : logger.getHandlers()) {logger.removeHandler(handler);}
        logger.addHandler(Main.consoleHandler);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
    }

    public HashMap<String, List<Map<String, String>>> readExcelfile(String file) {
        InputStream fileStream = null;
        File initialFile = null;
        try {
            initialFile = new File(file);
            fileStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
            logger.severe("Parse excel failed. Filename: " + initialFile.getAbsolutePath());
        }
        return this.readExcelStream(fileStream);
    }

    public HashMap<String, List<Map<String, String>>> readExcelStream(InputStream fileStream) {
        if (fileStream == null) return null;
        HashMap<String, List<Map<String, String>>> map = new HashMap<>();
        try {
            final XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
            List<XSSFName> allNames = workbook.getAllNames();
            Iterator it = workbook.iterator();
            while (it.hasNext()) {
                final XSSFSheet sheet = (XSSFSheet) it.next();
                List<Map<String, String>> list = sheetContent(sheet);
                map.put(sheet.getSheetName(), list);
            }
        } catch (IOException e) {
            logger.severe("Parsing excel failed.");
        }
        return map;
    }

    private List<Map<String, String>> sheetContent(XSSFSheet sheet) {
        final List<Map<String, String>> data = new ArrayList<>();
        final List<Row> rows = new ArrayList<>();

        Iterator it = sheet.iterator();
        while (it.hasNext()) {
            Row row = (Row) it.next();
            rows.add(row);
        }
//             = Lists.newArrayList(sheet);
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

        switch (cell.getCellTypeEnum()) {
            case STRING:
//            case CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getStringCellValue();
            case ERROR:
                return cell.getStringCellValue();
            case NUMERIC:
                final DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            default:
                throw new RuntimeException("unknown cell type: " + cell.getCellType());
        }

    }


}
