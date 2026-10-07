package no.sirktek.taxonomy.model;

import org.junit.jupiter.api.Test;

import static no.sirktek.taxonomy.model.CommonPropertyDefinition.PropertyType;
import static no.sirktek.taxonomy.model.CommonPropertyDefinition.getPropertyType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommonPropertyDefinitionTest {

    private static PropertyDefinition withRange(String name, String range) {
        return PropertyDefinition.builder().name(name).rangeType(range).build();
    }

    @Test
    void mapsAssetValueEntryToAssetValueForAllThreePerYearValues() {
        String range = "http://taxonomy.sirktek.no/common#AssetValueEntry";
        assertEquals(PropertyType.ASSET_VALUE, getPropertyType(withRange("bookValue", range)));
        assertEquals(PropertyType.ASSET_VALUE, getPropertyType(withRange("taxValue", range)));
        assertEquals(PropertyType.ASSET_VALUE, getPropertyType(withRange("wealthTaxValue", range)));
    }

    @Test
    void mapsDepreciationGroupEnumeration() {
        assertEquals(PropertyType.DEPRECIATION_GROUP,
                getPropertyType(withRange("depreciationGroup", "http://taxonomy.sirktek.no/common#DepreciationGroup")));
    }

    @Test
    void leavesXsdAndUnknownRangesToDomainDetection() {
        assertNull(getPropertyType(withRange("ledgerAccount", "http://www.w3.org/2001/XMLSchema#string")));
        assertNull(getPropertyType(withRange("x", null)));
    }
}
