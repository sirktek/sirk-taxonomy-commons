# common-taxonomy-loading Specification

## Purpose
How domain taxonomies load common-base.ttl: which commons properties a domain class gains, and which shared terms are declared once in commons instead of per domain.
## Requirements
### Requirement: Domain classes gain commons properties they opt into

A loader extending `RdfsTaxonomyLoader` SHALL load `/taxonomy/common-base.ttl` together with its
own Turtle file and SHALL attach to each of its classes every property, from any namespace, whose
`rdfs:domain` or `schema:domainIncludes` names that class. The tree SHALL contain only classes of
the loader's own namespace. A property's `name` SHALL be the local name of its IRI and its `uri`
the full IRI.

#### Scenario: Opted-in commons property attaches
- **WHEN** a domain Turtle file states `common:marketValue schema:domainIncludes d:TestBuilding` and its loader loads it
- **THEN** `TestBuilding` has a property with `name` `marketValue` and `uri` `http://taxonomy.sirktek.no/common#marketValue`, next to its own properties

#### Scenario: Commons property not opted in stays absent
- **WHEN** the same domain file does not opt `TestBuilding` into `common:ledgerAccount`
- **THEN** `TestBuilding` has no property `ledgerAccount`

#### Scenario: Commons classes do not leak into a domain tree
- **WHEN** a domain loader loads its taxonomy
- **THEN** no category of the tree has a URI in the commons namespace

#### Scenario: Commons enumeration values reach a domain class
- **WHEN** a domain class opts into `common:depreciationGroup`
- **THEN** that property's `enumValues` are the ten saldogrupper with notations `a` to `j`, each with `jurisdiction` `NO`, and `DepreciationGroupH` has attribute `depreciationRate` `4`

### Requirement: Properties carry their private set

The loader SHALL set `PropertyDefinition.privateSet` to the string value of the property's
`common:privateSet` annotation, and to `null` when the property has none.

#### Scenario: Annotated and plain properties
- **WHEN** a domain class has `common:valuations`, `common:marketValue` and an own property without annotation
- **THEN** their `privateSet` is `accounting`, `commercial` and `null` respectively

### Requirement: Accounting vocabulary uses ledger17 names

Commons SHALL declare `common:ledgerAccount` (`xsd:string`), `common:depreciationGroup` (range
`common:DepreciationGroup`) and `common:valuations` (range `common:AssetValuationEntry`), each
with `common:privateSet "accounting"` and without a domain. `CommonPropertyDefinition` SHALL map
the range `common:AssetValuationEntry` to `PropertyType.ASSET_VALUATIONS`, a list of entries
`{year, bookValue, taxValue, wealthValue}`. Commons SHALL NOT declare `common:bookValue`,
`common:taxValue`, `common:wealthTaxValue` or `common:AssetValueEntry`, and `PropertyType` SHALL
have no `ASSET_VALUE`.

#### Scenario: Valuations type
- **WHEN** `CommonPropertyDefinition.getPropertyType` is called for `common:valuations`
- **THEN** it returns `ASSET_VALUATIONS`

#### Scenario: Separate yearly value properties are gone
- **WHEN** the commons model is read
- **THEN** `common:bookValue`, `common:taxValue`, `common:wealthTaxValue` and `common:AssetValueEntry` have no `rdf:type`

### Requirement: Commercial terms live in commons

Commons SHALL declare `common:marketValue` (`xsd:decimal`), `common:marketValueDate` (`xsd:date`),
`common:monthlyRent` (`xsd:decimal`) and `common:monthlyServiceCharge` (`xsd:decimal`), each with
English and Norwegian labels, `common:privateSet "commercial"` and without a domain.

#### Scenario: Commercial term declaration
- **WHEN** the commons model is read
- **THEN** `common:monthlyRent` has range `xsd:decimal`, labels "Monthly rent (excl. VAT)"@en and "Månedsleie (eks. mva)"@no, `common:privateSet` "commercial", and neither `rdfs:domain` nor `schema:domainIncludes`

### Requirement: Shared asset properties are declared once in commons

Commons SHALL declare `common:color`, `common:material`, `common:variant`, `common:serialNumber`
(`xsd:string`), `common:model` (range `common:Model`), `common:operationsAndMaintenanceManual`
(`xsd:anyURI`), `common:length`, `common:width`, `common:height`, `common:weight` and
`common:volume` (`xsd:decimal`), each with an English and a Norwegian label and without
`rdfs:domain` or `schema:domainIncludes`. Commons SHALL declare `common:controlChecklist` with
range `common:ChecklistTemplateEntry`, labels "Checklist"@en and "Sjekkliste"@no, and no domain.
`common:manufacturer` SHALL name `common:Model` with
`schema:domainIncludes`, not `rdfs:domain`. A domain taxonomy SHALL attach these properties to its
classes with `schema:domainIncludes` and SHALL NOT declare a property with the same local name.

#### Scenario: Shared property declaration
- **WHEN** the commons model is read
- **THEN** `common:weight` has range `xsd:decimal`, labels "Weight (kg)"@en and "Vekt (kg)"@no, and neither `rdfs:domain` nor `schema:domainIncludes`

#### Scenario: Checklist property declared once in commons
- **WHEN** the commons model is read
- **THEN** `common:controlChecklist` has range `common:ChecklistTemplateEntry`, labels "Checklist"@en and "Sjekkliste"@no, and neither `rdfs:domain` nor `schema:domainIncludes`

#### Scenario: Shared property attaches without a key clash
- **WHEN** a domain Turtle file states `common:weight schema:domainIncludes d:TestBuilding` and its loader loads it
- **THEN** `TestBuilding` has exactly one property named `weight`, with `uri` `http://taxonomy.sirktek.no/common#weight`

#### Scenario: Manufacturer stays on the commons Model class
- **WHEN** the commons taxonomy is loaded
- **THEN** the category `Model` has a property `manufacturer` with range `common:Manufacturer`, and `common:manufacturer` has no `rdfs:domain`

#### Scenario: Shared properties attach nowhere by themselves
- **WHEN** the commons taxonomy is loaded
- **THEN** no commons category has a property named `weight`, `color` or `serialNumber`

