# Design — checklists (taxonomy-commons slice)

## Precedent

- **Complex values:** a `common:*Entry` marker class with no properties of its own, its fields
  described in `rdfs:comment`, mapped 1:1 to a `CommonPropertyDefinition.PropertyType`
  (`EmissionEntry` → `EMISSION`, `AllocationEntry` → `ALLOCATIONS`, `AssetValuationEntry` →
  `ASSET_VALUATIONS`). Domain TTLs already use these as ranges (logistics-base.ttl:
  `rdfs:range common:EmissionEntry`, `common:AllocationEntry`, `common:EnergySourceEntry`).
- **Hidden from trees:** sirk-orgadmin-backend's `CategoryTaxonomyTreeService#isPropertyRangeMarker`
  drops every root class whose name ends in `Entry` from the merged tree, and domain loaders keep
  only their own namespace's classes (spec "Commons classes do not leak into a domain tree").

`common:ChecklistTemplateEntry` → `CHECKLIST` follows this exactly. No departure.

## Inventory of everything new

| Kind | Item | Reader |
| --- | --- | --- |
| RDF-S class | `common:ChecklistTemplateEntry` | the `rdfs:range` of `logistics:controlChecklist` and `machine:controlChecklist` (their `checklists` changes) |
| Enum constant | `CommonPropertyDefinition.PropertyType.CHECKLIST` | sirk-orgadmin-backend `CategoryPropertyInheritanceService.convertToPropertyType` (its `checklists` change) |

No Java class, dependency or loader change.

## Decisions

### C1: The marker

```turtle
common:ChecklistTemplateEntry a rdfs:Class ;
    rdfs:label "Checklist Template"@en, "Sjekkliste"@no ;
    rdfs:comment "One JSON-encoded ChecklistTemplate v1 (the cross-repo checklists contract, sirk-service-backend openspec/changes/checklists): {version: 1, items: [{key, text, guidance, required, responseType}]} with 1-100 items; key matches ^[a-z0-9][a-z0-9_-]{0,63}$ and is unique; text is non-blank and at most 500 characters after trim; guidance is optional and at most 2000; required is true or false; responseType is CHECK. A property with this range holds the whole template as one JSON string"@en .
```

Placed after `common:AssetValuationEntry` in the "Property range marker classes" section.

- **The `Entry` suffix** is what keeps the class out of orgadmin's merged tree (precedent). The
  value is one template, not a list of entries; the name follows the convention rather than
  adding a second filter rule in orgadmin.
- **Avoid "Point", "Machine", "Model", "Manufacturer"** in the name: the logistics and machine
  mappers' fallbacks match those substrings. `ChecklistTemplateEntry` contains none, so their own
  `getPropertyType` returns `STRING` for it and only `CommonPropertyDefinition` gives it a type.

### C2: The mapping

`CommonPropertyDefinition.getPropertyType` gains
`case "http://taxonomy.sirktek.no/common#ChecklistTemplateEntry" -> PropertyType.CHECKLIST;`
and the enum gains `CHECKLIST` with the javadoc "One ChecklistTemplate v1, stored as a JSON
string (`ChecklistTemplateEntry`)". The class javadoc's list of markers names it.

## Risks

- None beyond a release: consumers see the new type only after they bump and a domain TTL uses
  the range.
