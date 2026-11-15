package ch.inss.openapi.joaswizard;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generated specs stay valid YAML and values cannot inject keys.
 */
@SuppressWarnings({"unchecked", "deprecation"})
class YamlSafeOutputTest {

    private static Map<String, Object> parse(String spec) {
        return (Map<String, Object>) new Yaml(new SafeConstructor(new LoaderOptions())).load(spec);
    }

    @Test
    void yamlSampleValuesRoundTrip() {
        String yaml = "pet:\n"
                + "  name: 'Rex: the #1 dog'\n"
                + "  note: \"line1\\nline2 with 'quotes' and \\\"dq\\\"\"\n"
                + "  evil: 'x\\n    injected: true'\n"
                + "  age: 3\n";
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData(yaml);
        in.setResource("pet");
        in.setResourceId("name");
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        Map<String, Object> props = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) parse(r.getSpec()).get("components")).get("schemas")).get("Pet");
        Map<String, Object> p = (Map<String, Object>) props.get("properties");
        assertEquals(Arrays.asList("name", "note", "evil", "age"), List.copyOf(p.keySet()));
        assertEquals("Rex: the #1 dog", ((Map<String, Object>) p.get("name")).get("example"));
        assertEquals("line1\nline2 with 'quotes' and \"dq\"", ((Map<String, Object>) p.get("note")).get("example"));
        assertEquals("x\\n    injected: true", ((Map<String, Object>) p.get("evil")).get("example"));
        assertEquals(3, ((Map<String, Object>) p.get("age")).get("example"));
    }

    @Test
    void excelDescriptionPatternEnumRoundTrip(@TempDir Path dir) throws Exception {
        String file = dir.resolve("t.xlsx").toString();
        String desc = "It's \"quoted\": a #b\nsecond line";
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pet");
            String[] header = {"Name", "OasType", "OasDescription", "OasPattern", "OasEnum", "OasExample"};
            String[][] rows = {
                    {"name", "string", desc, "^[a-z]{3}: #$", "a: b,12,x y", "Rex: #1"},
                    {"id", "integer", "plain", "", "", "5"}};
            Row h = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) h.createCell(i).setCellValue(header[i]);
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int i = 0; i < header.length; i++) row.createCell(i).setCellValue(rows[r][i]);
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
        }
        InputParameter in = new InputParameter();
        in.setInputFile(file);
        in.addMethods("get");
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        Map<String, Object> pet = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) parse(r.getSpec()).get("components")).get("schemas")).get("Pet");
        Map<String, Object> props = (Map<String, Object>) pet.get("properties");
        assertEquals(Arrays.asList("name", "id"), List.copyOf(props.keySet()));
        Map<String, Object> name = (Map<String, Object>) props.get("name");
        assertEquals(desc, name.get("description"));
        assertEquals("^[a-z]{3}: #$", name.get("pattern"));
        assertEquals("Rex: #1", name.get("example"));
        assertEquals(Arrays.asList("a: b", "12", "x y"), name.get("enum"));
        assertEquals(5, ((Map<String, Object>) props.get("id")).get("example"));
    }

    @Test
    void infoValuesAreQuoted() {
        OasInfo info = new OasInfo();
        info.setTitle("Pets: the #1 API");
        info.setDescription("Line1\nLine2: x");
        InputParameter in = new InputParameter();
        in.setSourceType(InputParameter.Sourcetype.YAMLSTRING);
        in.setSampleYamlData("pet:\n  name: Rex\n");
        in.setResource("pet");
        in.setResourceId("name");
        in.setOasInfo(info);
        GenerationResult r = new Joaswizard().generate(in);
        assertTrue(r.isOk(), r.toString());
        Map<String, Object> i = (Map<String, Object>) parse(r.getSpec()).get("info");
        assertEquals("Pets: the #1 API", i.get("title"));
        assertEquals("Line1\nLine2: x", i.get("description"));
        assertEquals(Arrays.asList("version", "title", "description", "contact", "license"), List.copyOf(i.keySet()));
    }
}
