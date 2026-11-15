package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for generate / generateToFile, the diagnostics and the deprecated state.
 */
@SuppressWarnings("deprecation")
class GenerationResultTest {

    private static final String PET = "pet:\n  name: Rex\n  age: 3\n";
    private static final String CAT = "cat:\n  name: Tom\n  color: grey\n  weight: 4.5\n";
    private static final String EXCEL = "src/test/resources/objectimport.xlsx";

    private InputParameter yamlString(String data, String resource) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData(data);
        in.setResource(resource);
        in.setResourceId("name");
        in.setCrud();
        return in;
    }

    private InputParameter file(String file, String resource) {
        InputParameter in = new InputParameter();
        in.setInputFile(file);
        in.setResource(resource);
        in.setResourceId("name");
        in.setCrud();
        return in;
    }

    private InputParameter excelFile() {
        InputParameter in = new InputParameter();
        in.setInputFile(EXCEL);
        in.addMethods("get");
        return in;
    }

    private InputParameter excelStream() throws Exception {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputStream(new FileInputStream(EXCEL));
        in.addMethods("get");
        return in;
    }

    @Test
    void testGenerateYamlString() {
        GenerationResult r = new Joaswizard().generate(yamlString(PET, "pet"));
        assertTrue(r.isOk());
        assertNull(r.getOutputFile());
        assertEquals(new Joaswizard().createFromYamlToString(yamlString(PET, "pet")), r.getSpec());
        assertTrue(r.getSpec().contains("openapi:"));
    }

    @Test
    void testGenerateYamlFile() {
        GenerationResult r = new Joaswizard().generate(file("src/test/resources/Pet.yml", "pet"));
        assertTrue(r.isOk(), r.toString());
        assertEquals(new Joaswizard().createFromYamlToString(file("src/test/resources/Pet.yml", "pet")), r.getSpec());
    }

    @Test
    void testGenerateJsonString() {
        InputParameter in = yamlString("{\"pet\": {\"name\": \"Rex\", \"age\": 3}}", "pet");
        in.setSourceType(InputParameter.Sourcetype.JSONSTRING);
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        InputParameter again = yamlString("{\"pet\": {\"name\": \"Rex\", \"age\": 3}}", "pet");
        again.setSourceType(InputParameter.Sourcetype.JSONSTRING);
        assertEquals(new Joaswizard().createFromJSONToString(again), r.getSpec());
    }

    @Test
    void testGenerateJsonFile() {
        GenerationResult r = new Joaswizard().generate(file("src/test/resources/ContactJSON.json", "contact"));
        assertTrue(r.isOk(), r.toString());
        assertEquals(new Joaswizard().createFromJSONToString(file("src/test/resources/ContactJSON.json", "contact")), r.getSpec());
    }

    @Test
    void testGenerateExcelFile() {
        GenerationResult r = new Joaswizard().generate(excelFile());
        assertTrue(r.isOk(), r.toString());
        assertEquals(new Joaswizard().createFromExcelfileToString(excelFile()), r.getSpec());
    }

    @Test
    void testGenerateExcelInputStream() throws Exception {
        GenerationResult r = new Joaswizard().generate(excelStream());
        assertTrue(r.isOk(), r.toString());
        assertEquals(new Joaswizard().createFromExcelInputstreamToString(excelStream()), r.getSpec());
    }

    @Test
    void testMissingFileIsError() {
        GenerationResult r = new Joaswizard().generate(file("src/test/resources/doesNotExist.yml", "pet"));
        assertFalse(r.isOk());
        assertNull(r.getSpec());
        assertEquals(1, r.getErrors().size());
        assertTrue(r.getErrors().get(0).getMessage().contains("doesNotExist.yml"));
        assertEquals(Diagnostic.Severity.ERROR, r.getErrors().get(0).getSeverity());
    }

    @Test
    void testInvalidYamlIsError() {
        GenerationResult r = new Joaswizard().generate(yamlString("pet: [unclosed\n  : :", "pet"));
        assertFalse(r.isOk());
        assertNull(r.getSpec());
        assertFalse(r.getErrors().isEmpty());
        assertFalse(r.getErrors().get(0).getMessage().isEmpty());
    }

    @Test
    void testNullInputIsError() {
        GenerationResult r = new Joaswizard().generate(null);
        assertFalse(r.isOk());
        assertNull(r.getSpec());
        assertFalse(r.getErrors().isEmpty());
    }

    @Test
    void testAllWarningsKept() {
        GenerationResult r = new Joaswizard().generate(excelFile());
        assertTrue(r.isOk());
        List<Diagnostic> warnings = r.getWarnings();
        assertTrue(warnings.size() > 1, warnings.toString());
        assertTrue(warnings.stream().anyMatch(w -> w.getMessage().contains("Unknown Excel header: 'Position'")));
        assertTrue(warnings.stream().anyMatch(w -> w.getMessage().contains("is not valid, using string")));
        assertEquals(warnings.size(), r.getDiagnostics().size() - r.getErrors().size());
        assertThrows(UnsupportedOperationException.class, () -> r.getDiagnostics().clear());
        /* The deprecated field still only keeps the last one. */
        Joaswizard jo = new Joaswizard();
        jo.createFromExcelfileToString(excelFile());
        assertEquals(warnings.get(warnings.size() - 1).getMessage(), jo.getWarningMessage());
    }

    @Test
    void testNoStateLeakBetweenCalls() {
        Joaswizard jo = new Joaswizard();
        GenerationResult bad = jo.generate(file("src/test/resources/doesNotExist.yml", "pet"));
        assertFalse(bad.isOk());
        GenerationResult good = jo.generate(yamlString(PET, "pet"));
        assertTrue(good.isOk());
        assertTrue(good.getErrors().isEmpty());
        assertTrue(good.getDiagnostics().stream().noneMatch(d -> d.getSeverity() == Diagnostic.Severity.ERROR));
    }

    @Test
    void testDeprecatedMessagesAreResetPerCall() {
        Joaswizard jo = new Joaswizard();
        assertNull(jo.createFromYamlToString(file("src/test/resources/doesNotExist.yml", "pet")));
        assertNotNull(jo.getErrorMessage());
        assertNotNull(jo.createFromYamlToString(yamlString(PET, "pet")));
        assertNull(jo.getErrorMessage());
        assertNull(jo.getWarningMessage());
    }

    @Test
    void testDeprecatedCrudStringReturnsErrorMessage() {
        Joaswizard jo = new Joaswizard();
        String out = jo.createCrudFromYamlToString(file("src/test/resources/doesNotExist.yml", "pet"));
        assertEquals(jo.getErrorMessage(), out);
        assertTrue(out.contains("doesNotExist.yml"));
    }

    @Test
    void testThreadSafety() throws Exception {
        Joaswizard shared = new Joaswizard();
        String expectedPet = new Joaswizard().generate(yamlString(PET, "pet")).getSpec();
        String expectedCat = new Joaswizard().generate(yamlString(CAT, "cat")).getSpec();
        assertNotEquals(expectedPet, expectedCat);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                final int thread = t;
                Callable<Boolean> task = () -> {
                    for (int i = 0; i < 20; i++) {
                        boolean pet = (thread + i) % 2 == 0;
                        GenerationResult r = shared.generate(pet ? yamlString(PET, "pet") : yamlString(CAT, "cat"));
                        if (!r.isOk() || !(pet ? expectedPet : expectedCat).equals(r.getSpec())) return false;
                    }
                    return true;
                };
                futures.add(pool.submit(task));
            }
            for (Future<Boolean> f : futures) assertTrue(f.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void testGenerateToFile(@TempDir Path dir) throws Exception {
        Path out = dir.resolve("sub").resolve("pet.yaml");
        InputParameter in = yamlString(PET, "pet");
        in.setOutputFile(out.toString());
        GenerationResult r = new Joaswizard().generateToFile(in);
        assertTrue(r.isOk(), r.toString());
        assertEquals(out.toString(), r.getOutputFile());
        assertEquals(r.getSpec(), new String(Files.readAllBytes(out), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    void testGenerateToFileWriteFailure(@TempDir Path dir) {
        InputParameter in = yamlString(PET, "pet");
        in.setOutputFile(dir.toString());
        GenerationResult r = new Joaswizard().generateToFile(in);
        assertFalse(r.isOk());
        assertNull(r.getOutputFile());
        assertNotNull(r.getSpec());
        assertEquals(1, r.getErrors().size());
        assertTrue(r.getErrors().get(0).getMessage().startsWith("Could not write file"));
    }

    @Test
    void testGenerateToFileGenerationFailure() {
        GenerationResult r = new Joaswizard().generateToFile(file("src/test/resources/doesNotExist.yml", "pet"));
        assertFalse(r.isOk());
        assertNull(r.getOutputFile());
    }
}
