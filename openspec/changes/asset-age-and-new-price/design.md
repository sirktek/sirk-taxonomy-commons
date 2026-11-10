# Design — asset-age-and-new-price

## Precedent

- **Shared properties are declared once in commons and domains opt in** with
  `common:<p> schema:domainIncludes <domain>:<Class>` in their own TTL
  (`consolidate-shared-properties`, archived 2026-10-15). A domain deletes its own declaration of
  the same local name in the same commit. `yearOfManufacture` and `installationYear` follow this.
- **A commons property that must reach a commons class as well as domain classes names the
  commons class in commons and the domain opts in.** `common:emissionPerUnit` has
  `schema:domainIncludes common:Model` in `common-base.ttl:152-156`, and furniture adds
  `common:emissionPerUnit schema:domainIncludes furniture:Furniture` (`furniture-base.ttl:229`).
  orgadmin inherits category properties down the class tree, so an opt-in on `Furniture` makes the
  property settable on `OfficeChair`. `newPriceExVAT` follows this (PO decision 4).
- **Commercial terms are private (`common:privateSet "commercial"`), `xsd:decimal`, in NOK, and
  state "excl. VAT" in the label and comment** (`common:monthlyRent`, `common:monthlyServiceCharge`,
  `common-base.ttl` "Commercial terms"). `newPriceExVAT` follows this; the commercial terms carry
  no domain, so `newPriceExVAT`'s `domainIncludes common:Model` is the one departure, required by
  PO decision 4 and covered by the `emissionPerUnit` precedent above.
- **A replaced domain property is deleted without migrating its values.** furniture #9
  replaced `furniture:state` with `common:condition`: "existing values under the `state` key are
  not migrated". This change does the same for `productionDate` and `builtYear`.
- **Stored values are keyed by local name** (orgadmin `category_property.property_name`,
  sirk-app-backend `asset.properties` and private-property maps). A local-name change orphans
  values; an IRI change alone does not.
- **Commons terms are labelled in English and Norwegian.**
- **An alternative wording is a `skos:altLabel` on the term** (`common:manufacturer`
  `skos:altLabel "Vendor"@en`, `common-base.ttl:103`). `RdfsTaxonomyLoader` reads `skos:altLabel`
  for classes only (`CategoryInfo.englishAltLabels`); on a property it reaches no
  `PropertyDefinition`, and "Vendor" is asserted only on the raw Jena model
  (`CommonTaxonomyServiceTest:263`). Its reader is sirk-mcp-server, which serves the TTL files
  (`TaxonomySchemas.java:26`). The `yearOfManufacture` altLabels follow this exactly: data only,
  no loader change.

No other departure.

## Inventory of everything new, changed or removed

| Kind | Item | Reader in this change |
| --- | --- | --- |
| TTL (commons), new | `common:yearOfManufacture`, `common:installationYear` (`xsd:integer`, public, no domain) | domain opt-ins (Groups 2–5) |
| TTL (commons), new | `common:newPriceExVAT` (`xsd:decimal`, `commercial`, `domainIncludes common:Model`) | furniture and ict opt-ins (Groups 2, 5); commons `Model` |
| TTL (commons), renamed | `common:monthlyRent` → `common:monthlyRentExVAT`, `common:monthlyServiceCharge` → `common:monthlyServiceChargeExVAT` | machine and logistics opt-ins |
| TTL (commons), new | `skos:altLabel "Built year"@en, "Byggeår"@no` on `common:yearOfManufacture` | sirk-mcp-server (raw TTL); not the loader |
| TTL (domains), removed | `furniture:productionDate`, `machine:yearOfManufacture`, `logistics:builtYear` | — |
| TTL (domains), new | opt-in triples (D2 table), including `common:newPriceExVAT` and `common:installationYear` `schema:domainIncludes ict:Hardware, ict:Software`, and `common:yearOfManufacture schema:domainIncludes ict:Hardware` | loader |
| Java, removed | machine `MachinePropertyDefinition`'s `yearOfManufacture` → `YEAR` branch and the `PropertyType.YEAR` constant; orgadmin's `case YEAR` (`CategoryPropertyInheritanceService.java:974`) | — (D3) |
| pom | `taxonomy-commons` in furniture/machine/logistics/ict; five taxonomy versions in orgadmin-backend and mcp-server | build |
| i18n keys, renamed | vardly-frontend and sirk-asset-frontend keys named after the old local names (D5) | those frontends |

