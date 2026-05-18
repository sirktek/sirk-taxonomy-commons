package no.sirktek.taxonomy.loader;

/**
 * Loads the common taxonomy from RDF-S Turtle.
 *
 * The common taxonomy holds cross-cutting classes (Manufacturer, Model,
 * Resource) and property range-marker classes (EmissionEntry,
 * ConsistsOfEntry, EnergySourceEntry) shared by all domain taxonomies
 * (furniture, logistics, machine, ...).
 */
public class CommonRdfsTaxonomyLoader extends RdfsTaxonomyLoader {

    private static final String COMMON_NAMESPACE = "http://taxonomy.sirktek.no/common#";
    private static final String RESOURCE_PATH = "/taxonomy/common-base.ttl";

    public CommonRdfsTaxonomyLoader() {
        super();
    }

    @Override
    protected String getNamespace() {
        return COMMON_NAMESPACE;
    }

    @Override
    protected String getResourcePath() {
        return RESOURCE_PATH;
    }
}
