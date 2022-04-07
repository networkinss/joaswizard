package ch.inss.joaswizard;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.*;
//import com.google.common.collect.Lists;

import static org.apache.poi.ss.usermodel.Cell.*;

public class ExcelWrapper {
    public HashMap<String, List<Map<String, String>>>  readExcel(String file, String sheetPrefix, String sheetPostfix){
        try {
            HashMap<String, List<Map<String, String>>> map = new HashMap<>();
            final XSSFWorkbook workbook = new XSSFWorkbook(file);
            List<XSSFName> allNames = workbook.getAllNames();
            Iterator it = workbook.iterator();
            while ( it.hasNext() ){
                final XSSFSheet sheet = (XSSFSheet) it.next();
                
                List<Map<String, String>> list = sheetContent(sheet);
                map.put(sheet.getSheetName(),list);
            }
            
            
            return map;
        } catch (IOException e) {
//            LOG.error("parse excel failed. name: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Parsing excel failed.");
        }
    }

    private List<Map<String, String>> sheetContent(XSSFSheet sheet) {
        final List<Map<String, String>> data = new ArrayList<>();
        final List<Row> rows = new ArrayList<>();
        
        Iterator it = sheet.iterator();
        while (it.hasNext()){
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

        switch (cell.getCellType()) {
            case CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case CELL_TYPE_NUMERIC:
                final DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            default:
                throw new RuntimeException("unknown cell type " + cell.getCellType());
        }

    }
    
    
}
