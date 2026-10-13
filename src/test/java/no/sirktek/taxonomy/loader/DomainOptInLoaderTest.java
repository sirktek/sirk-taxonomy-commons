package no.sirktek.taxonomy.loader;

import no.sirktek.taxonomy.model.CategoryInfo;
import no.sirktek.taxonomy.model.EnumValue;
import no.sirktek.taxonomy.model.PropertyDefinition;
import no.sirktek.taxonomy.model.TaxonomyTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DomainOptInLoaderTest {

    private static TaxonomyTree tree;
    private static Map<String, PropertyDefinition> props;

    @BeforeAll
    static void load() {
        tree = new TestDomainLoader().loadBaseTaxonomy();
        CategoryInfo building = tree.rootCategories().stream()
                .filter(c -> c.className().equals("TestBuilding"))
                .findFirst().orElseThrow();
        props = building.properties().stream()
                .collect(Collectors.toMap(PropertyDefinition::name, p -> p));
    }

    @Test
    void optedInCommonsPropertyAttachesNextToOwnProperties() {
        assertTrue(props.containsKey("floors"));
        PropertyDefinition dg = props.get("depreciationGroup");
        assertNotNull(dg);
        assertEquals("http://taxonomy.sirktek.no/common#depreciationGroup", dg.uri());
        assertNull(dg.domainClass());
    }

    @Test
    void commonsPropertyNotOptedInStaysAbsent() {
        assertFalse(props.containsKey("ledgerAccount"));
        assertEquals(2, props.size());
    }

    @Test
    void commonsClassesDoNotLeakIntoTree() {
        assertFalse(tree.rootCategories().isEmpty());
        assertTrue(allUris(tree.rootCategories())
                .allMatch(u -> u.startsWith("http://taxonomy.sirktek.no/testdomain#")));
    }

    @Test
    void commonsEnumerationValuesReachDomainClass() {
        List<EnumValue> values = props.get("depreciationGroup").enumValues();
        assertEquals(10, values.size());
        assertEquals("a", values.get(0).notation());
        assertEquals("j", values.get(9).notation());
        assertTrue(values.stream().allMatch(v -> "NO".equals(v.jurisdiction())));
        EnumValue h = values.stream().filter(v -> v.name().equals("DepreciationGroupH")).findFirst().orElseThrow();
        assertEquals("4", h.attributes().get("depreciationRate"));
    }

    @Test
    void privateSetIsReadFromAnnotation() {
        assertEquals("accounting", props.get("depreciationGroup").privateSet());
        assertNull(props.get("floors").privateSet());
    }

    private static java.util.stream.Stream<String> allUris(List<CategoryInfo> cats) {
        return cats.stream().flatMap(c -> java.util.stream.Stream.concat(
                java.util.stream.Stream.of(c.uri()), allUris(c.children())));
    }
}
