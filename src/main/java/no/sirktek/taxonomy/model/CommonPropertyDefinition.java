package no.sirktek.taxonomy.model;

/**
 * Maps {@code common:*} property range URIs to the orgadmin-side
 * {@link PropertyType} they should be rendered as.
 *
 * Only the cross-cutting property-range markers are mapped here —
 * EmissionEntry, ConsistsOfEntry, EnergySourceEntry. Domain taxonomies
 * (FurniturePropertyDefinition, LogisticsPropertyDefinition,
 * MachinePropertyDefinition) handle their own xsd-* and domain-specific
 * range types. Returns {@code null} for unrecognized ranges so callers
 * can fall back to their own detection.
 */
public class CommonPropertyDefinition {

    /**
     * Resolve the {@link PropertyType} for a common-namespace property
     * range. Returns {@code null} when the range is not a common URI —
     * caller should fall through to its own type detection.
     */
    public static PropertyType getPropertyType(PropertyDefinition propertyDef) {
        String rangeType = propertyDef.rangeType();
        if (rangeType == null) {
            return null;
        }
        return switch (rangeType) {
            case "http://taxonomy.sirktek.no/common#EmissionEntry"      -> PropertyType.EMISSION;
            case "http://taxonomy.sirktek.no/common#ConsistsOfEntry"    -> PropertyType.CONSISTS_OF;
            case "http://taxonomy.sirktek.no/common#EnergySourceEntry"  -> PropertyType.ENERGY_MIX;
            default -> null;
        };
    }

    /**
     * Property types contributed by the common taxonomy. Each maps 1:1
     * to a {@code common:*Entry} range-marker class.
     */
    public enum PropertyType {
        /** A list of per-LCA-module CO2e emission entries. */
        EMISSION,
        /** A bill-of-materials list of component category references with amounts. */
        CONSISTS_OF,
        /** A list of energy source entries describing an energy mix profile. */
        ENERGY_MIX
    }
}
