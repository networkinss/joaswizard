package ch.inss.joaswizardsamples;

import ch.inss.openapi.joaswizard.InputParameter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleTest {

    final static String sep = System.getProperty("file.separator");

    @Test
    public void testCreateOpenApiFromYaml()throws Exception {
        Sample sample = new Sample();
        sample.createOpenApiFromYaml();
        String output = "openapi_fromyaml.yaml";

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getPetList");
        list.add("description: Deletes pet by id.");
        list.add("example: Underdog");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    public void testCreateOpenApiFromArguments() throws Exception{
        Sample sample = new Sample();
        String output = "openapi_fromparameters.yaml";
        sample.createOpenApiFromArguments(new String[]{"./pet.yml", output, "pet", "name","yamlfile","crud,patch"});

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: addPet");
        list.add("description: Patches pet by name,");
        list.add("example: Underdog");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    public void testCreateOpenApiFromExcel() throws Exception {
        Sample sample = new Sample();
        sample.createOpenApiFromExcel("./objectimport.xlsx");
        String output = "openapi_fromexcel.yaml";

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getInvoiceList");
        list.add("description: List of customers.");
        list.add("example: U3dhZ2dlciByb2Nrcw==");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    public void testCreateOpenApiFromDBFieldsExcel() throws Exception {
        Sample sample = new Sample();
        sample.createOpenApiFromExcel("objectimport_onlyDBfieldformat.xlsx");
        String output = "openapi_fromexcel.yaml";

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getInvoiceList");
        list.add("summary: List of customers");
        list.add("example: U3dhZ2dlciByb2Nrcw==");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    public void testCreateOpenApiFromCustomMappingExcel() throws Exception {
        Sample sample = new Sample();
        String dir = "mysql" + sep;
        String output = "openapi_fromMySQLExcel.yaml";

        sample.createOpenApiFromExcel(dir + "mySQLObjectimport.xlsx",dir + "mysqlMapping.json",output);

        File file1 = new File(output);
        assertTrue(file1.isFile());

        final List<String> list = new ArrayList<>();
        list.add("operationId: getInvoiceList");
        list.add("description: List of customers.");
        list.add("example: U3dhZ2dlciByb2Nrcw==");
        try (Stream<String> lines = Files.lines(Paths.get(output))) {
            Stream<String> f = lines.filter(l -> list.contains(l.trim()));
            long x = f.count();
            assertEquals(3, x);
        }

        file1.delete();
        assertTrue(file1.isFile() == false);
    }

    @Test
    void testExcelFromInputStream() throws Exception {
        Sample sample = new Sample();
        FileInputStream input = new FileInputStream("objectimport.xlsx");

        String fromExcelInputstreamToString = sample.createOpenApiFromExcelInputStream(input);

        String[] outArr = fromExcelInputstreamToString.split("\n");
        final List<String> allLines = Arrays.asList(outArr).stream().map(String::trim).collect(Collectors.toList());
        final List<String> checkList = new ArrayList<>();
        checkList.add("description: OpenAPI specification generated by Joaswizard (https://github.com/networkinss/joaswizard).");
        checkList.add("email: john.doe@example.com");
        checkList.add("operationId: findInvoiceByid");
        checkList.add("operationId: getInvoiceList");
        boolean ok = false;
        try (Stream<String> lines = checkList.stream()) {
            ok = lines.allMatch(l -> allLines.contains(l));
        }
        if (ok == false) System.out.println(fromExcelInputstreamToString);
        assertTrue(ok);
    }


}