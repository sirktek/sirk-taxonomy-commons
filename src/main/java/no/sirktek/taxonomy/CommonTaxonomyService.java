package no.sirktek.taxonomy;

import no.sirktek.taxonomy.loader.CommonRdfsTaxonomyLoader;

/**
 * Service for accessing the common (cross-cutting) taxonomy.
 *
 * The common taxonomy ships the categories that don't belong to any single
 * domain — Manufacturer, Model, Resource — plus the property range-marker
 * classes (EmissionEntry, ConsistsOfEntry, EnergySourceEntry) that describe
 * the shape of complex property values used across furniture, logistics
 * and machine taxonomies.
 *
 * Consumers using {@code MergedTaxonomyService} should add this service
 * first so domain taxonomies can reference {@code common:*} URIs as
 * property ranges.
 */
public class CommonTaxonomyService extends TaxonomyService {

    public CommonTaxonomyService() {
        super(new CommonRdfsTaxonomyLoader());
    }
}