No class, migration, dependency, config, DTO field or column. The only Java production changes are the two deletions above.

## Decisions

### D1 — Commons terms

| Term | Range | Labels (en / no) | privateSet | Domain in commons |
| --- | --- | --- | --- | --- |
| `yearOfManufacture` | `xsd:integer` | "Year of manufacture" / "Produksjonsår"; `skos:altLabel` "Built year" / "Byggeår" | — | — |
| `installationYear` | `xsd:integer` | "Installation year" / "Installasjonsår" | — (public, PO 2026-11-09) | — |
| `newPriceExVAT` | `xsd:decimal` | "New price (excl. VAT)" / "Nypris (eks. mva)" | `commercial` | `schema:domainIncludes common:Model` |
| `monthlyRentExVAT` | `xsd:decimal` | "Monthly rent (excl. VAT)" / "Månedsleie (eks. mva)" (unchanged) | `commercial` | — |
| `monthlyServiceChargeExVAT` | `xsd:decimal` | "Monthly service charge (excl. VAT)" / "Felleskostnader per måned (eks. mva)" (unchanged) | `commercial` | — |

```turtle
# in "Shared asset properties", after common:condition and its grades
common:yearOfManufacture a rdf:Property ;
    rdfs:label "Year of manufacture"@en, "Produksjonsår"@no ;
    skos:altLabel "Built year"@en, "Byggeår"@no ;
    rdfs:comment "The year the item was manufactured or, for a building, built (four-digit year)"@en ;
    rdfs:range xsd:integer .

common:installationYear a rdf:Property ;
    rdfs:label "Installation year"@en, "Installasjonsår"@no ;
    rdfs:comment "The year the item was installed or put into use (four-digit year)"@en ;
    rdfs:range xsd:integer .

# in "Commercial terms"
common:newPriceExVAT a rdf:Property ;
    rdfs:label "New price (excl. VAT)"@en, "Nypris (eks. mva)"@no ;
    rdfs:comment "Price of a new equivalent item in NOK, excluding VAT. Set on a category it applies to every item in that category"@en ;
    common:privateSet "commercial" ;
    schema:domainIncludes common:Model ;
    rdfs:range xsd:decimal .

common:monthlyRentExVAT a rdf:Property ;            # was common:monthlyRent
    ...labels, comment, privateSet, range unchanged .

common:monthlyServiceChargeExVAT a rdf:Property ;   # was common:monthlyServiceCharge
    ...labels, comment, privateSet, range unchanged .
```

The "Commercial terms" header comment ("No domain: domains opt in") gains: "`newPriceExVAT` also
names `common:Model`, like `common:emissionPerUnit`."

### D2 — Opt-ins and deletions per domain

| Repo | Delete | Add |
| --- | --- | --- |
| furniture | `furniture:productionDate` (`furniture-base.ttl:202-205`) | `common:yearOfManufacture`, `common:installationYear`, `common:newPriceExVAT`, `common:valuations` `schema:domainIncludes furniture:Furniture` |
| machine | `machine:yearOfManufacture` (`machine-base.ttl:309-312` on origin/main) | `common:yearOfManufacture`, `common:installationYear` `schema:domainIncludes machine:Machine`; `common:monthlyRent` opt-in (`:437`) becomes `common:monthlyRentExVAT` |
| ict | — | `common:newPriceExVAT`, `common:installationYear` `schema:domainIncludes ict:Hardware, ict:Software` (one triple each, like `manufacturer`/`model` at `ict-base.ttl:136-137`); `common:yearOfManufacture schema:domainIncludes ict:Hardware` (next to `serialNumber`/`condition`, `:150-151`). Software gets no year of manufacture (PO 2026-11-10) |
| logistics | `logistics:builtYear` (`logistics-base.ttl:205-209`) | `common:yearOfManufacture`, `common:installationYear` `schema:domainIncludes logistics:RealEstate`; opt-ins at `:247-248` become `monthlyRentExVAT`/`monthlyServiceChargeExVAT` on `RealEstate, OperatedProperty` |

