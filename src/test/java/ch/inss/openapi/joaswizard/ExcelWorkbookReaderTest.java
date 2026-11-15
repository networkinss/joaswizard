package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.source.ExcelWorkbookReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExcelWorkbookReaderTest {

    @Test
    void testMissingFileReturnsNull(@TempDir Path dir) {
        ExcelWorkbookReader w = new ExcelWorkbookReader();
        assertNull(w.readExcelfile(dir.resolve("nothere.xlsx").toString()));
    }

    @Test
    void testExistingFileHasThreeSheets() {
        HashMap<String, List<Map<String, String>>> map = new ExcelWorkbookReader().readExcelfile("src/test/resources/objectimport.xlsx");
        assertNotNull(map);
        assertEquals(3, map.size());
    }

    @Test
    void testEmptySheet(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("empty.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            wb.createSheet("empty");
            wb.createSheet("filled").createRow(0).createCell(0).setCellValue("Name");
            wb.write(out);
        }
        HashMap<String, List<Map<String, String>>> map = new ExcelWorkbookReader().readExcelfile(file.toString());
        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.get("empty").isEmpty());
        assertTrue(map.get("filled").isEmpty());
    }

    @Test
    void testNonXlsxReturnsNull(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.xls");
        Files.write(file, new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        assertNull(new ExcelWorkbookReader().readExcelfile(file.toString()));
    }
}
