# Design — consolidate-shared-properties

## Precedent

- **Shared properties are declared once in commons, with no domain, and domains opt in.** The
  commercial terms and `valuations` (`common-base.ttl:158-162`, `:313-335`) carry no domain. A
  domain attaches one with `common:<p> schema:domainIncludes <domain>:<Class>` in its own TTL, for
  example `machine-base.ttl` `common:monthlyRent schema:domainIncludes machine:Machine` (commons
  README "Opting in from a domain"). This change follows that pattern.
- **A property used on several classes takes `schema:domainIncludes`, not repeated
  `rdfs:domain`.** See `common:emissionPerUnit` and `common:consistsOf` (`common-base.ttl:124-135`)
  and `ict:manufacturer`/`ict:model`.
- **Commons terms are labelled in English and Norwegian.** See the commercial terms.
- **Units live in the label text and in the consumer's name heuristic, not in RDF.** Examples:
  `"Weight (kg)"`, and `FurniturePropertyDefinition` `name.contains("weight")` → `DECIMAL_KG`
  (`FurniturePropertyDefinition.java:33-36`, `MachinePropertyDefinition.java:35-44`).
- **Stored values are keyed by local name.** See `CategoryProperty.propertyName`, column
  `property_name` (sirk-orgadmin-backend `CategoryProperty.java:34-35`), and `Asset.properties`
  `Map<String, String>` (sirk-app-backend `Asset.java:100-101`).

No departure. Moving `common:manufacturer` from `rdfs:domain` to `schema:domainIncludes` applies
the second precedent.

## Inventory of everything new or changed

| Kind | Item | Reader in this change |
| --- | --- | --- |
| TTL (commons) | `common:color`, `material`, `variant`, `serialNumber`, `model`, `operationsAndMaintenanceManual`, `length`, `width`, `height`, `weight`, `volume` | domain opt-ins (Groups 2–5) |
| TTL (commons) | `common:controlChecklist` (range `common:ChecklistTemplateEntry`) | logistics and machine opt-ins (Groups 4, 5) |
| TTL (commons), changed | `common:manufacturer`: `rdfs:domain common:Model` becomes `schema:domainIncludes common:Model`; adds `skos:altLabel "Vendor"@en` (moved from ict) | ict and machine opt-ins; mcp-server serves the raw TTL |
| TTL (domains), removed | 11 `furniture:*`, 3 `ict:*`, 2 or 3 `logistics:*`, 12 or 13 `machine:*` declarations (D2 table) | — |
| TTL (domains) | `schema:domainIncludes` opt-in triples, one per name and class | loader |
| pom | `taxonomy-commons` version in the four domains; all five taxonomy versions in orgadmin-backend and mcp-server | build |

No new class, Java code, dependency, config, DTO field, column or test fixture. Each domain's
existing `CommonPropertyOptInTest` gains assertions.

## Decisions

### D1 — Duplicate keys: none today, none during the change; no loader change

- **Can the loader emit two definitions with one key?** Yes, if two properties with the same
  local name both name a class. `getPropertiesForClass` (`RdfsTaxonomyLoader.java:314-329`) adds
  every match with no de-duplication, and the key is the local name (`:360`).
- **Does it happen for `address` or `manufacturer`?** No. `common:address` has
  `rdfs:domain common:Manufacturer` and `common:manufacturer` has `rdfs:domain common:Model`
  (`common-base.ttl:67-70`, `:86-89`). Domain trees keep only their own namespace's classes
  (`RdfsTaxonomyLoader.java:144`), and no domain TTL opts a class into either term (grep
  `common:address|common:manufacturer` over the four `*-base.ttl`: no hits). Each domain class
  carries only its own `address` or `manufacturer`.
- **What do consumers do with duplicates?** orgadmin keys a category's properties by name in
  `propertyMap`. The first RDF-S definition fixes type, range root, options and private set, and
  later ones only fill nulls (`CategoryPropertyInheritanceService.java:530-560`). A second
  definition would be dropped silently. The frontends receive orgadmin's list, already keyed by
  name. mcp-server serves raw Turtle and does not build definitions (`TaxonomySchemas.java:46-52`).
- **During this change:** each domain deletes its own declaration and adds the opt-in in the same
  commit, and the new commons terms have no domain. No class ever carries two definitions with
  one key.

So the loader stays as it is. The constraint "a domain must not opt a class into a commons
property whose local name it also declares" (commons README) is enforced by each domain's
`CommonPropertyOptInTest` (task groups 2–5).

### D2 — Differences between the redeclarations, and which wins

