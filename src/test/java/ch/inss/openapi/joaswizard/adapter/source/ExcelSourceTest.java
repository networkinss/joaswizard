package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.Diagnostic;
import ch.inss.openapi.joaswizard.GenerationResult;
import ch.inss.openapi.joaswizard.InputParameter;
import ch.inss.openapi.joaswizard.Joaswizard;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExcelSourceTest {

    private static void row(XSSFSheet sheet, int i, String... cells) {
        Row r = sheet.createRow(i);
        for (int c = 0; c < cells.length; c++) r.createCell(c).setCellValue(cells[c]);
    }

    private static Path workbook(Path dir, boolean filled) throws Exception {
        Path file = dir.resolve("wb.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            wb.createSheet("empty");
            row(wb.createSheet("headeronly"), 0, "Name", "OasType");
            if (filled) {
                XSSFSheet pet = wb.createSheet("pet");
                row(pet, 0, "Name", "OasType");
                row(pet, 1, "name", "string");
            }
            wb.write(out);
        }
        return file;
    }

    private static InputParameter input(Path file) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputFile(file.toString());
        in.addMethods("get");
        return in;
    }

    private static boolean has(GenerationResult r, Diagnostic.Severity s, String message) {
        return r.getDiagnostics().stream().anyMatch(d -> d.getSeverity() == s && d.getMessage().equals(message));
    }

    @Test
    void emptyAndHeaderOnlySheetsAreSkipped(@TempDir Path dir) throws Exception {
        GenerationResult r = new Joaswizard().generate(input(workbook(dir, true)));
        assertNotNull(r.getSpec(), r.toString());
        assertTrue(has(r, Diagnostic.Severity.WARNING, "Sheet empty is empty, skipped."));
        assertTrue(has(r, Diagnostic.Severity.WARNING, "Sheet headeronly is empty, skipped."));
        assertTrue(r.getSpec().contains("/pets:"));
        assertFalse(r.getSpec().contains("/emptys") || r.getSpec().contains("/headeronlys"));
        assertFalse(r.getSpec().contains("Headeronly:"));
    }

    @Test
    void allSheetsEmptyIsAnError(@TempDir Path dir) throws Exception {
        Joaswizard jo = new Joaswizard();
        GenerationResult r = jo.generate(input(workbook(dir, false)));
        assertNull(r.getSpec());
        assertTrue(has(r, Diagnostic.Severity.ERROR, "No data in the Excel input: all sheets are empty."));
        assertNull(jo.createFromExcelfileToString(input(dir.resolve("wb.xlsx"))));
        assertEquals("No data in the Excel input: all sheets are empty.", jo.getErrorMessage());
    }

    private static Path mappedWorkbook(Path dir) throws Exception {
        Path file = dir.resolve("mapped.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            for (String name : new String[]{"pet", "customer"}) {
                XSSFSheet s = wb.createSheet(name);
                row(s, 0, "Name", "DbType", "OasPattern");
                row(s, 1, "code", "zipcode", "");
                row(s, 2, "other", "zipcode", "^x$");
            }
            wb.write(out);
        }
        return file;
    }

    @Test
    void mappingPatternAppliedAndLoadedOncePerWorkbook(@TempDir Path dir) throws Exception {
        Path mapping = dir.resolve("map.json");
        Files.writeString(mapping, "[{\"dbtype\":\"zipcode\",\"oastype\":\"string\",\"oaspattern\":\"^[0-9]{4}$\"}]");
        InputParameter in = input(mappedWorkbook(dir));
        in.setMappingFile(mapping.toString());
        int before = MappingFileLoader.LOADS.get();
        GenerationResult r = new Joaswizard().generate(in);
        assertEquals(1, MappingFileLoader.LOADS.get() - before, "mapping loaded once for two sheets");
        assertNotNull(r.getSpec(), r.toString());
        String spec = r.getSpec();
        assertTrue(spec.contains("pattern: ^[0-9]{4}$"), spec);
        assertTrue(spec.contains("pattern: ^x$"), "pattern of the cell wins");
    }

    @Test
    void legacyErrorMessageIsTheFirstError(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("mystery.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            XSSFSheet s = wb.createSheet("pet");
            row(s, 0, "Name", "DbType");
            row(s, 1, "mystery", "");
            wb.write(out);
        }
        InputParameter in = input(file);
        in.setStopOnError(true);
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromExcelfileToString(in));
        assertEquals("Type mapping error in line 1, key: mystery", jo.getErrorMessage());
    }

    @Test
    void legacyNullInputReportsMissingInput() {
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromYamlToString(null));
        assertEquals("Input parameter is missing.", jo.getErrorMessage());
    }

    private static GenerationResult generateSheet(Path dir, String[] header, String[]... rows) throws Exception {
        Path file = dir.resolve("sheet.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            XSSFSheet s = wb.createSheet("pet");
            row(s, 0, header);
            for (int i = 0; i < rows.length; i++) row(s, i + 1, rows[i]);
            wb.write(out);
        }
        return new Joaswizard().generate(input(file));
    }

    @Test
    void stringEnumIsQuotedAndNumberEnumIsPlain(@TempDir Path dir) throws Exception {
        GenerationResult r = generateSheet(dir, new String[]{"Name", "OasType", "OasEnum"},
                new String[]{"code", "string", "1,2"}, new String[]{"level", "integer", "1,2"});
        assertNotNull(r.getSpec(), r.toString());
        String spec = r.getSpec();
        assertTrue(spec.matches("(?s).*code:.*enum:\\s*- '?\"?1'?\"?\\s*- '?\"?2'?\"?.*"), spec);
        assertTrue(spec.matches("(?s).*level:.*enum:\\s*- 1\\s*- 2\\s.*"), spec);
    }

    @Test
    void unknownHeaderGivesWarning(@TempDir Path dir) throws Exception {
        GenerationResult r = generateSheet(dir, new String[]{"Name", "OasType", "Bogus"}, new String[]{"id", "integer", "x"});
        assertNotNull(r.getSpec(), r.toString());
        assertTrue(has(r, Diagnostic.Severity.WARNING, "Unknown Excel header: 'Bogus' in sheet pet."), r.toString());
    }

    @Test
    void formatNotFittingTypeGivesWarning(@TempDir Path dir) throws Exception {
        GenerationResult r = generateSheet(dir, new String[]{"Name", "OasType", "OasFormat"}, new String[]{"price", "number", "int32"});
        assertNotNull(r.getSpec(), r.toString());
        assertTrue(r.getDiagnostics().stream().anyMatch(d -> d.getSeverity() == Diagnostic.Severity.WARNING
                && d.getMessage().contains("Check if type and format fit together")), r.toString());
    }

    @Test
    void excelInputStreamGivesInfoAndOperations() throws Exception {
        InputParameter in = new InputParameter();
        in.addMethods("get");
        try (java.io.FileInputStream stream = new java.io.FileInputStream("src/test/resources/objectimport.xlsx")) {
            in.setInputStream(stream);
            java.util.List<String> lines = new Joaswizard().generate(in).getSpec().lines().map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
            assertTrue(lines.containsAll(java.util.List.of("email: john.doe@example.com",
                    "operationId: findInvoiceByid", "operationId: getInvoiceList")));
        }
    }
}
