package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static ch.inss.openapi.joaswizard.Constants.nextLine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression set: the generated spec must equal the testReference*.yml file byte for byte.
 * A diff means a breaking output change. If the change is intended, update the reference file deliberately.
 * Each case is independent (fresh Joaswizard and InputParameter). Cases using deprecated file methods or Main
 * write relative to the current folder, below target/test-output/.
 */
class GoldenFileTest {

    private static final String RES = "src/test/resources/";
    private static final String OUT = "target/test-output/";

    @FunctionalInterface
    private interface Producer {
        String produce(Path tmp) throws Exception;
    }

    private static final class Case {
        final String name;
        final String reference;
        final Producer producer;

        Case(String name, String reference, Producer producer) {
            this.name = name;
            this.reference = reference;
            this.producer = producer;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static InputParameter yamlFile(String file, String methods) {
        InputParameter in = new InputParameter();
        in.setResourceId("name");
        in.setResource("pet");
        in.setInputFile(RES + file);
        in.setSourceType(InputParameter.Sourcetype.YAMLFILE.toString());
        if (methods != null) in.addMethods(methods);
        return in;
    }

    private static InputParameter excel(String file, String methods) {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.EXCEL);
        in.setInputFile(RES + file);
        in.addMethods(methods);
        return in;
    }

    private static String spec(InputParameter in) {
        GenerationResult r = new Joaswizard().generate(in);
        assertNotNull(r.getSpec(), r.toString());
        return r.getSpec();
    }

    /** Schema section only, one block per object, via the deprecated schema methods. */
    @SuppressWarnings("deprecation")
    private static String schemas(InputParameter in) throws Exception {
        Joaswizard jo = new Joaswizard();
        in.setSampleYamlData(Util.readFromFile(in.getInputFile()));
        StringBuilder sb = new StringBuilder();
        for (InputParameter p : jo.createMustacheDataFromYaml(in)) {
            sb.append(jo.createSchemaObjects(p)).append(nextLine);
        }
        return sb.toString();
    }

    private static String read(String file) throws Exception {
        return Files.readString(Paths.get(file), StandardCharsets.UTF_8);
    }

    static Stream<Case> cases() {
        return List.of(
                // Main (Yaml file, CRUD) writing to a relative file.
                new Case("contactViaMain", "testReferenceContact.yml", t -> {
                    Main.createOpenApiFromYamlfile(new String[]{RES + "Contact.yml", OUT + "contactViaMain.yml", "contact"});
                    return read(OUT + "contactViaMain.yml");
                }),
                new Case("schemaYamlObjects", "testReferenceSchemaPetObject.yml", t -> {
                    InputParameter in = new InputParameter();
                    in.setResourceId("name");
                    in.setInputFile(RES + "YamlObjects.yml");
                    return schemas(in);
                }),
                new Case("schemaPet", "testReferenceSchemaPet.yml", t -> {
                    InputParameter in = new InputParameter();
                    in.setResourceId("name");
                    in.setResource("PET");
                    in.setInputFile(RES + "Pet.yml");
                    return schemas(in);
                }),
                // Deprecated file method writing to a relative file.
                new Case("minimalStringDeprecatedFile", "testReferenceMinimalString.yml", t -> {
                    InputParameter in = new InputParameter();
                    in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
                    in.setSampleYamlData("price: 12.05");
                    in.setOutputFile(OUT + "minimalString.yml");
                    in.setResource("object");
                    assertEquals(true, new Joaswizard().createCrudFromYamlToFile(in));
                    return read(OUT + "minimalString.yml");
                }),
                // generateToFile with an absolute file.
                new Case("base64StringToFile", "testReferenceString.yml", t -> {
                    InputParameter in = new InputParameter();
                    in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
                    in.setSampleYamlBase64("Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==");
                    in.setResourceId("name");
                    in.setResource("contact");
                    in.setCrud();
                    in.setOutputFile(t.resolve("string.yml").toString());
                    GenerationResult r = new Joaswizard().generateToFile(in);
                    assertNotNull(r.getOutputFile(), r.toString());
                    return read(r.getOutputFile());
                }),
                new Case("excelGet", "testReferenceGetMethodList.yml", t -> spec(excel("objectimport.xlsx", "get"))),
                new Case("excelDbFieldsGet", "testReferenceGetMethodList.yml", t -> spec(excel("objectimport_onlyDBfieldformat.xlsx", "get"))),
                new Case("excelMySQLCustomMapping", "testReferenceMySQLDBFieldsExcelsheet.yml", t -> {
                    InputParameter in = excel("mySQLObjectimport.xlsx", "post,patch,delete");
                    in.setMappingFile(RES + "mysqlMapping.json");
                    in.setPrefixMatch(true);
                    return spec(in);
                }),
                // Main (Excel, delete,post,patch) writing to a relative file.
                new Case("excelViaMain", "testReferenceMySQLDBFieldsExcelsheet.yml", t -> {
                    Main.main(new String[]{RES + "objectimport.xlsx", OUT + "excelViaMain.yaml", "pet", "id", "excel", "delete,post,patch"});
                    return read(OUT + "excelViaMain.yaml");
                }),
                new Case("yamlSingleObjectPutGet", "testReferenceSingleYamlObject.yml", t -> spec(yamlFile("Pet.yml", "put,get"))),
                new Case("yamlCrudSingleObject", "testReferenceCrudSingleYamlObject.yml", t -> {
                    InputParameter in = yamlFile("Pet.yml", null);
                    in.setCrud();
                    return spec(in);
                }),
                new Case("yamlCrudMultipleObjects", "testReferenceCrudMultipleYamlObject.yml", t -> {
                    InputParameter in = yamlFile("YamlObjects.yml", null);
                    in.setCrud();
                    return spec(in);
                }),
                new Case("yamlCrudMaxOneObject", "testReferenceCrudMaxOneObject.yml", t -> {
                    InputParameter in = yamlFile("YamlObjects.yml", null);
                    in.setCrud();
                    in.setMaxobjects(1);
                    return spec(in);
                })
        ).stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void matchesReference(Case c, @TempDir Path tmp) throws Exception {
        String expected = read(RES + c.reference);
        assertEquals(expected, c.producer.produce(tmp),
                "There is a breaking change, output is not equal to " + c.reference);
    }
}