All copies of a name share range, datatype and unit. None carries `common:privateSet`,
`common:jurisdiction` or `common:multiValued`. The only differences are missing Norwegian labels
(furniture) and freight-specific comments (logistics). The commons term takes the machine/ict
labels and a domain-neutral comment.

| Name | Declarations (file:line) | Range | Differences | Commons term |
| --- | --- | --- | --- | --- |
| `color` | furniture:248, machine:324 | `xsd:string` | furniture en only | "Color"@en, "Farge"@no |
| `material` | furniture:253, machine:329 | `xsd:string` | furniture en only | "Material"@en, "Materiale"@no |
| `variant` | furniture:202, machine:314 | `xsd:string` | furniture en only | "Variant"@en, "Variant"@no |
| `serialNumber` | furniture:243, ict:157, machine:319 | `xsd:string` | furniture en only | "Serial Number"@en, "Serienummer"@no |
| `model` | furniture:263, ict:141, machine:492 | `common:Model` | furniture en only; ict `domainIncludes` | "Model"@en, "Modell"@no |
| `operationsAndMaintenanceManual` | furniture:268, machine:487 | `xsd:anyURI` | furniture en only, extra inert `domainIncludes common:Model` | "Operations and Maintenance Manual"@en, "Drift- og vedlikeholdshåndbok"@no |
| `length` | furniture:217, machine:354 | `xsd:decimal`, cm | furniture en only | "Length (cm)"@en, "Lengde (cm)"@no |
| `width` | furniture:222, machine:359 | `xsd:decimal`, cm | furniture en only | "Width (cm)"@en, "Bredde (cm)"@no |
| `height` | furniture:227, machine:364 | `xsd:decimal`, cm | furniture en only | "Height (cm)"@en, "Høyde (cm)"@no |
| `weight` | furniture:212, logistics:371, machine:349 | `xsd:decimal`, kg | furniture en only; logistics label has no unit, comment "of the freight in kilograms" | "Weight (kg)"@en, "Vekt (kg)"@no; comment "The weight in kilograms" |
| `volume` | furniture:207, logistics:377, machine:369 | `xsd:decimal`, m³ | as `weight`; logistics comment "in cubic meters" | "Volume (m³)"@en, "Volum (m³)"@no; comment "The volume in cubic meters" |
| `manufacturer` | common:86, ict:134, machine:497 | `common:Manufacturer` | ict `skos:altLabel "Vendor"@en` | existing term; gains the altLabel |
| `controlChecklist` | logistics:260, machine:339 | `xsd:string` today; `common:ChecklistTemplateEntry` after `checklists` | comment names the subject (control point / machine) | "Checklist"@en, "Sjekkliste"@no; neutral comment (D5) |
| `address` | common:67, logistics:101 | `xsd:string` | different subject and consumer type | **stays split** (D3) |

Domain TTL paths: `src/main/resources/taxonomy/<domain>-base.ttl` in each repo; commons is
`common-base.ttl`.

Furniture's `operationsAndMaintenanceManual schema:domainIncludes common:Model` is dropped with
furniture's declaration. It is inert today because the commons loader never reads
`furniture-base.ttl`. Making it live would be new behaviour (proposal, non-goals).

### D3 — `address` stays split

`logistics:address` is typed `ADDRESS` because orgadmin routes `logistics#` IRIs to
`LogisticsPropertyDefinition` (`CategoryPropertyInheritanceService.java:733-734`,
`LogisticsPropertyDefinition.java:25`). As `common:address` it would fall through to the furniture
mapper and become `STRING` (`:744-747`). Keeping the type would need Java changes in orgadmin. The
two terms also describe different subjects: a manufacturer's address and a location's physical
address.

### D4 — How consumers type the consolidated properties

orgadmin types a property in `convertToPropertyType` (`CategoryPropertyInheritanceService.java:708-748`).
`CommonPropertyDefinition` range markers come first. Then it routes by the property IRI's
namespace, and a `common#` IRI falls back to the furniture mapper. For the consolidated names:

- color, material, variant, serialNumber: `STRING` from both the machine and furniture mappers.
- length/width/height `DECIMAL_CM`, weight `DECIMAL_KG`, volume `DECIMAL_M3`: same name heuristic
  in both mappers.
- operationsAndMaintenanceManual: `URL`.
- model/manufacturer: `CATEGORY`. The range contains "Model"/"Manufacturer" in the furniture,
  machine and ict mappers. None is `multiValued`, so ict's `MULTI_CATEGORY` branch does not apply.
- controlChecklist: `CHECKLIST` via `CommonPropertyDefinition`, whatever the namespace (orgadmin
  `checklists` change).

