package no.sirktek.taxonomy.model;

import lombok.Builder;

import java.util.List;

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
 * @param multiValued    Whether the property holds multiple values (e.g. a
 *                       multi-category reference). Set from the
 *                       {@code common:multiValued} annotation in the taxonomy.
 * @param jurisdiction   ISO 3166-1 alpha-2 country code when the property only applies
 *                       under one country's law or accounting rules (e.g. "NO" for the
 *                       Norwegian depreciationGroup). Set from the
 *                       {@code common:jurisdiction} annotation; {@code null} means the
 *                       property applies everywhere.
 * @param enumValues     the values of an enumeration-typed property: the individuals whose
 *                       {@code rdf:type} is the range class, in the loaded model. Empty for
 *                       literal, marker-class and non-enumeration ranges. Each value carries
 *                       its own jurisdiction so a consumer can filter the list per country.
 */
@Builder
public record PropertyDefinition(
        String name,
        String englishLabel,
        String norwegianLabel,
        String uri,
        String rangeType,
        String domainClass,
        String description,
        boolean multiValued,
        String jurisdiction,
        List<EnumValue> enumValues) {

    public PropertyDefinition {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }
}