Each delete and its opt-in land in the same commit, so no class ever carries two definitions with
one key (consolidate-shared-properties D1). Machine's local name is unchanged, so its stored
values keep their key.

The local furniture, machine and ict checkouts are on `feature/asset-condition`, behind
`origin/main`; implementers branch from `origin/main`. Line numbers above are `origin/main`.

### D3 — Range for a year: `xsd:integer`

orgadmin types a property in `CategoryPropertyInheritanceService.convertToPropertyType`: commons
range markers, then category references, then routing by the property IRI's namespace; a
`common#` IRI falls back to the furniture mapper.

| Range | Furniture mapper (all `common#` terms) | Logistics mapper | Machine mapper |
| --- | --- | --- | --- |
| `xsd:gYear` | `STRING` (default branch) | `STRING` (today's `builtYear`) | `STRING` |
| `xsd:integer` | `INTEGER` | `INTEGER` | `INTEGER` (`YEAR` for the name `yearOfManufacture` until this change) |
| `xsd:date` | `DATE` | `DATE` | `DATE` |

`xsd:gYear` would be an unvalidated free-text field in every domain. `xsd:integer` gives
`INTEGER` (integer validation, `CategoryValidationUtil.java:157`), which is what machine reports
today. Type changes in orgadmin:

- furniture age: `DATE` (`productionDate`) → `INTEGER` (`yearOfManufacture`)
- real-estate age: `STRING` (`builtYear`, gYear) → `INTEGER`
- machine age: `INTEGER` → `INTEGER` (now via the furniture fallback, not `MachinePropertyDefinition`)

`newPriceExVAT` is `xsd:decimal` and its name matches no unit heuristic, so it is `DECIMAL`.
`MachinePropertyDefinition`'s `name.equals("yearOfManufacture")` → `YEAR` branch
(`MachinePropertyDefinition.java:53`) no longer sees loaded data: orgadmin routes the `common#` IRI
to the furniture mapper. It is deleted (Group 3), and with it the `PropertyType.YEAR` constant,
whose only other users are machine's own `PropertyDefinitionTest` (`:224`, `:365`) and orgadmin's
`case YEAR -> PropertyType.INTEGER` (`CategoryPropertyInheritanceService.java:974`). No other
repo names it (grep of orgadmin, app-backend, service-backend, mcp-server). Removing the constant
breaks orgadmin's compile on the bump, so orgadmin deletes `case YEAR` in the same commit as the
version bump (Group 6).

### D4 — References to the old names (verified 2026-11-09)

Grep of `productionDate|yearOfManufacture|builtYear|monthlyRent|monthlyServiceCharge` over
sirk-orgadmin-backend, sirk-orgadmin-frontend, sirk-app-backend, sirk-service-backend,
sirk-mcp-server, ledger17, sirk-asset-frontend, vardly-frontend and sirk-frontend-commons
(excluding `node_modules`, `target`, `openspec`, `specs`):

| Repo | Reference | What it needs |
| --- | --- | --- |
| sirk-orgadmin-backend | `V1.4.1__CategoryData.sql:11` seeds `productionDate` (null value, type `DATE`) on the root `furniture` category; `V1.6.0` copied it into `category_property` | Nothing; applied migrations are immutable. The null row stays in old databases, like `state` (PO 2026-11-09) |
| sirk-orgadmin-backend | `CategoryInheritancePinTest.java:67` (`productionDate` `DATE`), `:251-265` (sixteen names, no `privateSet`, no `valuations`) | Update pins (Group 6) |
| sirk-orgadmin-backend | `PrivatePropertyTreeIT.java:84-85,120,126,158,174` (`monthlyRent`, `monthlyServiceCharge` loaded from the taxonomy) | Rename (Group 6) |
| sirk-orgadmin-backend | `SimplePropertyValueJsonTest.java:59` (hand-built `monthlyRent`) | None; no taxonomy load |
| sirk-orgadmin-frontend | tests only: `privateProperties.test.tsx`, `CategoryEdit.parent.test.tsx` (hand-built fixtures) | None |
| sirk-app-backend | tests only: `PrivateProperties*Test.java` (hand-built `monthlyRent`/`monthlyServiceCharge` keys); private-set keys come from config, names are not validated (`AssetService.java:138,662`) | None. It pins commons 3.19/machine 3.21/logistics 3.28 for class names only; no bump |
| sirk-service-backend, sirk-mcp-server, ledger17 | no hits | mcp-server: bump (Group 7), it serves the TTLs |
| vardly-frontend | **production code** `src/helpers/propertyFacts.ts:21` `FACT_KEYS` contains `'builtYear'`; i18n `properties.facts.builtYear` (`en.json:112`/`no.json:112`), `labels.commercial.monthlyRent`/`monthlyServiceCharge` (`:179-180`); many test fixtures | Group 8 |
| sirk-asset-frontend | i18n `assets.detail.taxonomy.properties.productionDate` (`en.json`/`no.json:507`), `privateProperties.labels.commercial.monthlyRent`/`monthlyServiceCharge` (`:1002-1003`); private labels fall back to the raw name (`privatePropertyCopy.ts:20`); tests use `builtYear`/`monthlyRent` fixtures | Group 9 |
| sirk-frontend-commons | tests only (hand-built `monthlyRent`/`monthlyServiceCharge`) | None |

No production code stores or computes on these keys beyond the display lists above. Whether any
database row holds a value is the PO's statement (2026-11-09), not verified against production.

### D5 — Frontend keys named after local names

Both frontends already key some labels and one fact list by local name. This change does not add
any; it renames the existing keys so the display survives:

- vardly-frontend: `FACT_KEYS` `'builtYear'` → `'yearOfManufacture'`; i18n
  `properties.facts.builtYear` → `properties.facts.yearOfManufacture`, keeping "Built year"/"Byggeår"
  (Vardly shows real estate, where "Byggeår" is the right word); `labels.commercial.monthlyRent` →
  `monthlyRentExVAT`, `monthlyServiceCharge` → `monthlyServiceChargeExVAT`; test fixtures follow.
- sirk-asset-frontend: `assets.detail.taxonomy.properties.productionDate` →
  `yearOfManufacture` ("Year of manufacture"/"Produksjonsår");
  `privateProperties.labels.commercial.*` renamed as above; test fixtures follow.

Without Group 9, sirk-asset-frontend would label the private fields with the raw names
`monthlyRentExVAT` / `monthlyServiceChargeExVAT`.

### D6 — Releases and order

Every merge to `main` tags `3.<run>` and publishes to GitHub Packages; no Maven Central publish is
needed (consolidate-shared-properties D6: sirk-app-backend uses class names only).

1. sirk-taxonomy-commons (Group 1).
2. furniture, machine, logistics, ict (Groups 2–5), any order, each bumping commons to Group 1's
   tag.
3. sirk-orgadmin-backend (Group 6): one commit bumping commons and the four domains (ict 3.9 →
   Group 5's tag) and deleting `case YEAR`.
4. sirk-mcp-server (Group 7): same five versions, one commit (ict 3.8 today).
5. vardly-frontend and sirk-asset-frontend (Groups 8, 9): deploy with or right after Group 6.
   Before Group 6 deploys, the new keys show nothing; after, the old keys show nothing.

## Risks

- **Version skew** (consolidate-shared-properties Risks): a new domain jar on an old commons jar
  opts into undeclared terms, which then vanish from the class. Mitigation: D6 step 3.
- **Removing `PropertyType.YEAR` is a binary break** of the machine jar for any switch over it;
  orgadmin is the only one (D3).
- **Renames are breaking** for anything holding values under the old keys; accepted on the PO's
  statement that none exist (D4).
