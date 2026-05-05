package no.sirktek.taxonomy.model;

import lombok.Builder;

import java.util.List;

/**
 * Represents a single category in a taxonomy
 *
 * @param className          The RDF class name (URI fragment), e.g., "Location", "Furniture"
 * @param englishName        English label for the category (rdfs:label@en)
 * @param englishAltLabels   Alternative English labels (skos:altLabel@en) — used to match
 *                           legacy category strings whose phrasing differs from the
 *                           canonical English label, e.g., "Sitting Furniture" → SeatingFurniture.
 *                           Defaults to an empty list when no alt labels are declared.
 * @param norwegianName      Norwegian label for the category
 * @param description        Human-readable description
 * @param parentClassName    The parent category's class name, null for root categories
 * @param uri                Complete URI of this RDF class
 * @param properties         Properties defined for this category
 * @param children           Child categories
 */
@Builder
public record CategoryInfo(
        String className,
        String englishName,
        List<String> englishAltLabels,
        String norwegianName,
        String description,
        String parentClassName,
        String uri,
        List<PropertyDefinition> properties,
        List<CategoryInfo> children) {

    public CategoryInfo {
        englishAltLabels = englishAltLabels == null ? List.of() : List.copyOf(englishAltLabels);
    }

    /**
     * Whether this is a root category (no parent)
     * @return true if this category has no parent, false otherwise
     */
    public boolean isRoot() {
        return parentClassName == null;
    }

    /**
     * Stable negative Long ID derived from this category's URI.
     * See {@link RdfsCategoryIds} for the contract.
     */
    public Long negativeId() {
        return RdfsCategoryIds.negativeIdFromUri(uri);
    }

}
