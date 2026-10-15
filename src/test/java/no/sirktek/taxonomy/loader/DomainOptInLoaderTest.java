package no.sirktek.taxonomy.loader;

import no.sirktek.taxonomy.model.CategoryInfo;
import no.sirktek.taxonomy.model.CommonPropertyDefinition;
import no.sirktek.taxonomy.model.CommonPropertyDefinition.PropertyType;
import no.sirktek.taxonomy.model.EnumValue;
import no.sirktek.taxonomy.model.PropertyDefinition;
import no.sirktek.taxonomy.model.TaxonomyTree;
import no.sirktek.taxonomy.loader.RdfsTaxonomyLoader.TaxonomyLoadException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    void optedInCommonsPropertyAttachesNextToOwnPropertiesAndUnoptedStaysAbsent() {
        assertEquals(Set.of("floors", "depreciationGroup", "valuations", "marketValue", "weight"), props.keySet());
        PropertyDefinition dg = props.get("depreciationGroup");
        assertEquals("http://taxonomy.sirktek.no/common#depreciationGroup", dg.uri());
        assertNull(dg.domainClass());
    }

    @Test
    void missingResourceReportsTheResourceNotAWrapper() {
        TaxonomyLoadException e = assertThrows(TaxonomyLoadException.class,
                () -> new TestDomainLoader().loadTaxonomyFromResource("/taxonomy/does-not-exist.ttl"));
        assertTrue(e.getMessage().contains("Could not find resource"));
        assertTrue(e.getMessage().contains("/taxonomy/does-not-exist.ttl"));
        assertFalse(e.getMessage().startsWith("Failed to load"));
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
        assertEquals("accounting", props.get("valuations").privateSet());
        assertEquals("commercial", props.get("marketValue").privateSet());
        assertNull(props.get("floors").privateSet());
    }

    @Test
    void commercialPropertyKeepsCommonsUriAndValuationsMapsToAssetValuations() {
        assertEquals("http://taxonomy.sirktek.no/common#marketValue", props.get("marketValue").uri());
        assertEquals("http://taxonomy.sirktek.no/common#weight", props.get("weight").uri());
        assertEquals(PropertyType.ASSET_VALUATIONS,
                CommonPropertyDefinition.getPropertyType(props.get("valuations")));
    }

    private static Stream<String> allUris(List<CategoryInfo> cats) {
        return cats.stream().flatMap(c -> Stream.concat(
                Stream.of(c.uri()), allUris(c.children())));
    }
}
