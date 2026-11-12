## ADDED Requirements

### Requirement: Expected service life is declared once in commons

Commons SHALL declare `common:expectedServiceLife` with range `xsd:integer`, labels
"Expected service life (years)"@en and "Forventet levetid (år)"@no, and none of `rdfs:domain`,
`schema:domainIncludes` or `common:privateSet`. A domain taxonomy SHALL attach it with
`schema:domainIncludes` and SHALL NOT declare its own property with that local name.

#### Scenario: Expected service life declaration
- **WHEN** the commons model is read
- **THEN** `common:expectedServiceLife` has range `xsd:integer`, labels "Expected service life (years)"@en and "Forventet levetid (år)"@no, and none of `rdfs:domain`, `schema:domainIncludes` or `common:privateSet`

#### Scenario: Expected service life attaches nowhere by itself
- **WHEN** the commons taxonomy is loaded
- **THEN** no commons category has a property named `expectedServiceLife`
