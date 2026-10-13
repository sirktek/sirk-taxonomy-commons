# Design — shared-commons-properties

## Precedent

- **Shared properties:** declared once in `common-base.ttl` with no domain; a domain opts a class
  in with `common:<p> schema:domainIncludes <domain>:<Class>` in its own TTL (commons TTL section
  comments, furniture-base.ttl:282-287, README "domainIncludes"). The comments already say
  "Merged into a single Jena graph at load time"; the code never did it. This change makes the
  code match the documented precedent; it is not a new mechanism.
- **Annotations on properties:** `common:jurisdiction` / `common:multiValued` are `rdf:Property`
  declarations in commons, read in `RdfsTaxonomyLoader.buildPropertyDefinition` through a
  `private static final Property COMMON_…` constant and copied onto a `PropertyDefinition` record
  component. `common:privateSet` follows this exactly.
- **Complex values:** a `common:*Entry` marker class with no properties of its own, its fields
  described in `rdfs:comment`, mapped 1:1 to a `CommonPropertyDefinition.PropertyType`
  (`EmissionEntry` → `EMISSION`, `AssetValueEntry` → `ASSET_VALUE`). `AssetValuationEntry` →
  `ASSET_VALUATIONS` follows this; no field properties are declared for it.
- **Enumerations:** individuals of the range class in the loaded model become
  `PropertyDefinition.enumValues` (`getEnumValues`). Unchanged; it starts working for domain
  loaders because the commons individuals are now in their model.

Departure (PO-approved 2026-10-13): `getPropertiesForClass` stops filtering properties by the
loader's namespace. Reason: the opt-in precedent cannot work otherwise.

## Inventory of everything new or removed

| Kind | Item | Reader in this change |
| --- | --- | --- |
| Loader | reads `/taxonomy/common-base.ttl` before the own TTL | every loader |
| Loader | namespace test removed from `getPropertiesForClass` | every loader |
| TTL | `common:privateSet` (annotation property, `xsd:string`) | loader |
| Record | `PropertyDefinition.privateSet` (String, null when absent) | consumers |
| TTL | `common:valuations` (range `common:AssetValuationEntry`) | loader, test |
| TTL | `common:AssetValuationEntry` (marker class) | `CommonPropertyDefinition` |
| Enum | `CommonPropertyDefinition.PropertyType.ASSET_VALUATIONS` | orgadmin (follow-up) |
| TTL | `common:marketValue`, `marketValueDate`, `monthlyRent`, `monthlyServiceCharge` | domains opt in (follow-up) |
| Removed | `common:bookValue`, `common:taxValue`, `common:wealthTaxValue`, `common:AssetValueEntry`, `PropertyType.ASSET_VALUE` | — |

No new dependency, class, package, config or Java constant per property/class. The test adds one
fixture TTL and a test-only loader subclass.

## Decisions

### D1 — Loader reads commons, properties attach across namespaces

`loadTaxonomyFromResource(resourcePath)` creates the model, reads `/taxonomy/common-base.ttl`
through `RdfsTaxonomyLoader.class.getResourceAsStream` (the commons jar, whatever classloader the
subclass has), then the requested resource as today. The commons loader reads commons twice; an
RDF graph is a set, so this is harmless and needs no special case.

`getPropertiesForClass` drops `propertyResource.getURI().startsWith(namespace)`; `hasDomain`
alone decides. Unchanged, and verified:

- **Classes** stay filtered by namespace in `buildTaxonomyTree` (`startsWith(namespace)`), so
  commons classes (`Manufacturer`, `DepreciationGroup`, `*Entry`) never appear in a domain tree;
  parent resolution in `buildCategoryInfo` stays namespace-filtered.
- **Property keys** stay local names (`getLocalName(uri)`): `common:marketValue` → `marketValue`.
  `uri` carries the full IRI, so consumers can still tell the namespace.
- **Enum values** (`getEnumValues`) read from `rangeClass.getModel()`, the merged model, so
  `depreciationGroup` on a domain class yields the ten `common:DepreciationGroup` individuals
  with notation, labels, `jurisdiction` and `attributes.depreciationRate`.
- **`domainClass`** comes from `rdfs:domain` only; commons properties have none, so it is `null`
  (as for every `domainIncludes`-only property today).

Constraint for the domain follow-ups (documented, not enforced): a domain must not opt a class
into a commons property whose local name it also declares on that class, or the class gets two
properties with the same key. Today only `logistics:marketValue` collides, and logistics deletes
it in the same change that opts into `common:marketValue`.

### D2 — `common:privateSet`

```turtle
common:privateSet a rdf:Property ;
    rdfs:label "Private set"@en, "Privat sett"@no ;
    rdfs:comment "Annotation on a property: the key of the private property set its values belong to (e.g. \"accounting\", \"commercial\"). Values in a private set are visible only to the owning organisation. Absent ⇒ public property"@en ;
    rdfs:range xsd:string .
```

