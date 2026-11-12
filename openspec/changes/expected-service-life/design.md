# Design — expected-service-life

## Precedent

- **Shared asset properties are declared once in commons, with no domain, and domains opt in**
  with `common:<p> schema:domainIncludes <domain>:<Class>` (`consolidate-shared-properties`;
  `common:weight`, `common:yearOfManufacture`, `common-base.ttl:165-239`). `expectedServiceLife`
  follows this.
- **Commons properties that name `common:Model` still need an explicit domain opt-in.**
  `common:emissionPerUnit` (`schema:domainIncludes common:Model, common:Resource`, range
  `common:EmissionEntry`, `common-base.ttl:152-156`) and `common:newPriceExVAT` (`common:Model`,
  `:455-460`) reach a domain class only through the domain's own triple:
  `common:emissionPerUnit schema:domainIncludes furniture:Furniture .` (`furniture-base.ttl:224`),
  `common:newPriceExVAT schema:domainIncludes furniture:Furniture .` (`:245`), and ICT's
  `newPriceExVAT` opt-in (`ict-base.ttl:139`). Logistics does not opt into `emissionPerUnit`. The
  machine and ICT opt-ins follow furniture.
- **Public unless commercial or accounting.** `privateSet` marks only prices and rents
  (`commercial`) and ledger terms (`accounting`); physical and age properties (`weight`,
  `yearOfManufacture`, `installationYear`) are public. `expectedServiceLife` is public: it is a
  technical property of the item and a stated input of a published method description, not a
  commercial term.
- **Units go in the label in parentheses** (`"Weight (kg)"`, `"Volume (m³)"`); the comment
  restates them. Years are `xsd:integer` (`yearOfManufacture`, `installationYear`), and orgadmin's
  only existing service life, `FunctionalUnitDto.expectedServiceLife`, is an `Integer` of years
  labelled "Forventet levetid" (`msg_no.properties:74`).
- **Category value and asset value use the same property.** A property opted into a domain class
  is settable on every category below it (orgadmin inherits category properties down the class
  tree) and on an asset (sirk-app-backend stores `asset.properties` by local name, names not
  validated: asset-age-and-new-price D4). `common:controlChecklist`'s comment states this; the new
  comment says the same.
- **`common:Model` is named only where the PO asked** (`newPriceExVAT`, decision 4 of
  asset-age-and-new-price). `weight` does not name it, and the method reads asset then category,
  never Model, so `expectedServiceLife` follows `weight` (PO 2026-11-12).
- **Class choice follows the age properties**, since `share` compares age with service life:
  `yearOfManufacture` is on `furniture:Furniture`, `machine:Machine` and `ict:Hardware`, not
  `ict:Software` (PO 2026-11-12).
- **Category values are per tenant.** Global (organisation-less) `category_property` rows are
  invisible to inheritance (orgadmin `CategoryInheritancePinTest.globalPropertyRowsAreInvisibleToInheritance`);
  a tenant's value on an RDF-S class row is inherited down its tree.

No departure.

## Inventory of everything new or changed

| Kind | Item | Reader in this change |
| --- | --- | --- |
| TTL (commons), new | `common:expectedServiceLife` (`xsd:integer`, public, no domain) | domain opt-ins (Groups 2–4) |
| TTL (furniture), new | `expectedServiceLife` on `furniture:Furniture` | loader |
| TTL (machine), new | `expectedServiceLife`, `newPriceExVAT`, `emissionPerUnit` on `machine:Machine` | loader |
| TTL (ict), new | `expectedServiceLife`, `weight`, `emissionPerUnit` on `ict:Hardware` | loader |
| pom | `taxonomy-commons` in furniture/machine/ict; commons, furniture, machine, ict versions in orgadmin-backend and mcp-server | build |
| tests | commons, furniture, machine, ict opt-in tests; orgadmin `CategoryInheritancePinTest`, `PrivatePropertyTreeIT` | — |

No class, Java production code, migration, dependency, config, DTO field, column or i18n key.

## Decisions

### D1 — Commons term

```turtle
# in "Shared asset properties", after common:installationYear
common:expectedServiceLife a rdf:Property ;
    rdfs:label "Expected service life (years)"@en, "Forventet levetid (år)"@no ;
    rdfs:comment "Expected service life of a new item, in whole years. Set on a category it is the default for every item in that category; set on an asset it replaces the category value for that item"@en ;
    rdfs:range xsd:integer .
```

### D2 — Opt-ins per domain

| Repo | Add | Next to |
| --- | --- | --- |
| furniture | `common:expectedServiceLife schema:domainIncludes furniture:Furniture .` | the age opt-ins, `furniture-base.ttl:243-246` |
| machine | `common:expectedServiceLife schema:domainIncludes machine:Machine .` | the age opt-ins, `machine-base.ttl:437-440`; extend the comment "Age: ..." to name it |
| machine | `common:newPriceExVAT schema:domainIncludes machine:Machine .` | the `monthlyRentExVAT` commercial opt-in, `:426-428`; the comment names both |
| machine | `common:emissionPerUnit schema:domainIncludes machine:Machine .` | a new short block "LCA (emissionPerUnit is declared in commons on Model and Resource)" in "Cross-cutting property domain extensions", like `furniture-base.ttl:221-224` |
| ict | `common:expectedServiceLife`, `common:weight`, `common:emissionPerUnit` `schema:domainIncludes ict:Hardware .` (one triple each) | `serialNumber`/`condition`/`yearOfManufacture`, `ict-base.ttl:152-155`; extend that comment |

