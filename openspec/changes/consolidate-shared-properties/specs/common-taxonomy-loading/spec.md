## ADDED Requirements

### Requirement: Shared asset properties are declared once in commons

Commons SHALL declare `common:color`, `common:material`, `common:variant`, `common:serialNumber`
(`xsd:string`), `common:model` (range `common:Model`), `common:operationsAndMaintenanceManual`
(`xsd:anyURI`), `common:length`, `common:width`, `common:height`, `common:weight` and
`common:volume` (`xsd:decimal`), each with an English and a Norwegian label and without
`rdfs:domain` or `schema:domainIncludes`. `common:manufacturer` SHALL name `common:Model` with
`schema:domainIncludes`, not `rdfs:domain`. A domain taxonomy SHALL attach these properties to its
classes with `schema:domainIncludes` and SHALL NOT declare a property with the same local name.

#### Scenario: Shared property declaration
- **WHEN** the commons model is read
- **THEN** `common:weight` has range `xsd:decimal`, labels "Weight (kg)"@en and "Vekt (kg)"@no, and neither `rdfs:domain` nor `schema:domainIncludes`

#### Scenario: Shared property attaches without a key clash
- **WHEN** a domain Turtle file states `common:weight schema:domainIncludes d:TestBuilding` and its loader loads it
- **THEN** `TestBuilding` has exactly one property named `weight`, with `uri` `http://taxonomy.sirktek.no/common#weight`

#### Scenario: Manufacturer stays on the commons Model class
- **WHEN** the commons taxonomy is loaded
- **THEN** the category `Model` has a property `manufacturer` with range `common:Manufacturer`, and `common:manufacturer` has no `rdfs:domain`

#### Scenario: Shared properties attach nowhere by themselves
- **WHEN** the commons taxonomy is loaded
- **THEN** no commons category has a property named `weight`, `color` or `serialNumber`