Loader: `COMMON_PRIVATE_SET` constant, `getString()` when present, else `null`;
`PropertyDefinition` gains `String privateSet` after `jurisdiction` (javadoc `@param` like
jurisdiction's). Not excluded in `getEnumValues` attributes (never put on individuals).

### D3 — Accounting with ledger17 names and shape

ledger17 (`accounting-core.ttl`, `accounting-no.ttl`): `acct:ledgerAccount` (xsd:string),
`acct:depreciationGroup` (range `acct:DepreciationGroup`), `acct:valuations` (range
`acct:AssetValuation` with `acct:year` integer, `acct:bookValue` decimal, `acct:taxValue` decimal,
`acctno:wealthValue` decimal).

- `common:ledgerAccount`, `common:depreciationGroup`: local names already match ledger17. Add
  `common:privateSet "accounting"`; nothing else changes.
- `common:DepreciationGroupA`…`J`: local names, notations a–j and rates (30, 20, 24, 20, 14, 12,
  5, 4, 2, 10) match `acctno:DepreciationGroupA`…`J`. Labels differ in wording only; keep ours.
- New:

```turtle
common:AssetValuationEntry a rdfs:Class ;
    rdfs:label "Asset Valuation Entry"@en ;
    rdfs:comment "The values of one asset at the end of one fiscal year: year (integer, the year the fiscal year ends), bookValue (carrying amount after accounting depreciation), taxValue (tax written-down value, saldoverdi), wealthValue (formuesverdi, Norway only). Amounts are decimals in NOK; every field but year is optional. Same names and shape as ledger17's acct:AssetValuation"@en .

common:valuations a rdf:Property ;
    rdfs:label "Valuations"@en, "Verdsettelser"@no ;
    rdfs:comment "The yearly valuations of the asset, one entry per fiscal year (see AssetValuationEntry)"@en ;
    common:privateSet "accounting" ;
    rdfs:range common:AssetValuationEntry .
```

  No `common:jurisdiction` on `valuations`: the property is neutral, only its `wealthValue` field is
  Norwegian (stated in the comment, as ledger17 states it on `acctno:wealthValue`).
- `CommonPropertyDefinition`: `…#AssetValuationEntry` → `ASSET_VALUATIONS` (javadoc: a list of
  `{year, bookValue, taxValue, wealthValue}` per fiscal year). Remove the `AssetValueEntry` case
  and `ASSET_VALUE`.
- Delete `common:bookValue`, `common:taxValue`, `common:wealthTaxValue`, `common:AssetValueEntry`
  and rewrite the "Cross-cutting financial properties" / "accounting and tax" comment blocks to
  describe `valuations`. `common:jurisdiction`, `common:depreciationRate`, `DepreciationGroup`
  and its individuals stay.

### D4 — Commercial terms

From sirk-commercial-taxonomy `commercial-base.ttl` (1.1), without `appliesToClass`, SHACL or
`messageNo`; no `rdfs:domain` / `schema:domainIncludes` (domains opt in):

| Property | Range | en | no |
| --- | --- | --- | --- |
| `common:marketValue` | `xsd:decimal` | Market value | Markedsverdi |
| `common:marketValueDate` | `xsd:date` | Valuation date | Verdsettelsesdato |
| `common:monthlyRent` | `xsd:decimal` | Monthly rent (excl. VAT) | Månedsleie (eks. mva) |
| `common:monthlyServiceCharge` | `xsd:decimal` | Monthly service charge (excl. VAT) | Felleskostnader per måned (eks. mva) |

Each carries `common:privateSet "commercial"` and the commercial `rdfs:comment` minus references
to other services. They map to no common `PropertyType` (`getPropertyType` returns `null`; the
consumer's xsd detection applies, as for `ledgerAccount`).

### D5 — Tests

New fixture `src/test/resources/taxonomy/test-domain-optin.ttl` in namespace
`http://taxonomy.sirktek.no/testdomain#`: class `TestBuilding` with one own property
(`testdomain:floors`, xsd:integer) and opt-ins `common:depreciationGroup`, `common:valuations`,
`common:marketValue` via `schema:domainIncludes testdomain:TestBuilding`. A test-only
`TestDomainLoader extends RdfsTaxonomyLoader` (namespace + resource path). New
`DomainOptInLoaderTest` asserts:

- `TestBuilding` has exactly `floors`, `depreciationGroup`, `valuations`, `marketValue` (keys are
  local names; `uri` of `marketValue` is `common#marketValue`); `ledgerAccount` (not opted in) is
  absent.
- `privateSet`: `accounting` on `depreciationGroup` and `valuations`, `commercial` on
  `marketValue`, `null` on `floors`.
- `depreciationGroup.enumValues()` are the ten saldogrupper, notations a–j, all `jurisdiction`
  `NO`, `DepreciationGroupH` with `depreciationRate` `4`.
- `CommonPropertyDefinition.getPropertyType(valuations)` is `ASSET_VALUATIONS`.
- The tree has only `testdomain` classes (no commons class leaks).

Existing tests updated: `CommonTaxonomyServiceTest.accountingPropertiesAreDeclared…` (ledgerAccount,
depreciationGroup, valuations; the three deleted properties and `AssetValueEntry` absent;
`privateSet` values; no domain), `rangeMarkerClassesHaveNoProperties` (`AssetValuationEntry`
instead of `AssetValueEntry`), a commercial-terms declaration test (ranges, labels, privateSet,
no domain), `CommonPropertyDefinitionTest` (`ASSET_VALUATIONS`, no `ASSET_VALUE`).

## Inventory: what newly attaches per domain once it bumps (TTL unchanged)

Opt-ins today (grep `common:` in each `*-base.ttl`, commons 3.13):

| Domain | Class | Attaches after bump | Inert (target deleted) |
| --- | --- | --- | --- |
| logistics | RealEstate | `ledgerAccount`, `depreciationGroup` (privateSet accounting, 10 options) | `wealthTaxValue`, `bookValue`, `taxValue` (logistics-base.ttl:229, 237, 238) |
| machine | Machine | `ledgerAccount`, `depreciationGroup` | `bookValue`, `taxValue` (machine-base.ttl:519-520) |
| furniture | Furniture | `emissionPerUnit` (EMISSION), `consistsOf` (CONSISTS_OF), `ledgerAccount`, `depreciationGroup` | `bookValue`, `taxValue` (furniture-base.ttl:293-294) |
| ict | Hardware, Software | `ledgerAccount`, `depreciationGroup` | `bookValue`, `taxValue` (ict-base.ttl:259-262) |

Nothing attaches that a domain did not opt into: commons properties with `rdfs:domain` /
`domainIncludes` on commons classes (`address`, `manufacturer`, `emissionPerUnit` on Model, …)
match only commons classes, which domain trees exclude. `valuations` and the commercial terms
attach nowhere until the domain follow-ups opt in. Per PO round 3, the machine, furniture and ict
follow-ups delete their accounting opt-ins, so `ledgerAccount` / `depreciationGroup` on those
classes appear only between a bump and that edit; ship each domain's bump and opt-in edit in one
release.

## Inventory: consumers of `ASSET_VALUE` / `AssetValueEntry` / `bookValue` / `taxValue` / `wealthTaxValue`

Grep over ~/dev (code only; openspec/docs omitted; ledger17's own `bookValue`/`taxValue` are its
domain fields, unrelated):

- **sirk-orgadmin-backend:** `CategoryPropertyInheritanceService:700` (exhaustive switch on the
  commons enum, compile break on bump), its own `dto/taxonomy/PropertyType.ASSET_VALUE`,
  `AssetValueCategoryProperty`, `AssetValueEntry`, `CategoryPropertyDto` subtype mapping,
  `CategoryPropertyDtoFactory` (parse/serialise), `CategoryValidationUtil:139`,
  `CategoryPropertyInheritanceService:524,664`; tests `CategoryPropertyIT`,
  `JurisdictionFilteringTest`.
- **sirk-orgadmin-frontend:** `types/category.ts` (`ASSET_VALUE`), `AddPropertyRow`,
  `AssetValueInput`, `PropertyValueDisplay`, `PropertyValueInput`, `PropertyTypesEditor`,
  `CategoryShow`, `categoryPayloadHelpers`, i18n en/no, `categoryHelpers.test.ts`.
- **sirk-logistics/machine/furniture/ict-taxonomy:** TTL opt-ins (above), README tables,
  `CommonPropertyOptInTest` lists; ict `pom.xml` and `IctPropertyDefinition` comments.
- **sirk-app-backend:** `AssetPrivatePropertiesDaoTest` uses `"bookValue"` as an arbitrary set key
  in a fixture; no dependency on the type. No change needed.
- **sirk-frontend-commons, vardly-homepage:** none. **sirk-asset-frontend:** only a local variable
  `assetValue` (unrelated). **vardly-frontend / sirk-asset-frontend:** design docs only.

Accounting is the only user of `ASSET_VALUE`, so it is deleted here; downstream deletions are the
follow-ups in proposal.md.

## Risks

- Removing `ASSET_VALUE` breaks orgadmin-backend's compile on bump: intended, fixed in its
  follow-up. Stored orgadmin category rows of type `ASSET_VALUE` are orgadmin's own enum and keep
  loading until remove-category-accounting.
- A domain bump without its TTL follow-up shows accounting on Machine/Furniture/Hardware/Software
  in orgadmin's tree: mitigated by bundling bump and opt-in edit per domain.
- The same applies on the consumer side: orgadmin-backend pins taxonomy-commons directly, so
  Maven nearest-wins applies the merging loader to whatever domain jars it still has. Its bump
  must land together with, or after, all four domain follow-ups; otherwise the old
  `ledgerAccount` / `depreciationGroup` opt-ins attach in orgadmin's tree until they do.