logistics: nothing. `common:weight` is already on `logistics:Freight`; real estate service life is
not asked for.

orgadmin types `emissionPerUnit` `EMISSION` on any class: range markers are matched before the
namespace routing (asset-age-and-new-price D3), and machine's own mapper deliberately does not
recognise `common:EmissionEntry` (`PropertyDefinitionTest.shouldNotResolveCommonRangeMarkersToMachinePropertyType`).
`newPriceExVAT` on `Machine` is `DECIMAL`, `commercial`.

Each repo's delta spec (its slice, tasks Groups 2–4):

- furniture `furniture-common-properties`: ADDED "Furniture carries the commons expected service
  life" — `Furniture` has one `expectedServiceLife`, `common#` IRI, `xsd:integer`, no `privateSet`.
- machine `machine-common-properties`: ADDED "Machine carries the commons expected service life,
  new price and emission" — `Machine` has one each of `expectedServiceLife` (`xsd:integer`, no
  `privateSet`), `newPriceExVAT` (`xsd:decimal`, `commercial`) and `emissionPerUnit` (range
  `common#EmissionEntry`, no `privateSet`), all `common#` IRIs.
- ict `ict-common-properties`: MODIFIED "ICT carries the shared, new-price and age commons
  properties" — `ict:Hardware` only also gets `expectedServiceLife`, `weight` and
  `emissionPerUnit`; the allowed-commons list in its first scenario gains all three; a scenario for
  them on Hardware (ranges `xsd:integer`, `xsd:decimal`, `common#EmissionEntry`, no `privateSet`);
  Software carries none of them.

Each satellite gets a pointer `proposal.md` like asset-age-and-new-price's ("Tracked in
sirk-taxonomy-commons ...; this directory holds only this repo's delta spec").

### D3 — Range and orgadmin type

`xsd:integer` (PO 2026-11-12). orgadmin routes `common#` IRIs to the furniture mapper, which maps
`xsd:integer` to `INTEGER` (`FurniturePropertyDefinition.java:43`); the name heuristics (`weight`,
`volume`, `length`/`width`/`height`, `unit`, `resourceType`) apply to decimals and do not match
`expectedServiceLife`. ICT `weight` resolves to `DECIMAL_KG` as on furniture and machine.

### D4 — References and consumers (verified 2026-11-12)

| Repo | Reference | What it needs |
| --- | --- | --- |
| sirk-taxonomy-commons | `CommonTaxonomyServiceTest.sharedAssetPropertiesAreDeclaredWithoutDomain` and `sharedAssetPropertiesAttachToNoCommonsCategory` list terms | Add the term (Group 1) |
| sirk-ict-taxonomy | `CommonPropertyOptInTest.SHARED` is an allowlist of commons names on ICT classes | Add `expectedServiceLife`, `weight`, `emissionPerUnit` (Group 4) |
| sirk-furniture-, sirk-machine-taxonomy | `CommonPropertyOptInTest` checks named properties one by one; no exact set | New tests only |
| sirk-orgadmin-backend | `CategoryInheritancePinTest` (fixture JSON `:56-80`, `globalFurnitureHasExactlyTheNineteenDefinitions` `:256-273`) is the only place naming `newPriceExVAT`/`yearOfManufacture`; no seeded value, no production code keyed on them | Add `expectedServiceLife` (`INTEGER`); nineteen → twenty (Group 5) |
| sirk-orgadmin-backend | `PrivatePropertyTreeIT.machineCarriesRentAndAccountingAndSubclassesInheritThem` asserts Machine's private names exactly (`depreciationGroup, ledgerAccount, monthlyRentExVAT, valuations`) | Add `newPriceExVAT` (`DECIMAL`, `commercial`) to the list and to `assertMachineSet` (Group 5). RealEstate's count of 7 is unaffected |
| sirk-orgadmin-backend | `RdfsCategoryPropertyInheritanceTest` checks Machine/Hardware properties by name or `>=` counts | Nothing |
| sirk-mcp-server | serves the TTLs; precedent bump needed no test change | Bump (Group 6) |
| vardly-frontend, sirk-asset-frontend | asset-age-and-new-price changed them only for renamed keys (its D5); the new `newPriceExVAT`/`installationYear` needed nothing | Nothing |
| sirk-app-backend | pins commons for class names only; asset property names not validated | Nothing, no bump |

### D5 — Releases and order

Every merge to `main` tags `3.<run>` and publishes to GitHub Packages (asset-age-and-new-price D6).

1. sirk-taxonomy-commons (Group 1).
2. furniture, machine, ict (Groups 2–4), any order, each bumping commons to Group 1's tag.
3. sirk-orgadmin-backend (Group 5): one commit bumping commons and the three domains; logistics
   stays at 3.29.
4. sirk-mcp-server (Group 6): same four versions, one commit.

### D6 — Category values are per tenant (PO 2026-11-12)

Expected service life, new price and emission per category are entered by each tenant in
orgadmin's category property editor and stored in its `category_property` rows, as every category
value is today (Precedent). A shared or global default set is out of scope.

## Risks

- **Version skew:** a new domain jar on an old commons jar opts into an undeclared term, which then
  vanishes from the class. Mitigation: D5 steps 3–4 bump commons with the domains.
