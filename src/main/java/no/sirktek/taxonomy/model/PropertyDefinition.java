package no.sirktek.taxonomy.model;

import lombok.Builder;

/**
 * Represents a property definition in the RDF-S taxonomy
 *
 * @param name           Property name/identifier (local name from URI)
 * @param englishLabel   English label for the property
 * @param norwegianLabel Norwegian label for the property (if available)
 * @param uri            Complete property URI
 * @param rangeType      RDF range type (e.g., xsd:string, xsd:decimal, etc.)
 * @param domainClass    Domain classes this property applies to
 * @param description    Human-readable description
 */
@Builder
public record PropertyDefinition(
        String name,
        String englishLabel,
        String norwegianLabel,
        String uri,
        String rangeType,
        String domainClass,
        String description) {
}
