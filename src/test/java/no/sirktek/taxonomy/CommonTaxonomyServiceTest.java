package no.sirktek.taxonomy;

import no.sirktek.taxonomy.loader.CommonRdfsTaxonomyLoader;
import no.sirktek.taxonomy.model.CategoryInfo;
import no.sirktek.taxonomy.model.EnumValue;
import no.sirktek.taxonomy.model.PropertyDefinition;
import no.sirktek.taxonomy.model.TaxonomyTree;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void depreciationGroupEnumerationClassIsExposed() {
        CategoryInfo group = taxonomyService.getCategoryByClassName("DepreciationGroup").orElseThrow();
        assertEquals("Tax Depreciation Group", group.englishName());
        assertEquals("Saldogruppe", group.norwegianName());
        assertEquals(0, group.properties().size(),
                "DepreciationGroup is an enumeration class — its instances carry the data, not rdf:Property domains");
    }

    @Test
    void depreciationGroupInstancesCoverSaldogruppeAToJWithStatutoryRates() {
        Model model = loadCommonModel();
        String ns = "http://taxonomy.sirktek.no/common#";
        Property rate = model.createProperty(ns + "depreciationRate");
        Property jurisdiction = model.createProperty(ns + "jurisdiction");
        Property notation = model.createProperty("http://www.w3.org/2004/02/skos/core#notation");
        Resource groupClass = model.createResource(ns + "DepreciationGroup");

        // skatteloven § 14-43 (1) maximum yearly rates, percent
        Map<String, Integer> expected = Map.of(
                "a", 30, "b", 20, "c", 24, "d", 20, "e", 14,
                "f", 12, "g", 5, "h", 4, "i", 2, "j", 10);

        Map<String, Integer> actual = model.listSubjectsWithProperty(RDF.type, groupClass).toList().stream()
                .collect(Collectors.toMap(
                        r -> r.getRequiredProperty(notation).getString(),
                        r -> r.getRequiredProperty(rate).getLiteral().getValue() instanceof BigDecimal bd
                                ? bd.intValue()
                                : r.getRequiredProperty(rate).getInt()));
        assertEquals(expected, actual);

        for (Resource group : model.listSubjectsWithProperty(RDF.type, groupClass).toList()) {
            assertEquals("NO", group.getRequiredProperty(jurisdiction).getString(),
                    group.getURI() + " must be tagged as Norwegian");
            assertTrue(group.getURI().startsWith(ns + "DepreciationGroup"), group.getURI());
            assertTrue(group.listProperties(RDFS.label).toList().stream()
                    .map(st -> st.getLanguage()).collect(Collectors.toSet()).containsAll(Set.of("en", "no")),
                    group.getURI() + " needs en and no labels");
        }
    }

    @Test
    void accountingPropertiesAreDeclaredWithExpectedRangesAndJurisdiction() {
        Model model = loadCommonModel();
        String ns = "http://taxonomy.sirktek.no/common#";
        Property jurisdiction = model.createProperty(ns + "jurisdiction");

        Resource ledgerAccount = model.getResource(ns + "ledgerAccount");
        Resource bookValue = model.getResource(ns + "bookValue");
        Resource taxValue = model.getResource(ns + "taxValue");
        Resource wealthTaxValue = model.getResource(ns + "wealthTaxValue");
        assertNull(model.getProperty(model.getResource(ns + "assetValue"), RDF.type),
                "assetValue was renamed to wealthTaxValue");
        Resource depreciationGroup = model.getResource(ns + "depreciationGroup");

        assertEquals("http://www.w3.org/2001/XMLSchema#string",
                ledgerAccount.getRequiredProperty(RDFS.range).getResource().getURI());
        for (Resource p : List.of(bookValue, taxValue, wealthTaxValue)) {
            assertEquals(ns + "AssetValueEntry",
                    p.getRequiredProperty(RDFS.range).getResource().getURI(),
                    p.getURI() + " reuses the AssetValueEntry (year, value) shape so consumers need no new DTO");
        }
        assertEquals(ns + "DepreciationGroup",
                depreciationGroup.getRequiredProperty(RDFS.range).getResource().getURI());

        assertEquals("NO", wealthTaxValue.getRequiredProperty(jurisdiction).getString(),
                "formuesverdi is a Norwegian wealth-tax figure");
        assertNull(taxValue.getProperty(jurisdiction), "every tax regime has a tax written-down value");
        assertNull(depreciationGroup.getProperty(jurisdiction),
                "depreciationGroup is global; its values are tagged per jurisdiction instead");
        assertNull(model.getResource(ns + "DepreciationGroup").getProperty(jurisdiction),
                "the enumeration class is global; its instances are tagged");
        assertNull(ledgerAccount.getProperty(jurisdiction), "ledgerAccount is jurisdiction-neutral");
        assertNull(bookValue.getProperty(jurisdiction), "bookValue is jurisdiction-neutral");
        assertNull(model.getResource(ns + "AssetValueEntry").getProperty(jurisdiction),
                "the shared (year, value) range marker stays neutral because bookValue uses it too");

        // No domain in commons: domains opt in via schema:domainIncludes in their own ttl.
        Property domainIncludes = model.createProperty("https://schema.org/domainIncludes");
        for (Resource p : List.of(ledgerAccount, bookValue, taxValue, wealthTaxValue, depreciationGroup)) {
            assertNull(p.getProperty(RDFS.domain), p.getURI() + " must not fix a domain in commons");
            assertNull(p.getProperty(domainIncludes), p.getURI() + " must not fix a domain in commons");
        }
    }

    @Test
    void loaderSurfacesJurisdictionOnPropertyDefinition() {
        TaxonomyTree tree = new CommonRdfsTaxonomyLoader()
                .loadTaxonomyFromResource("/taxonomy/test-jurisdiction.ttl");
        CategoryInfo asset = tree.rootCategories().stream()
                .filter(c -> "TestAsset".equals(c.className()))
                .findFirst().orElseThrow();
        Map<String, PropertyDefinition> props = asset.properties().stream()
                .collect(Collectors.toMap(PropertyDefinition::name, p -> p));

        assertEquals("NO", props.get("norwegianOnly").jurisdiction());
        assertNull(props.get("everywhere").jurisdiction());
        assertTrue(props.get("everywhere").enumValues().isEmpty(), "xsd:string has no enum values");
    }

    @Test
    void loaderExposesEnumerationValuesWithPerValueJurisdiction() {
        TaxonomyTree tree = new CommonRdfsTaxonomyLoader()
                .loadTaxonomyFromResource("/taxonomy/test-jurisdiction.ttl");
        PropertyDefinition taxClass = tree.rootCategories().stream()
                .filter(c -> "TestAsset".equals(c.className()))
                .flatMap(c -> c.properties().stream())
                .filter(p -> "taxClass".equals(p.name()))
                .findFirst().orElseThrow();

        assertNull(taxClass.jurisdiction(), "the property itself is global");
        List<EnumValue> values = taxClass.enumValues();
        assertEquals(List.of("TestClassGbMain", "TestClassGlobal", "TestClassNoA", "TestClassNoB"),
                values.stream().map(EnumValue::name).toList(),
                "sorted by notation (absent first), then name");

        EnumValue noB = values.stream().filter(v -> "b".equals(v.notation())).findFirst().orElseThrow();
        assertEquals("NO", noB.jurisdiction());
        assertEquals("Norwegian B", noB.englishLabel());
        assertEquals("Norsk B", noB.norwegianLabel());
        assertEquals("second", noB.description());
        assertEquals(Map.of("depreciationRate", "20"), noB.attributes());
        assertEquals("http://taxonomy.sirktek.no/common#TestClassNoB", noB.uri());

        assertEquals("GB", values.stream().filter(v -> "TestClassGbMain".equals(v.name()))
                .findFirst().orElseThrow().jurisdiction());
        assertNull(values.stream().filter(v -> "TestClassGlobal".equals(v.name()))
                .findFirst().orElseThrow().jurisdiction());

        // What a consumer does for a Norwegian organisation:
        List<String> forNorway = values.stream()
                .filter(v -> v.jurisdiction() == null || "NO".equals(v.jurisdiction()))
                .map(EnumValue::name).toList();
        assertEquals(List.of("TestClassGlobal", "TestClassNoA", "TestClassNoB"), forNorway);
    }

    private Model loadCommonModel() {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = getClass().getResourceAsStream("/taxonomy/common-base.ttl")) {
            assertNotNull(in, "common-base.ttl missing from classpath");
            model.read(in, null, "TURTLE");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        return model;
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
