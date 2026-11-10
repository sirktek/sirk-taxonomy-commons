## MODIFIED Requirements

### Requirement: Commercial terms live in commons

Commons SHALL declare `common:marketValue` (`xsd:decimal`), `common:marketValueDate` (`xsd:date`),
`common:monthlyRentExVAT` (`xsd:decimal`) and `common:monthlyServiceChargeExVAT` (`xsd:decimal`),
each with English and Norwegian labels, `common:privateSet "commercial"` and without a domain.
Commons SHALL NOT declare `common:monthlyRent` or `common:monthlyServiceCharge`.

#### Scenario: Commercial term declaration
- **WHEN** the commons model is read
- **THEN** `common:monthlyRentExVAT` has range `xsd:decimal`, labels "Monthly rent (excl. VAT)"@en and "Månedsleie (eks. mva)"@no, `common:privateSet` "commercial", and neither `rdfs:domain` nor `schema:domainIncludes`

#### Scenario: Old rent names are gone
- **WHEN** the commons model is read
- **THEN** `common:monthlyRent` and `common:monthlyServiceCharge` have no `rdf:type`

## ADDED Requirements

### Requirement: New price is a commercial term on Model

Commons SHALL declare `common:newPriceExVAT` with range `xsd:decimal`, labels
"New price (excl. VAT)"@en and "Nypris (eks. mva)"@no, `common:privateSet "commercial"`,
`schema:domainIncludes common:Model` and no `rdfs:domain`. Domains SHALL attach it to their own
classes with `schema:domainIncludes`.

#### Scenario: New price declaration
- **WHEN** the commons model is read
- **THEN** `common:newPriceExVAT` has range `xsd:decimal`, labels "New price (excl. VAT)"@en and "Nypris (eks. mva)"@no, `common:privateSet` "commercial", `schema:domainIncludes` `common:Model`, and no `rdfs:domain`

#### Scenario: New price on the commons Model class
- **WHEN** the commons taxonomy is loaded
- **THEN** the category `Model` has a property `newPriceExVAT` with `privateSet` `commercial`

### Requirement: Item age properties are declared once in commons

Commons SHALL declare `common:yearOfManufacture` (labels "Year of manufacture"@en,
"Produksjonsår"@no) and `common:installationYear` (labels "Installation year"@en,
"Installasjonsår"@no), each with range `xsd:integer`, without `rdfs:domain`,
`schema:domainIncludes` or `common:privateSet`. A domain taxonomy SHALL attach them with
`schema:domainIncludes` and SHALL NOT declare its own property for the year an item was made.

#### Scenario: Age property declaration
- **WHEN** the commons model is read
- **THEN** `common:yearOfManufacture` and `common:installationYear` have range `xsd:integer`, the labels above, and none of `rdfs:domain`, `schema:domainIncludes` or `common:privateSet`

#### Scenario: Age properties attach nowhere by themselves
- **WHEN** the commons taxonomy is loaded
- **THEN** no commons category has a property named `yearOfManufacture` or `installationYear`

### Requirement: Year of manufacture has the building wording as an alternative label

Commons SHALL give `common:yearOfManufacture` the `skos:altLabel` values "Built year"@en and
"Byggeår"@no, as `common:manufacturer` carries "Vendor"@en.


#### Scenario: Building wording as an alternative label
- **WHEN** the commons model is read
- **THEN** `common:yearOfManufacture` has `skos:altLabel` "Built year"@en and "Byggeår"@no
