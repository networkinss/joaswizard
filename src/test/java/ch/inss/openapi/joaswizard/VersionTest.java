package ch.inss.openapi.joaswizard;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void testVersionMatchesPom() throws Exception {
        org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("pom.xml"));
        String pomVersion = null;
        org.w3c.dom.NodeList children = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if ("version".equals(children.item(i).getNodeName())) {
                pomVersion = children.item(i).getTextContent().trim();
                break;
            }
        }
        assertNotNull(pomVersion);
        assertEquals(pomVersion, Constants.version);
    }

    @Test
    void testVersionFormat() {
        assertFalse(Constants.version.contains("${"));
        assertNotEquals("unknown", Constants.version);
        assertTrue(Constants.version.matches("\\d+\\.\\d+\\.\\d+.*"));
    }
}
