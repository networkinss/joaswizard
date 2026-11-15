package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.InputParameter;
import ch.inss.openapi.joaswizard.Joaswizard;
import ch.inss.openapi.joaswizard.OasInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class YamlSourceTest {

    private static List<String> trimmedLines(String text) {
        return text.lines().map(String::trim).collect(Collectors.toList());
    }

    @Test
    @SuppressWarnings("deprecation")
    void getMethodsOfPetOnlyCreatesPathSection() {
        InputParameter in = new InputParameter();
        in.setResourceId("name");
        in.setResource("pet");
        in.addMethods("GET");
        in.setInputFile("src/test/resources/Pet.yml");
        in.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        String paths = new Joaswizard().createMethodsFromList(List.of(in));
        List<String> wanted = List.of("/pet/{name}:", "description: List of pets.", "$ref: '#/components/schemas/Pet'");
        assertEquals(4, trimmedLines(paths).stream().filter(wanted::contains).count());
    }

    @Test
    @SuppressWarnings("deprecation")
    void wrongInputIsReported() {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData("price"); // not Yaml objects
        in.setResource("object");
        Joaswizard jo = new Joaswizard();
        assertNotNull(jo.createCrudFromYamlToString(in));
        assertEquals("Could not parse input (string): not valid Yaml/JSON.", jo.getErrorMessage());
    }

    @Test
    void customInfoIsUsed() {
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlBase64("Zmlyc3RuYW1lOiBNYXgKbmFtZTogTXVzdGVybWFubgpwaG9uZTogMTIzNDU2Nzg5CmVtYWlsOiAibWF4QGV4YW1wbGUuY29tIg==");
        in.setResourceId("name");
        in.setResource("contact");
        in.setCrud();
        OasInfo info = new OasInfo();
        info.setContactName("Jane Doe");
        info.setContactEmail("jane.doe@example.com");
        info.setDescription("Generated for JUnit test.");
        info.setTitle("Title coming from JUnit test.");
        in.setOasInfo(info);
        List<String> lines = trimmedLines(new Joaswizard().generate(in).getSpec());
        assertTrue(lines.containsAll(List.of("description: Generated for JUnit test.", "email: jane.doe@example.com",
                "name: Jane Doe", "title: Title coming from JUnit test.")));
    }
}