So types are unchanged for furniture, machine and ict. Logistics `Freight.weight`/`volume` change
from `DECIMAL` to `DECIMAL_KG`/`DECIMAL_M3`; validation of all decimal kinds is identical
(`CategoryValidationUtil.java:143`). RDF-S definitions are processed before stored rows
(`CategoryPropertyInheritanceService.java:254` before `:261`, `:269`), so the RDF-S type is
reported. orgadmin's existing pins (`CategoryPropertyInheritanceServiceTest.java:136,155,295-297`)
use furniture and keep passing.

`PropertyDefinition.domainClass` becomes `null` for properties reached through `domainIncludes`.
Nothing reads it (grep `domainClass` over orgadmin, app-backend, mcp-server, the domains and the
four frontends: only the model record).

### D5 — Stored values, IRIs, and `controlChecklist`

- **Values are keyed by local name.** Examples: orgadmin `category_property.property_name`, and
  `taxonomy_uri` is the category's IRI, not the property's (`CategoryProperty.java:27-35`).
  sirk-app-backend `asset.properties` / `batch.properties` are `Map<String, String>`. Local names
  are unchanged, so no data migration.
- **Nothing keys on a property IRI.** I grepped the full property IRIs
  `taxonomy.sirktek.no/(furniture|machine|ict|logistics|common)#<name>` for these names over the
  `src` of sirk-orgadmin-backend, sirk-app-backend, sirk-mcp-server, sirk-service-backend,
  sirk-orgadmin-frontend, sirk-asset-frontend, vardly-frontend and sirk-frontend-commons. The
  only hit is a hand-built fixture, `CategoryServiceTest.java:428` (`furniture#width`), which does
  not load the taxonomy. The frontends contain no `taxonomy.sirktek.no/` string outside tests. The
  one IRI reader is orgadmin's namespace routing (D4).
- **`controlChecklist`** (PO OK 2026-10-15): one `common:controlChecklist` with range
  `common:ChecklistTemplateEntry`, label "Checklist"@en, "Sjekkliste"@no, and comment "The checks
  to perform on the asset on each visit of a control round: one ChecklistTemplate v1 (see
  ChecklistTemplateEntry). Set on a category it is the default for every asset in that category;
  set on an asset it replaces the category default"@en. Vardly resolves the checklist by the key
  `controlChecklist` (machine-base.ttl:334-338 comment), which is unchanged.

### D6 — Releases and version bumps

Every merge to `main` tags `3.<run>` and publishes to GitHub Packages (commons README
"Releases"). orgadmin-backend and mcp-server resolve the taxonomies from GitHub Packages
(`pom.xml` repositories `github-taxonomy-commons`, `github-*-taxonomy`). Maven Central is manual
and only needed for a Central consumer. The only one is sirk-app-backend, which pins
`taxonomy-commons` 3.10 and `furniture-taxonomy` 3.20 and uses only class names
(`CategoryNameResolver`), never properties. It needs no bump, so **no Maven Central publish**.

Order:

1. **sirk-taxonomy-commons:** the `checklists` change (marker `ChecklistTemplateEntry`), then this
   change's Group 1. The domains bump once, to the tag that carries both.
2. **Four domains, one merge each, in any order:** bump `taxonomy-commons`, delete their own
   declarations and opt in. Logistics and machine fold in their `checklists` slice (D7).
3. **sirk-orgadmin-backend: one commit** bumping `taxonomy-commons` and all four domains together.
   Today it pins 3.14 / furniture 3.23 / logistics 3.24 / machine 3.17 / ict 3.6
   (`pom.xml:214-240`). It must land with or after its own `checklists` change, because the new
   `CommonPropertyDefinition.PropertyType.CHECKLIST` breaks the exhaustive switch at
   `CategoryPropertyInheritanceService.java:718-727`.
4. **sirk-mcp-server: one commit** bumping the five `*.version` properties (`pom.xml:30-34`). No
   code change: it serves the TTLs as text, and the domain TTLs then point to the commons
   definitions it also serves.

Nothing else pins these artifacts (grep of `pom.xml` under ~/dev:
`sirk-logistics-taxonomy-backup` is a stale copy and not built).

### D7 — Sequencing with the open `checklists` changes

- Commons `checklists` adds `common:ChecklistTemplateEntry` and `CHECKLIST`. This change only
  uses that marker as a range. It does not redo the marker or the mapping, and it runs after
  commons `checklists` Group 1.
- The logistics and machine `checklists` slices change the range of their own `controlChecklist`.
  Groups 4 and 5 here replace those slices: the declaration is deleted, and the
  opt-in reaches `common:controlChecklist`, which already has the marker range. The slices'
  test assertions (range and `CHECKLIST` type) move into Groups 4 and 5. Archive those slices as
  superseded.
  Each domain releases once.
