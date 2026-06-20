package no.sirktek.taxonomy;

import no.sirktek.taxonomy.model.CategoryInfo;
import no.sirktek.taxonomy.model.TaxonomyTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonTaxonomyServiceTest {

    private CommonTaxonomyService taxonomyService;

    @BeforeEach
    void setUp() {
        taxonomyService = new CommonTaxonomyService();
    }

    @Test
    void shouldLoadBaseTaxonomy() {
        TaxonomyTree taxonomy = taxonomyService.loadBaseTaxonomy();

        assertNotNull(taxonomy);
        assertNotNull(taxonomy.rootCategories());
        assertTrue(taxonomy.rootCategories().size() >= 6,
                "Expected at least 6 root classes (Manufacturer, Model, Resource, EmissionEntry, ConsistsOfEntry, EnergySourceEntry)");
    }

    @Test
    void shouldExposeExpectedCrossCuttingClasses() {
        Set<String> classNames = taxonomyService.loadBaseTaxonomy().rootCategories().stream()
                .map(CategoryInfo::className)
                .collect(Collectors.toSet());

        assertTrue(classNames.containsAll(List.of(
                "Manufacturer",
                "Model",
                "Resource",
                "EmissionEntry",
                "ConsistsOfEntry",
                "EnergySourceEntry")),
                "Missing one or more cross-cutting classes; got " + classNames);
    }

    @Test
    void allClassUrisAreInCommonNamespace() {
        for (CategoryInfo cat : taxonomyService.loadBaseTaxonomy().rootCategories()) {
            assertTrue(cat.uri() != null && cat.uri().startsWith("http://taxonomy.sirktek.no/common#"),
                    "Unexpected namespace for " + cat.className() + ": " + cat.uri());
        }
    }

    @Test
    void manufacturerHasExpectedProperties() {
        Optional<CategoryInfo> manufacturer = taxonomyService.getCategoryByClassName("Manufacturer");

        assertTrue(manufacturer.isPresent());
        Set<String> propNames = manufacturer.get().properties().stream()
                .map(p -> p.name())
                .collect(Collectors.toSet());
        assertTrue(propNames.containsAll(List.of("address", "organizationNumber", "homepage")),
                "Manufacturer should declare address/organizationNumber/homepage; got " + propNames);
    }

    @Test
    void modelHasExpectedProperties() {
        Optional<CategoryInfo> model = taxonomyService.getCategoryByClassName("Model");

        assertTrue(model.isPresent());
        Set<String> propNames = model.get().properties().stream()
                .map(p -> p.name())
                .collect(Collectors.toSet());
        assertTrue(propNames.containsAll(List.of("manufacturer", "epd", "productPage")),
                "Model should declare manufacturer/epd/productPage; got " + propNames);
    }

    @Test
    void resourceHasExpectedProperties() {
        Optional<CategoryInfo> resource = taxonomyService.getCategoryByClassName("Resource");

        assertTrue(resource.isPresent());
        Set<String> propNames = resource.get().properties().stream()
                .map(p -> p.name())
                .collect(Collectors.toSet());
        assertTrue(propNames.containsAll(List.of("unit", "resourceType")),
                "Resource should declare unit/resourceType; got " + propNames);
    }

    @Test
    void rangeMarkerClassesHaveNoProperties() {
        for (String marker : List.of("EmissionEntry", "ConsistsOfEntry", "EnergySourceEntry", "AllocationEntry", "AssetValueEntry")) {
            CategoryInfo cat = taxonomyService.getCategoryByClassName(marker).orElseThrow();
            assertEquals(0, cat.properties().size(),
                    marker + " is a property-range marker — should declare no rdf:Property of its own");
        }
    }
}
