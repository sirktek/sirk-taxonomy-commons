package no.sirktek.taxonomy.loader;

import lombok.extern.slf4j.Slf4j;
import no.sirktek.taxonomy.model.CategoryInfo;
import no.sirktek.taxonomy.model.EnumValue;
import no.sirktek.taxonomy.model.PropertyDefinition;
import no.sirktek.taxonomy.model.TaxonomyTree;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base loader for RDF-S taxonomies using Apache Jena
 * Subclasses should override getNamespace() and getResourcePath()
 */
@Slf4j
public abstract class RdfsTaxonomyLoader {

    /**
     * schema:domainIncludes — used instead of rdfs:domain for union semantics
     * (multiple rdfs:domain triples imply intersection; schema:domainIncludes implies union)
     */
    private static final Property SCHEMA_DOMAIN_INCLUDES =
            ResourceFactory.createProperty("https://schema.org/domainIncludes");

    /**
     * skos:altLabel — alternative human-readable label for a class. Used to map legacy
     * category strings whose phrasing diverged from the canonical rdfs:label.
     */
    private static final Property SKOS_ALT_LABEL =
            ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#altLabel");

    /**
     * common:multiValued — boolean annotation marking a property as holding
     * multiple values (e.g. a multi-category reference). Absent ⇒ single-valued.
     */
    private static final Property COMMON_MULTI_VALUED =
            ResourceFactory.createProperty("http://taxonomy.sirktek.no/common#multiValued");

    /**
     * common:jurisdiction — ISO 3166-1 alpha-2 annotation marking a property or an
     * enumeration value as applicable only under one country's law or accounting
     * rules (e.g. "NO"). Absent ⇒ applies everywhere.
     */
    private static final Property COMMON_JURISDICTION =
            ResourceFactory.createProperty("http://taxonomy.sirktek.no/common#jurisdiction");

    /**
     * skos:notation — short code of an enumeration value (e.g. the saldogruppe letter).
     */
    private static final Property SKOS_NOTATION =
            ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#notation");

    /**
     * Default constructor
     */
    public RdfsTaxonomyLoader() {
        // Default constructor
    }

    /**
     * Get the namespace URI for this taxonomy
     * @return the namespace URI (e.g., "http://taxonomy.sirktek.no/furniture#")
     */
    protected abstract String getNamespace();

    /**
     * Get the resource path for the base taxonomy file
     * @return the resource path (e.g., "/taxonomy/furniture-base.ttl")
     */
    protected abstract String getResourcePath();

    /**
     * Load the base taxonomy from the Turtle file
     * @return the loaded taxonomy tree
     */
    public TaxonomyTree loadBaseTaxonomy() {
        return loadTaxonomyFromResource(getResourcePath());
    }

    /**
     * Load taxonomy from a specific resource file
     * @param resourcePath path to the RDF-S resource file
     * @return the loaded taxonomy tree
     */
    public TaxonomyTree loadTaxonomyFromResource(String resourcePath) {
        log.debug("Loading taxonomy from resource: {}", resourcePath);

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new TaxonomyLoadException("Could not find resource: " + resourcePath);
            }

            // Create Jena model and read Turtle data
            Model model = ModelFactory.createDefaultModel();
            model.read(inputStream, null, "TURTLE");

