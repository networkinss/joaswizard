package ch.inss.openapi.joaswizard;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterisation test for the topic 4 refactoring: pins the observable behaviour of the public API
 * (generated spec, ordered diagnostics, mutations of the InputParameter, legacy messages).
 * The expected specs are stored in src/test/resources/guard/ and can be regenerated with -Dguard.update=true
 * (only do that for an intended behaviour change). Expected diagnostics and mutations are inline in the cases.
 */
class RefactorGuardTest {

    private static final String RES = "src/test/resources/";
    private static final Path GUARD_DIR = Paths.get(RES + "guard");
    private static final boolean UPDATE = Boolean.getBoolean("guard.update");

    /** Which deprecated string method is called for the legacy check. */
    private enum Legacy {YAML, JSON, EXCEL_FILE, EXCEL_STREAM}

    private static final class Case {
        final String name;
        final Legacy legacy;
        final Supplier<InputParameter> input;
        /** Diagnostics of generate(), one "SEVERITY: message" per line, in order. */
        final String diagnostics;
        /** InputParameter state after the deprecated call. */
        final String mutations;
        /** errorMessage / warningMessage after the deprecated call. */
        final String legacyMessages;

        Case(String name, Legacy legacy, Supplier<InputParameter> input, String diagnostics, String mutations, String legacyMessages) {
            this.name = name;
            this.legacy = legacy;
            this.input = input;
            this.diagnostics = diagnostics;
            this.mutations = mutations;
            this.legacyMessages = legacyMessages;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // ---- input builders -------------------------------------------------------------------------------------

    private static String read(String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(RES + file)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static InputParameter yamlString(String resource, String data, String methods) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setResource(resource);
        in.setSampleYamlData(data);
        if (methods != null) in.addMethods(methods);
        return in;
    }

    private static InputParameter file(InputParameter.Sourcetype type, String resource, String path, String methods) {
        InputParameter in = new InputParameter();
        in.setSourceType(type);
        in.setResource(resource);
        in.setInputFile(path);
        in.setSourceType(type);
        if (methods != null) in.addMethods(methods);
        return in;
    }

    private static InputParameter excelFile(String path, String methods) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputFile(path);
        if (methods != null) in.addMethods(methods);
        return in;
    }

