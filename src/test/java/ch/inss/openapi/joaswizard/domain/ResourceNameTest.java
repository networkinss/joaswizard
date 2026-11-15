package ch.inss.openapi.joaswizard.domain;

import ch.inss.openapi.joaswizard.InputParameter;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNameTest {

    private static final String[] NAMES = {"pet", "Pet", "PET", "pEt", "category", "invoice", "TITLE", "Iris", "a"};

    private static void assertSameAsInputParameter(String name) {
        InputParameter in = new InputParameter();
        in.setResource(name);
        ResourceName rn = new ResourceName(name);
        assertEquals(in.getCapResource(), rn.cap(), name);
        assertEquals(in.getCapResources(), rn.capPlural(), name);
        assertEquals(in.getLowerResource(), rn.lower(), name);
        assertEquals(in.getLowerResources(), rn.lowerPlural(), name);
        assertEquals(in.getResources(), rn.plural(), name);
    }

    @Test
    void matchesInputParameterGetters() {
        for (String n : NAMES) assertSameAsInputParameter(n);
    }

    @Test
    void matchesInputParameterInTurkishLocale() {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            for (String n : NAMES) assertSameAsInputParameter(n);
            ResourceName title = new ResourceName("TITLE");
            // default locale lower case turns I into dotless i, ROOT does not.
            assertEquals("title", title.lower());
            assertEquals("tıtles", title.lowerPlural());
            assertEquals("Title", title.cap());
        } finally {
            Locale.setDefault(old);
        }
    }

    @Test
    void knownValues() {
        ResourceName pet = new ResourceName("PET");
        assertEquals("Pet", pet.cap());
        assertEquals("Pets", pet.capPlural());
        assertEquals("pet", pet.lower());
        assertEquals("pets", pet.lowerPlural());
        assertEquals("PETS", pet.plural());
    }

    @Test
    void pluralRules() {
        String[][] cases = {{"category", "categories"}, {"day", "days"}, {"boy", "boys"}, {"address", "addresses"},
                {"box", "boxes"}, {"quiz", "quizes"}, {"church", "churches"}, {"dish", "dishes"}, {"pet", "pets"},
                {"y", "ys"}, {"Category", "Categories"}, {"CATEGORY", "CATEGORIES"}, {"Box", "Boxes"}, {"PET", "PETS"}, {"BOX", "BOXES"}, {"A1", "A1S"}, {"123", "123s"}};
        for (String[] c : cases) {
            assertEquals(c[1], new ResourceName(c[0]).plural(), c[0]);
        }
        ResourceName cat = new ResourceName("CATEGORY");
        assertEquals("Categories", cat.capPlural());
        assertEquals("categories", cat.lowerPlural());
        assertEquals("Addresses", new ResourceName("address").capPlural());
        assertEquals("boxes", new ResourceName("BOX").lowerPlural());
        assertEquals("days", new ResourceName("Day").lowerPlural());
    }

    @Test
    void nullNameRejected() {
        assertThrows(NullPointerException.class, () -> new ResourceName(null));
    }

    @Test
    void httpMethodCrudAndUnreadable() {
        assertEquals(Set.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE), HttpMethod.crud());
        assertThrows(UnsupportedOperationException.class, () -> HttpMethod.crud().add(HttpMethod.PATCH));
        ResourceDefinition u = ResourceDefinition.unreadable(new ResourceName("x"));
        assertNull(u.properties());
        assertTrue(u.isUnreadable());
    }

    @Test
    void apiInfoWithTitle() {
        ApiInfo a = new ApiInfo("3.0.2", "1.0.0", null, "d", "s", "sd", "c", "lu", "ln", "e", "u");
        ApiInfo b = a.withTitle("Pet API");
        assertNull(a.title());
        assertEquals("Pet API", b.title());
        assertEquals(a.contactUrl(), b.contactUrl());
    }
}