            return buildTaxonomyTree(model);

        } catch (Exception e) {
            throw new TaxonomyLoadException("Failed to load taxonomy from " + resourcePath, e);
        }
    }

    /**
     * Build the taxonomy tree from the RDF model
     */
    private TaxonomyTree buildTaxonomyTree(Model model) {
        log.debug("Building taxonomy tree from RDF model");

        String namespace = getNamespace();

        // Get all classes
        Map<String, CategoryInfo> allCategories = new HashMap<>();
        Map<String, List<String>> parentChildMap = new HashMap<>();

        // First pass: create all category objects
        ResIterator classIterator = model.listSubjectsWithProperty(RDF.type, RDFS.Class);
        while (classIterator.hasNext()) {
            Resource classResource = classIterator.nextResource();

            if (classResource.getURI() != null && classResource.getURI().startsWith(namespace)) {
                CategoryInfo categoryInfo = buildCategoryInfo(classResource, model, namespace);
                allCategories.put(categoryInfo.className(), categoryInfo);

                // Track parent-child relationships
                if (categoryInfo.parentClassName() != null) {
                    parentChildMap.computeIfAbsent(categoryInfo.parentClassName(), k -> new ArrayList<>())
                                  .add(categoryInfo.className());
                }
            }
        }

        // Build the hierarchy recursively
        Map<String, CategoryInfo> categoriesWithChildren = buildHierarchy(allCategories, parentChildMap);

        // Find root categories (those with no parent)
        List<CategoryInfo> rootCategories = categoriesWithChildren.values().stream()
                .filter(CategoryInfo::isRoot)
                .sorted(Comparator.comparing(CategoryInfo::englishName))
                .collect(Collectors.toList());

        log.info("Loaded taxonomy with {} total categories, {} root categories",
                allCategories.size(), rootCategories.size());

        return TaxonomyTree.builder()
                .rootCategories(rootCategories)
                .build();
    }

    /**
     * Build the category hierarchy recursively
     */
    private Map<String, CategoryInfo> buildHierarchy(Map<String, CategoryInfo> allCategories,
                                                     Map<String, List<String>> parentChildMap) {
        Map<String, CategoryInfo> categoriesWithChildren = new HashMap<>();

        // First, create all categories without children
        for (CategoryInfo category : allCategories.values()) {
            categoriesWithChildren.put(category.className(), category);
        }

        // Then, recursively build children for each category
        for (CategoryInfo category : allCategories.values()) {
            CategoryInfo categoryWithChildren = buildCategoryWithChildren(category, allCategories, parentChildMap);
            categoriesWithChildren.put(category.className(), categoryWithChildren);
        }

        return categoriesWithChildren;
    }

    /**
     * Build a category with all its recursive children
     */
    private CategoryInfo buildCategoryWithChildren(CategoryInfo category,
                                                   Map<String, CategoryInfo> allCategories,
                                                   Map<String, List<String>> parentChildMap) {
        List<String> childClassNames = parentChildMap.getOrDefault(category.className(), Collections.emptyList());

        List<CategoryInfo> children = new ArrayList<>();
        for (String childClassName : childClassNames) {
            CategoryInfo childCategory = allCategories.get(childClassName);
            if (childCategory != null) {
                // Recursively build the child with its children
                CategoryInfo childWithChildren = buildCategoryWithChildren(childCategory, allCategories, parentChildMap);
                children.add(childWithChildren);
            }
        }

        // Sort children by English name
        children.sort(Comparator.comparing(CategoryInfo::englishName));

        return CategoryInfo.builder()
                .className(category.className())
                .englishName(category.englishName())
                .englishAltLabels(category.englishAltLabels())
                .norwegianName(category.norwegianName())
                .description(category.description())
                .parentClassName(category.parentClassName())
                .uri(category.uri())
                .properties(category.properties())
                .children(children)
                .build();
    }

    /**
     * Build a CategoryInfo object from an RDF resource
     */
    private CategoryInfo buildCategoryInfo(Resource classResource, Model model, String namespace) {
        String uri = classResource.getURI();
        String className = getLocalName(uri);

        // Get labels
        String englishName = getLabel(classResource, "en");
        String norwegianName = getLabel(classResource, "no");
        List<String> englishAltLabels = getAltLabels(classResource, "en");

        if (englishName == null) {
            englishName = className; // Fallback to class name
        }

        // Get comment/description
        String description = null;
        Statement commentStmt = classResource.getProperty(RDFS.comment);
        if (commentStmt != null) {
            description = commentStmt.getString();
        }

        // Get parent class
        String parentClassName = null;
        StmtIterator subClassStatements = classResource.listProperties(RDFS.subClassOf);
        while (subClassStatements.hasNext()) {
            Statement stmt = subClassStatements.nextStatement();
            Resource parentResource = stmt.getResource();
            if (parentResource.getURI() != null && parentResource.getURI().startsWith(namespace)) {
                parentClassName = getLocalName(parentResource.getURI());
                break; // Take the first namespace-related parent
            }
        }

        // Get properties defined for this class
        List<PropertyDefinition> properties = getPropertiesForClass(classResource, model, namespace);

        return CategoryInfo.builder()
                .className(className)
                .englishName(englishName)
                .englishAltLabels(englishAltLabels)
                .norwegianName(norwegianName)
                .description(description)
                .parentClassName(parentClassName)
                .uri(uri)
                .properties(properties)
                .children(Collections.emptyList()) // Will be populated in second pass
                .build();
    }

    /**
     * Get the localized label for a resource
     */
    private String getLabel(Resource resource, String language) {
        StmtIterator labelStatements = resource.listProperties(RDFS.label);
        while (labelStatements.hasNext()) {
            Statement stmt = labelStatements.nextStatement();
            Literal literal = stmt.getLiteral();
            if (literal != null && language.equals(literal.getLanguage())) {
                return literal.getString();
            }
        }
        return null;
    }

    /**
     * Get all skos:altLabel values for a resource in the given language. Returns an
     * empty list when none are declared.
     */
    private List<String> getAltLabels(Resource resource, String language) {
        List<String> alts = new ArrayList<>();
        StmtIterator labelStatements = resource.listProperties(SKOS_ALT_LABEL);
        while (labelStatements.hasNext()) {
            Statement stmt = labelStatements.nextStatement();
            Literal literal = stmt.getLiteral();
            if (literal != null && language.equals(literal.getLanguage())) {
                alts.add(literal.getString());
            }
        }
        return alts;
    }

    /**
     * Get properties that have this class as their domain
     */
    private List<PropertyDefinition> getPropertiesForClass(Resource classResource, Model model, String namespace) {
        List<PropertyDefinition> properties = new ArrayList<>();
        String classUri = classResource.getURI();

        ResIterator propertyIterator = model.listSubjectsWithProperty(RDF.type, RDF.Property);
        while (propertyIterator.hasNext()) {
            Resource propertyResource = propertyIterator.nextResource();

            if (propertyResource.getURI() != null && propertyResource.getURI().startsWith(namespace) && hasDomain(propertyResource, classUri)) {
                PropertyDefinition propertyDef = buildPropertyDefinition(propertyResource);
                properties.add(propertyDef);
            }
        }

        return properties;
    }

    /**
     * Check if a property has the specified class as its domain.
     * Supports both rdfs:domain (single-domain) and schema:domainIncludes (union-domain).
     */
    private boolean hasDomain(Resource propertyResource, String classUri) {
        StmtIterator domainStatements = propertyResource.listProperties(RDFS.domain);
        while (domainStatements.hasNext()) {
            Statement stmt = domainStatements.nextStatement();
            Resource domainResource = stmt.getResource();
            if (classUri.equals(domainResource.getURI())) {
                return true;
            }
        }
        StmtIterator domainIncludesStatements = propertyResource.listProperties(SCHEMA_DOMAIN_INCLUDES);
        while (domainIncludesStatements.hasNext()) {
            Statement stmt = domainIncludesStatements.nextStatement();
            Resource domainResource = stmt.getResource();
            if (classUri.equals(domainResource.getURI())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build a PropertyDefinition from an RDF property resource
     */
    private PropertyDefinition buildPropertyDefinition(Resource propertyResource) {
        String uri = propertyResource.getURI();
        String name = getLocalName(uri);
        String englishLabel = getLabel(propertyResource, "en");
        String norwegianLabel = getLabel(propertyResource, "no");

        // Get range type
        String rangeType = null;
        Statement rangeStmt = propertyResource.getProperty(RDFS.range);
        if (rangeStmt != null) {
            rangeType = rangeStmt.getResource().getURI();
        }

        // Get domain
        String domainClass = null;
        Statement domainStmt = propertyResource.getProperty(RDFS.domain);
        if (domainStmt != null) {
            domainClass = getLocalName(domainStmt.getResource().getURI());
        }

        // Cardinality marker: common:multiValued true ⇒ holds multiple values.
        boolean multiValued = false;
        Statement multiValuedStmt = propertyResource.getProperty(COMMON_MULTI_VALUED);
        if (multiValuedStmt != null) {
            multiValued = multiValuedStmt.getBoolean();
        }

        // Jurisdiction marker: common:jurisdiction "NO" ⇒ Norway-only property.
        String jurisdiction = getJurisdiction(propertyResource);

        // Enumeration values: individuals typed by the range class, if any.
        List<EnumValue> enumValues = rangeStmt != null
                ? getEnumValues(rangeStmt.getResource())
                : List.of();

        return PropertyDefinition.builder()
                .name(name)
                .englishLabel(englishLabel)
                .norwegianLabel(norwegianLabel)
                .uri(uri)
                .rangeType(rangeType)
                .domainClass(domainClass)
                .description(null) // Could add comments if needed
                .multiValued(multiValued)
                .jurisdiction(jurisdiction)
                .enumValues(enumValues)
                .build();
    }

    /**
     * Extract the local name from a URI
     */
    private String getJurisdiction(Resource resource) {
        Statement stmt = resource.getProperty(COMMON_JURISDICTION);
        return stmt != null ? stmt.getString() : null;
    }

    /**
     * Collect the individuals whose rdf:type is the given range class, in the model
     * the property was read from. Sorted by skos:notation, then local name, so the
     * order is stable for UI pickers. Empty when the range is a datatype, a marker
     * class without instances, or a class outside the loaded model.
     */
    private List<EnumValue> getEnumValues(Resource rangeClass) {
        Model model = rangeClass.getModel();
        if (model == null || rangeClass.getURI() == null) {
            return List.of();
        }
        List<EnumValue> values = new ArrayList<>();
        ResIterator it = model.listSubjectsWithProperty(RDF.type, rangeClass);
        while (it.hasNext()) {
            Resource individual = it.nextResource();
            if (individual.getURI() == null) {
                continue;
            }
            Statement notation = individual.getProperty(SKOS_NOTATION);
            String description = getLabelOrAny(individual, RDFS.comment);

            Map<String, String> attributes = new TreeMap<>();
            StmtIterator stmts = individual.listProperties();
            while (stmts.hasNext()) {
                Statement s = stmts.nextStatement();
                Property p = s.getPredicate();
                if (!s.getObject().isLiteral()
                        || p.equals(RDF.type) || p.equals(RDFS.label) || p.equals(RDFS.comment)
                        || p.equals(SKOS_NOTATION) || p.equals(COMMON_JURISDICTION)) {
                    continue;
                }
                attributes.put(getLocalName(p.getURI()), s.getLiteral().getLexicalForm());
            }

            values.add(EnumValue.builder()
                    .uri(individual.getURI())
                    .name(getLocalName(individual.getURI()))
                    .notation(notation != null ? notation.getString() : null)
                    .englishLabel(getLabel(individual, "en"))
                    .norwegianLabel(getLabel(individual, "no"))
                    .description(description)
                    .jurisdiction(getJurisdiction(individual))
                    .attributes(attributes)
                    .build());
        }
        values.sort(Comparator
                .comparing((EnumValue v) -> v.notation() == null ? "" : v.notation())
                .thenComparing(EnumValue::name));
        return values;
    }

    /** rdfs:comment in English if present, else any language, else null. */
    private String getLabelOrAny(Resource resource, Property predicate) {
        String any = null;
        StmtIterator it = resource.listProperties(predicate);
        while (it.hasNext()) {
            Statement s = it.nextStatement();
            if (!s.getObject().isLiteral()) continue;
            if ("en".equals(s.getLanguage())) return s.getString();
            if (any == null) any = s.getString();
        }
        return any;
    }

    private String getLocalName(String uri) {
        if (uri == null) return null;
        int hashIndex = uri.lastIndexOf('#');
        if (hashIndex >= 0) {
            return uri.substring(hashIndex + 1);
        }
        int slashIndex = uri.lastIndexOf('/');
        if (slashIndex >= 0) {
            return uri.substring(slashIndex + 1);
        }
        return uri;
    }

    /**
     * Exception thrown when taxonomy loading fails
     */
    public static class TaxonomyLoadException extends RuntimeException {
        /**
         * Create exception with message
         * @param message error message
         */
        public TaxonomyLoadException(String message) {
            super(message);
        }

        /**
         * Create exception with message and cause
         * @param message error message
         * @param cause underlying cause
         */
        public TaxonomyLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