    private static InputParameter excelStream(String path, String methods) {
        InputParameter in = new InputParameter();
        try {
            in.setInputStream(new FileInputStream(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (methods != null) in.addMethods(methods);
        return in;
    }

    /** Workbook with one sheet "pet": row 2 has a name but neither OasType nor DbType (unmappable). */
    private static String unmappableWorkbook() {
        Path dir = Paths.get("target", "guard-fixtures");
        Path file = dir.resolve("unmappable.xlsx");
        try {
            Files.createDirectories(dir);
            try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file.toFile())) {
                Sheet sheet = wb.createSheet("pet");
                Row h = sheet.createRow(0);
                h.createCell(0).setCellValue("Name");
                h.createCell(1).setCellValue("OasType");
                h.createCell(2).setCellValue("DbType");
                Row r1 = sheet.createRow(1);
                r1.createCell(0).setCellValue("id");
                r1.createCell(1).setCellValue("integer");
                Row r2 = sheet.createRow(2);
                r2.createCell(0).setCellValue("mystery");
                wb.write(out);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return file.toString();
    }

    // ---- cases ----------------------------------------------------------------------------------------------

    static Stream<Case> cases() {
        List<Case> l = new ArrayList<>();
        InputParameter.Sourcetype YF = InputParameter.Sourcetype.YAMLFILE;
        InputParameter.Sourcetype JF = InputParameter.Sourcetype.JSONFILE;

        l.add(new Case("yamlString_pet_crud", Legacy.YAML,
                () -> yamlString("pet", read("Pet.yml"), "crud"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLSTRING" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlFile_pet_defaultMethods", Legacy.YAML,
                () -> file(YF, "pet", RES + "Pet.yml", null),
                "WARNING: No REST API methods defined, setting to default CRUD.", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet API", "error=null" +
                "\n" + "warning=No REST API methods defined, setting to default CRUD."));
        l.add(new Case("yamlFile_contact_crud", Legacy.YAML,
                () -> file(YF, "contact", RES + "Contact.yml", "crud"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Contact API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlFile_multiObject", Legacy.YAML,
                () -> file(YF, "pet", RES + "YamlObjects.yml", "get,post"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET, POST]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet, customer, invoice API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlFile_multiObject_max1", Legacy.YAML, () -> {
            InputParameter in = file(YF, "pet", RES + "YamlObjects.yml", "get,post");
            in.setMaxobjects(1);
            return in;
        }, "WARNING: Maximum number of objects (1) reached, further objects are skipped.", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET, POST]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet API", "error=null" +
                "\n" + "warning=Maximum number of objects (1) reached, further objects are skipped."));
        l.add(new Case("jsonString_contact", Legacy.JSON, () -> {
            InputParameter in = yamlString("contact", read("ContactJSON.json"), "get,patch");
            in.setSourceType(InputParameter.Sourcetype.JSONSTRING);
            return in;
        }, "", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET, PATCH]" +
                "\n" + "sourceType=JSONSTRING" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Contact API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("jsonFile_contact", Legacy.JSON,
                () -> file(JF, "contact", RES + "ContactJSON.json", "crud"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=JSONFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Contact API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("excelFile_objectimport_get", Legacy.EXCEL_FILE,
                () -> excelFile(RES + "objectimport.xlsx", "get"),
                "WARNING: Unknown Excel header: 'Position' in sheet Invoice." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Customer." +
                "\n" + "WARNING: Sheet Customer: row 3, property 'secondName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Sheet Customer: row 4, property 'thirdName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Pet.", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=Invoice, customer, pet API", "error=null" +
                "\n" + "warning=Unknown Excel header: 'Position' in sheet Pet."));
        l.add(new Case("excelFile_mysqlMapping", Legacy.EXCEL_FILE, () -> {
            InputParameter in = excelFile(RES + "mySQLObjectimport.xlsx", "post,patch,delete");
            in.setMappingFile(RES + "mysqlMapping.json");
            in.setPrefixMatch(true);
            return in;
        }, "WARNING: Unknown Excel header: 'Position' in sheet Invoice." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Customer." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Pet.", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, PATCH, POST]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=Invoice, customer, pet API", "error=null" +
                "\n" + "warning=Unknown Excel header: 'Position' in sheet Pet."));
        l.add(new Case("excelStream_objectimport_get", Legacy.EXCEL_STREAM,
                () -> excelStream(RES + "objectimport.xlsx", "get"),
                "WARNING: Unknown Excel header: 'Position' in sheet Invoice." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Customer." +
                "\n" + "WARNING: Sheet Customer: row 3, property 'secondName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Sheet Customer: row 4, property 'thirdName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Pet.", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=Invoice, customer, pet API", "error=null" +
                "\n" + "warning=Unknown Excel header: 'Position' in sheet Pet."));
        l.add(new Case("excelFile_stopOnError_unmappable", Legacy.EXCEL_FILE, () -> {
            InputParameter in = excelFile(unmappableWorkbook(), "get");
            in.setStopOnError(true);
            return in;
        }, "ERROR: Type mapping error in line 2, key: mystery" +
                "\n" + "ERROR: Type cannot be mapped or is not valid. Breaking because stopOnError is set to true." +
                "\n" + "ERROR: No data. Please define input file or set sample yaml.", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=null", "error=No data. Please define input file or set sample yaml." +
                "\n" + "warning=null"));
        l.add(new Case("excelFile_noStopOnError_unmappable", Legacy.EXCEL_FILE,
                () -> excelFile(unmappableWorkbook(), "get"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=Pet API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("excelFile_max1", Legacy.EXCEL_FILE, () -> {
            InputParameter in = excelFile(RES + "objectimport.xlsx", "get");
            in.setMaxobjects(1);
            return in;
        }, "WARNING: Unknown Excel header: 'Position' in sheet Invoice." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Customer." +
                "\n" + "WARNING: Sheet Customer: row 3, property 'secondName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Sheet Customer: row 4, property 'thirdName': type 'wrongtype' is not valid, using string." +
                "\n" + "WARNING: Unknown Excel header: 'Position' in sheet Pet." +
                "\n" + "WARNING: Maximum number of objects (1) reached, further objects are skipped.", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET]" +
                "\n" + "sourceType=EXCEL" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=Invoice API", "error=null" +
                "\n" + "warning=Maximum number of objects (1) reached, further objects are skipped."));
        l.add(new Case("yamlFile_multiObject_doPathsFalse", Legacy.YAML, () -> {
            InputParameter in = file(YF, "pet", RES + "YamlObjects.yml", "crud");
            in.setDoPaths(false);
            return in;
        }, "", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet, customer, invoice API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlFile_multiObject_doSchemasFalse", Legacy.YAML, () -> {
            InputParameter in = file(YF, "pet", RES + "YamlObjects.yml", "crud");
            in.setDoSchemas(false);
            return in;
        }, "", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet, customer, invoice API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlString_postOnly", Legacy.YAML,
                () -> yamlString("pet", read("Pet.yml"), "post"),
                "", "outputFile=openapi.yaml" +
                "\n" + "methods=[POST]" +
                "\n" + "sourceType=YAMLSTRING" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Pet API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlString_getPatch_customInfo", Legacy.YAML, () -> {
            InputParameter in = yamlString("category", read("Pet.yml"), "get,patch");
            in.getOasInfo().setDescription("Custom description: with colon");
            return in;
        }, "", "outputFile=openapi.yaml" +
                "\n" + "methods=[GET, PATCH]" +
                "\n" + "sourceType=YAMLSTRING" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=Category API", "error=null" +
                "\n" + "warning=null"));
        l.add(new Case("yamlFile_missingFile", Legacy.YAML,
                () -> file(YF, "pet", RES + "doesNotExist.yml", "crud"),
                "ERROR: Yaml file not found: src/test/resources/doesNotExist.yml", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLFILE" +
                "\n" + "sampleYamlData=null" +
                "\n" + "title=null", "error=Yaml file not found: src/test/resources/doesNotExist.yml" +
                "\n" + "warning=null"));
        l.add(new Case("yamlString_invalidYaml", Legacy.YAML,
                () -> yamlString("pet", "a: [b\n  c: : :\n", "crud"),
                "ERROR: Could not parse input (string): not valid Yaml/JSON.", "outputFile=openapi.yaml" +
                "\n" + "methods=[DELETE, GET, POST, PUT]" +
                "\n" + "sourceType=YAMLSTRING" +
                "\n" + "sampleYamlData=set" +
                "\n" + "title=null", "error=Could not parse input (string): not valid Yaml/JSON." +
                "\n" + "warning=null"));
        l.add(new Case("nullInput", Legacy.YAML, () -> null,
                "ERROR: Input parameter is missing.", "input=null", "error=null" +
                "\n" + "warning=null"));
        return l.stream();
    }

    // ---- test -----------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void pinsBehaviour(Case c) throws IOException {
        // (a) + (b): generate()
        GenerationResult result = new Joaswizard().generate(c.input.get());
        String diagnostics = result.getDiagnostics().stream().map(Diagnostic::toString).collect(Collectors.joining("\n"));

        // (c) + (d): deprecated string call on a fresh input
        InputParameter legacyIn = c.input.get();
        Joaswizard legacyJo = new Joaswizard();
        String legacySpec = callLegacy(legacyJo, c.legacy, legacyIn);
        String mutations = mutations(legacyIn);
        String legacyMessages = "error=" + legacyJo.getErrorMessage() + "\nwarning=" + legacyJo.getWarningMessage();

        Path specFile = GUARD_DIR.resolve(c.name + ".yml");
        if (UPDATE) {
            Files.createDirectories(GUARD_DIR);
            if (result.getSpec() == null) Files.deleteIfExists(specFile);
            else Files.write(specFile, result.getSpec().getBytes(StandardCharsets.UTF_8));
            Path dump = Paths.get("target", "guard-signatures.txt");
            Files.createDirectories(dump.getParent());
            String block = "=== " + c.name + "\n--- d\n" + diagnostics + "\n--- m\n" + mutations + "\n--- l\n" + legacyMessages + "\n";
            Files.write(dump, block.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            return;
        }

        if (result.getSpec() == null) {
            assertFalse(Files.exists(specFile), "expected a spec for " + c.name);
        } else {
            assertTrue(Files.exists(specFile), "missing guard file " + specFile);
            assertArrayEquals(Files.readAllBytes(specFile), result.getSpec().getBytes(StandardCharsets.UTF_8),
                    "spec changed for " + c.name + " (breaking change?)");
        }
        // The deprecated call must produce the same spec as generate() (null stays null).
        assertEquals(result.getSpec(), legacySpec, "deprecated call differs from generate()");
        assertEquals(c.diagnostics, diagnostics, "diagnostics of generate()");
        assertEquals(c.mutations, mutations, "InputParameter mutations");
        assertEquals(c.legacyMessages, legacyMessages, "legacy messages");
    }

    private static String callLegacy(Joaswizard jo, Legacy kind, InputParameter in) {
        switch (kind) {
            case JSON:
                return jo.createFromJSONToString(in);
            case EXCEL_FILE:
                return jo.createFromExcelfileToString(in);
            case EXCEL_STREAM:
                return jo.createFromExcelInputstreamToString(in);
            default:
                return jo.createFromYamlToString(in);
        }
    }

    private static String mutations(InputParameter in) {
        if (in == null) return "input=null";
        String methods = in.getMethodList() == null ? "null"
                : in.getMethodList().stream().map(Enum::name).sorted().collect(Collectors.toList()).toString();
        return "outputFile=" + in.getOutputFile()
                + "\nmethods=" + methods
                + "\nsourceType=" + in.getSourceType()
                + "\nsampleYamlData=" + (in.getSampleYamlData() == null ? "null" : "set")
                + "\ntitle=" + (in.getOasInfo() == null ? "null" : in.getOasInfo().getTitle());
    }
}
