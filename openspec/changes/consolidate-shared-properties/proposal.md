# Proposal — consolidate-shared-properties

## Why

The furniture, ict, logistics and machine taxonomies each declare their own copy of the same
property: `furniture:weight`, `machine:weight` and `logistics:weight` all have the key `weight`.
The product owner (2026-10-13) called the redeclaration wrong: a shared property is declared once
and attached to other taxonomies' classes. The mechanism shipped in commons 3.14
(`shared-commons-properties`): every domain loader reads `common-base.ttl` first, and a
`common:<p> schema:domainIncludes <domain>:<Class>` triple in the domain's TTL attaches the
property. A domain loader sees only commons and its own TTL, so a term shared by two domains has to
live in commons (verified in `RdfsTaxonomyLoader.java:103-116`, `:144`, `:314-329`).

Local names, and so every stored value, stay the same. The change is to taxonomy data only.

## What changes

- **Commons (`common-base.ttl`):** eleven new properties with no domain: `color`, `material`,
  `variant`, `serialNumber`, `model`, `operationsAndMaintenanceManual`, `length`, `width`,
  `height`, `weight`, `volume`. `controlChecklist` is a twelfth, with range
  `common:ChecklistTemplateEntry`, if the product owner OKs it (see open questions).
  `common:manufacturer` gets `schema:domainIncludes common:Model` in place of
  `rdfs:domain common:Model` and takes ict's `skos:altLabel "Vendor"@en`.
- **Domains:** each deletes its own declaration of these names and opts its classes in with
  `schema:domainIncludes`:
  - furniture: `Furniture`, all eleven new names
  - ict: `Hardware`/`Software` for `manufacturer` and `model`, `Hardware` for `serialNumber`
  - logistics: `Freight` for `weight` and `volume`, plus `ControlPoint` for `controlChecklist` if
    OK'd
  - machine: `Machine`, all eleven new names and `manufacturer`, plus `controlChecklist` if OK'd
- **Consumers:** sirk-orgadmin-backend and sirk-mcp-server bump commons and all four domains in a
  single commit. They need no code change.
- **Stays split:** `address`. `logistics:address` is a location's address, typed `ADDRESS`
  through the logistics mapper. Moved to commons, it would be typed `STRING` (design D3).

No loader, Java or test-fixture mechanism is added. Item 1 in design.md shows the loader never
emits two definitions with the same key here.

## Impact

- **Stored values:** no migration. orgadmin's `category_property.property_name` and
  sirk-app-backend's `asset.properties` keys are local names. No consumer keys on a property IRI.
- **Property IRIs change** (e.g. `machine:weight` becomes `common:weight`). The only reader of a
  property IRI is orgadmin's mapper routing (design D4). It gives every consolidated name the type
  it has today, with one exception: logistics `Freight.weight`/`volume` go from `DECIMAL` to
  `DECIMAL_KG`/`DECIMAL_M3`, the units their comments already state. Validation is the same
  (`CategoryValidationUtil.java:143`).
- **Releases:** one commons merge, one merge per domain, one bump commit each in orgadmin-backend
  and mcp-server. Everything goes through GitHub Packages. No Maven Central publish is needed
  (design D6).
- **Breaking:** no, for data or keys. Consumers must bump commons with or before the domains
  (design D6, Risks).

## Non-goals

- The ledger17-accounting overlaps (`ledgerAccount`, `depreciationGroup`, `depreciationRate`,
  `valuations`) are out of scope.
- Consolidating `address` (above).
- Changing loader or Java code: the domain `*PropertyDefinition` name heuristics and orgadmin's
  namespace routing stay as they are.
- Making furniture's existing `schema:domainIncludes common:Model` triples live
  (`furniture-base.ttl:240,271`). They are inert today because the commons loader never reads
  furniture's TTL.

## Open questions for the product owner

1. **`controlChecklist` into commons?** On 2026-10-15 you said no in the commons `checklists`
   proposal, because "Vardly and stored data already use `controlChecklist`". The local name
   stays `controlChecklist`, so stored values and Vardly are unaffected. Only the IRI changes
   (`logistics:`/`machine:` to `common:`), and nothing reads it (design D5). This design includes
   it, pending your explicit OK. If you say no, drop tasks 1.3, 4.3 and 5.3 and run the domain
   `checklists` slices as written, on the same branches (design D7).
2. **Freight weight/volume as kg/m³:** after the change, orgadmin types them `DECIMAL_KG` and
   `DECIMAL_M3`, like furniture and machine, instead of plain `DECIMAL`. Is that acceptable? The
   alternative is keeping them split, which keeps the redeclaration.
