package no.sirktek.taxonomy.model;

import lombok.Builder;

import java.util.Map;

/**
 * One value of an enumeration-typed property: an RDF individual whose
 * {@code rdf:type} is the property's range class (e.g. the
 * {@code common:DepreciationGroup} instances, or {@code logistics:LocationType}).
 *
 * @param uri            full URI of the individual
 * @param name           local name (e.g. "DepreciationGroupH")
 * @param notation       {@code skos:notation} if present (e.g. the statutory letter "h")
 * @param englishLabel   {@code rdfs:label@en}
 * @param norwegianLabel {@code rdfs:label@no}
 * @param description    {@code rdfs:comment} (any language, English preferred)
 * @param jurisdiction   ISO 3166-1 alpha-2 code from {@code common:jurisdiction} when the
 *                       value only applies in one country; {@code null} means everywhere.
 *                       Consumers filter the list on this against the organisation's country.
 * @param attributes     other literal-valued statements on the individual, keyed by the
 *                       predicate's local name with the literal's lexical form as value
 *                       (e.g. {@code depreciationRate=4}). Never null.
 */
@Builder
public record EnumValue(
        String uri,
        String name,
        String notation,
        String englishLabel,
        String norwegianLabel,
        String description,
        String jurisdiction,
        Map<String, String> attributes) {

    public EnumValue {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
