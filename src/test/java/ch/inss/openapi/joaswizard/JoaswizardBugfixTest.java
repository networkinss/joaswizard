package ch.inss.openapi.joaswizard;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JoaswizardBugfixTest {

    private static final String PET = "pet:\n  name: Rex\n  age: 3\n";
    private static final String THREE = "a:\n  x: 1\nb:\n  y: 2\nc:\n  z: 3\n";

    private InputParameter yaml(String data, String resource) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData(data);
        in.setResource(resource);
        in.setResourceId("name");
        in.setCrud();
        return in;
    }

    private Path excel(Path dir, String[] row) throws Exception {
        Path file = dir.resolve("t.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            XSSFSheet sheet = wb.createSheet("thing");
            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("Name");
            h.createCell(1).setCellValue("OasType");
            h.createCell(2).setCellValue("OasFormat");
            Row r = sheet.createRow(1);
            for (int i = 0; i < 3; i++) r.createCell(i).setCellValue(row[i]);
            wb.write(out);
        }
        return file;
    }

    // 1
    @Test
    void testDoPathsFalseGivesNoPaths() {
        InputParameter in = yaml(PET, "pet");
        in.setDoPaths(false);
        String out = new Joaswizard().createFromYamlToString(in);
        assertNotNull(out);
        assertFalse(out.contains("operationId"));
        assertTrue(out.contains("components:"));
    }

    @Test
    void testDoSchemasFalseGivesNoSchemas() {
        InputParameter in = yaml(PET, "pet");
        in.setDoSchemas(false);
        String out = new Joaswizard().createFromYamlToString(in);
        assertNotNull(out);
        assertFalse(out.contains("components:"));
        assertFalse(out.contains("schemas:"));
        assertTrue(out.contains("operationId"));
    }

    // 2
    @Test
    void testCreateSchemaObjectsSkipsWhenDoSchemasFalse() {
        InputParameter in = yaml(PET, "pet");
        in.setDoSchemas(false);
        assertEquals("", new Joaswizard().createSchemaObjects(in));
    }

    // 3
    @Test
    void testInputMandatoryBase64() {
        String b64 = Base64.getEncoder().encodeToString(PET.getBytes(StandardCharsets.UTF_8));
        InputParameter in = new Joaswizard().inputMandatory(b64, "YAMLSTRINGBASE64", "pet");
        assertEquals(PET, in.getSampleYamlData());
        in = new Joaswizard().inputMandatory(b64, "yamlstringbase64", "pet");
        assertEquals(PET, in.getSampleYamlData());
    }

    // 4
    @Test
    void testIntegerWithFloatFormatIsDropped(@TempDir Path dir) throws Exception {
        Path file = excel(dir, new String[]{"count", "integer", "float"});
        InputParameter in = new InputParameter();
        in.setInputFile(file.toString());
        in.addMethods("get");
        Joaswizard jo = new Joaswizard();
        String out = jo.createFromExcelfileToString(in);
        assertNotNull(out);
        assertFalse(out.contains("format: float"));
        assertNotNull(jo.getWarningMessage());
        assertTrue(jo.getWarningMessage().contains("format: float"));
    }

    @Test
    void testValidFormatIsKept(@TempDir Path dir) throws Exception {
        Path file = excel(dir, new String[]{"count", "integer", "int64"});
        InputParameter in = new InputParameter();
        in.setInputFile(file.toString());
        in.addMethods("get");
        String out = new Joaswizard().createFromExcelfileToString(in);
        assertNotNull(out);
        assertTrue(out.contains("format: int64"));
    }

    // 5 (empty header name must not fail)
    @Test
    void testEmptyHeaderIsIgnored(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("e.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            XSSFSheet sheet = wb.createSheet("thing");
            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("Name");
            h.createCell(2).setCellValue("OasType");
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue("a");
            r.createCell(2).setCellValue("string");
            wb.write(out);
        }
        InputParameter in = new InputParameter();
        in.setInputFile(file.toString());
        in.addMethods("get");
        Joaswizard jo = new Joaswizard();
        assertNotNull(jo.createFromExcelfileToString(in));
        assertTrue(jo.getWarningMessage() == null || !jo.getWarningMessage().contains("Unknown Excel header: ''"));
    }

    // 6
    @Test
    void testNoNullPrefixInWarning() {
        InputParameter in = yaml(THREE, "x");
        in.setMaxobjects(1);
        Joaswizard jo = new Joaswizard();
        assertNotNull(jo.createFromYamlToString(in));
        assertNotNull(jo.getWarningMessage());
        assertFalse(jo.getWarningMessage().startsWith("null"));
        assertTrue(jo.getWarningMessage().contains("Maximum number of objects (1) reached"));
    }

    @Test
    void testNullYamlValueDoesNotCrash() {
        InputParameter in = yaml("pet:\n  name: Rex\n  tag:\n", "pet");
        String out = new Joaswizard().createFromYamlToString(in);
        assertNotNull(out);
        assertTrue(out.contains("tag:"));
        assertTrue(out.contains("name:"));
    }

    // 7
    @Test
    void testMaxObjectsExactlyN() {
        InputParameter in = yaml(THREE, "x");
        in.setMaxobjects(2);
        String out = new Joaswizard().createFromYamlToString(in);
        assertNotNull(out);
        assertTrue(out.contains("    A:"));
        assertTrue(out.contains("    B:"));
        assertFalse(out.contains("    C:"));
    }

    @Test
    void testMaxObjectsExcelExactlyN(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("m.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
            for (String n : new String[]{"one", "two", "three"}) {
                XSSFSheet sheet = wb.createSheet(n);
                sheet.createRow(0).createCell(0).setCellValue("Name");
                sheet.createRow(1).createCell(0).setCellValue("f");
            }
            wb.write(out);
        }
        InputParameter in = new InputParameter();
        in.setInputFile(file.toString());
        in.addMethods("get");
        in.setMaxobjects(2);
        Joaswizard jo = new Joaswizard();
        String result = jo.createFromExcelfileToString(in);
        assertNotNull(result);
        int count = 0;
        for (String n : new String[]{"One:", "Two:", "Three:"}) {
            if (result.contains("    " + n)) count++;
        }
        assertEquals(2, count);
    }

    @Test
    void testFirstObjectResourceNotMutated() {
        InputParameter in = yaml("a:\n  x: 1\nb:\n  y: 2\n", "x");
        Joaswizard jo = new Joaswizard();
        java.util.List<InputParameter> list = jo.createMustacheDataFromYaml(in);
        String first = list.get(0).getResource();
        assertNotNull(jo.createFromYamlToString(in));
        assertEquals("a", first);
        assertEquals("a", list.get(0).getResource());
        assertEquals("x", in.getResource());
    }

    // 8
    @Test
    void testExcelInputstreamValidationFailureReturnsNull() {
        InputParameter in = new InputParameter();
        in.addMethods("get");
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromExcelInputstreamToString(in));
        assertNotNull(jo.getErrorMessage());
    }

    @Test
    void testExcelfileMissingReturnsNull(@TempDir Path dir) {
        InputParameter in = new InputParameter();
        in.setInputFile(dir.resolve("missing.xlsx").toString());
        in.addMethods("get");
        assertNull(new Joaswizard().createFromExcelfileToString(in));
    }

    @Test
    void testExcelfileNotXlsxReturnsNull(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("fake.xlsx");
        Files.write(f, "not a workbook".getBytes(StandardCharsets.UTF_8));
        InputParameter in = new InputParameter();
        in.setInputFile(f.toString());
        in.addMethods("get");
        assertNull(new Joaswizard().createFromExcelfileToString(in));
    }

    @Test
    void testExcelInputstreamWorks() throws Exception {
        InputParameter in = new InputParameter();
        in.addMethods("get");
        try (FileInputStream s = new FileInputStream("src/test/resources/objectimport.xlsx")) {
            in.setInputStream(s);
            String out = new Joaswizard().createFromExcelInputstreamToString(in);
            assertNotNull(out);
            assertTrue(out.contains("operationId: getInvoiceList"));
        }
    }

    // 9
    @Test
    void testJsonString() {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.JSONSTRING);
        in.setSampleYamlData("{\"id\": 1, \"firstname\": \"Max\", \"phone\": 123456789}");
        in.setResource("contact");
        in.setResourceId("id");
        in.setCrud();
        String out = new Joaswizard().createFromJSONToString(in);
        assertNotNull(out);
        assertTrue(out.contains("    Contact:"));
        assertTrue(out.contains("firstname:"));
    }

    @Test
    void testJsonFile() {
        InputParameter in = new InputParameter();
        in.setInputFile("src/test/resources/ContactJSON.json");
        assertEquals(InputParameter.Sourcetype.JSONFILE, in.getSourceType());
        in.setResource("contact");
        in.setResourceId("id");
        in.setCrud();
        String out = new Joaswizard().createFromJSONToString(in);
        assertNotNull(out);
        assertTrue(out.contains("    Contact:"));
        assertTrue(out.contains("email:"));
    }

    @Test
    void testJsonFileMissing(@TempDir Path dir) {
        InputParameter in = new InputParameter();
        in.setInputFile(dir.resolve("none.json").toString());
        in.setResource("contact");
        in.setCrud();
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromJSONToString(in));
        assertNotNull(jo.getErrorMessage());
    }

    // 11 (resource names are normalized: first letter upper, rest lower)
    @Test
    void testMixedCaseResource() {
        InputParameter in = yaml("PET:\n  name: Rex\n", "PET");
        String out = new Joaswizard().createFromYamlToString(in);
        assertNotNull(out);
        assertTrue(out.contains("    Pet:"));
        assertTrue(out.contains("#/components/schemas/Pet"));
    }

    // log message tests
    @Test
    void testParseErrorNamesStringSource() {
        InputParameter in = yaml("price", "object");
        Joaswizard jo = new Joaswizard();
        jo.createFromYamlToString(in);
        assertEquals("Could not parse input (string): not valid Yaml/JSON.", jo.getErrorMessage());
    }

    @Test
    void testParseErrorNamesFileSource(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("bad.yml");
        Files.write(f, "price".getBytes(StandardCharsets.UTF_8));
        InputParameter in = new InputParameter();
        in.setInputFile(f.toString());
        in.setResource("object");
        in.setResourceId("name");
        in.setCrud();
        Joaswizard jo = new Joaswizard();
        jo.createFromYamlToString(in);
        assertEquals("Could not parse input (file " + f + "): not valid Yaml/JSON.", jo.getErrorMessage());
    }

    @Test
    void testJsonFileMissingSaysJson(@TempDir Path dir) {
        InputParameter in = new InputParameter();
        in.setInputFile(dir.resolve("none.json").toString());
        in.setSourceType(InputParameter.Sourcetype.JSONFILE);
        in.setResource("contact");
        in.setCrud();
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromJSONToString(in));
        assertTrue(jo.getErrorMessage().startsWith("JSON file not found: "), jo.getErrorMessage());
    }

    @Test
    void testYamlFileMissingSaysYaml(@TempDir Path dir) {
        InputParameter in = new InputParameter();
        in.setInputFile(dir.resolve("none.yml").toString());
        in.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        in.setResource("contact");
        in.setCrud();
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromYamlToString(in));
        assertTrue(jo.getErrorMessage().startsWith("Yaml file not found: "), jo.getErrorMessage());
    }

    @Test
    void testMaxObjectsWarningSingleSentence() {
        InputParameter in = yaml(THREE, "x");
        in.setMaxobjects(2);
        Joaswizard jo = new Joaswizard();
        assertNotNull(jo.createFromYamlToString(in));
        assertEquals("Maximum number of objects (2) reached, further objects are skipped.", jo.getWarningMessage());
    }

    @Test
    void testInvalidExcelTypeWarningNamesRowAndProperty(@TempDir Path dir) throws Exception {
        Path file = excel(dir, new String[]{"foo", "wrongtype", ""});
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputFile(file.toString());
        in.setCrud();
        Joaswizard jo = new Joaswizard();
        assertNotNull(jo.createFromExcelfileToString(in));
        assertEquals("Sheet thing: row 2, property 'foo': type 'wrongtype' is not valid, using string.", jo.getWarningMessage());
    }
}