- The commons `checklists` proposal's non-goal "a shared `common:controlChecklist`" and its
  2026-10-15 "no" are reversed by the PO the same day; that proposal records the reversal and
  points here.

## Commons TTL (Group 1)

A new section after `common:resourceVariant`:

```turtle
# -------------------------------------------------------------------
# Shared asset properties
#
# Physical and identifying properties that several domains use on their
# asset classes. Declared once, with no domain; each domain opts its
# classes in with `schema:domainIncludes` in its own ttl. Keys are the
# local names, so stored values do not depend on the namespace.
# -------------------------------------------------------------------

common:model a rdf:Property ;
    rdfs:label "Model"@en, "Modell"@no ;
    rdfs:range common:Model .

common:serialNumber a rdf:Property ;
    rdfs:label "Serial Number"@en, "Serienummer"@no ;
    rdfs:range xsd:string .

common:variant a rdf:Property ;
    rdfs:label "Variant"@en, "Variant"@no ;
    rdfs:range xsd:string .

common:color a rdf:Property ;
    rdfs:label "Color"@en, "Farge"@no ;
    rdfs:range xsd:string .

common:material a rdf:Property ;
    rdfs:label "Material"@en, "Materiale"@no ;
    rdfs:range xsd:string .

common:operationsAndMaintenanceManual a rdf:Property ;
    rdfs:label "Operations and Maintenance Manual"@en, "Drift- og vedlikeholdshåndbok"@no ;
    rdfs:range xsd:anyURI .

common:length a rdf:Property ;
    rdfs:label "Length (cm)"@en, "Lengde (cm)"@no ;
    rdfs:range xsd:decimal .

common:width a rdf:Property ;
    rdfs:label "Width (cm)"@en, "Bredde (cm)"@no ;
    rdfs:range xsd:decimal .

common:height a rdf:Property ;
    rdfs:label "Height (cm)"@en, "Høyde (cm)"@no ;
    rdfs:range xsd:decimal .

common:weight a rdf:Property ;
    rdfs:label "Weight (kg)"@en, "Vekt (kg)"@no ;
    rdfs:comment "The weight in kilograms"@en ;
    rdfs:range xsd:decimal .

common:volume a rdf:Property ;
    rdfs:label "Volume (m³)"@en, "Volum (m³)"@no ;
    rdfs:comment "The volume in cubic meters"@en ;
    rdfs:range xsd:decimal .
```

Also (D5 text):

```turtle
common:controlChecklist a rdf:Property ;
    rdfs:label "Checklist"@en, "Sjekkliste"@no ;
    rdfs:comment "<D5 comment>"@en ;
    rdfs:range common:ChecklistTemplateEntry .
```

`common:manufacturer` becomes:

```turtle
common:manufacturer a rdf:Property ;
    rdfs:label "Manufacturer"@en, "Produsent"@no ;
    skos:altLabel "Vendor"@en ;
    schema:domainIncludes common:Model ;
    rdfs:range common:Manufacturer .
```

`common:Model` keeps `manufacturer` in the commons tree (`hasDomain` accepts `domainIncludes`), so
`CommonTaxonomyServiceTest.modelHasExpectedProperties` still holds.

## Domain edits (Groups 2–5)

Each domain deletes the listed declarations (D2 table lines) and adds, next to its existing
opt-ins:

- **furniture:** `common:<n> schema:domainIncludes furniture:Furniture .` for the eleven names.
- **ict:** `common:manufacturer` and `common:model` `schema:domainIncludes ict:Hardware, ict:Software`;
  `common:serialNumber schema:domainIncludes ict:Hardware`.
- **logistics:** `common:weight`, `common:volume` `schema:domainIncludes logistics:Freight`; and
  `common:controlChecklist schema:domainIncludes logistics:ControlPoint`.
- **machine:** the eleven names and `common:manufacturer` `schema:domainIncludes machine:Machine`;
  `common:controlChecklist schema:domainIncludes machine:Machine`, and delete the
  "until commons has a shared controlChecklist term" comment (machine-base.ttl:334-338).

Each domain README gets one line in its opt-in paragraph that names the consolidated properties.

## Risks

- **Version skew in a consumer:** orgadmin and mcp-server pin `taxonomy-commons` directly, so
  Maven nearest-wins applies. A new domain jar on an old commons jar opts into `common:weight`,
  which is undeclared there (no `rdf:type rdf:Property`). The property then disappears from the
  class (`getPropertiesForClass` lists only typed properties). Mitigation: D6 step 3, all five
  versions in one commit.
- **Freight weight/volume type:** reported as `DECIMAL_KG`/`DECIMAL_M3` (D4); PO-approved
  2026-10-15.
