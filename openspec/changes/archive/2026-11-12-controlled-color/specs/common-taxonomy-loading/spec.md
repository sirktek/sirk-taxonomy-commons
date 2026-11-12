## MODIFIED Requirements

### Requirement: Shared asset properties are declared once in commons

Commons SHALL declare `common:material`, `common:variant`, `common:serialNumber`
(`xsd:string`), `common:color` (range `common:Color`), `common:model` (range `common:Model`),
`common:operationsAndMaintenanceManual` (`xsd:anyURI`), `common:length`, `common:width`,
`common:height`, `common:weight` and `common:volume` (`xsd:decimal`), each with an English and a
Norwegian label and without `rdfs:domain` or `schema:domainIncludes`. Commons SHALL declare
`common:controlChecklist` with range `common:ChecklistTemplateEntry`, labels "Checklist"@en and
"Sjekkliste"@no, and no domain. `common:manufacturer` SHALL name `common:Model` with
`schema:domainIncludes`, not `rdfs:domain`. A domain taxonomy SHALL attach these properties to its
classes with `schema:domainIncludes` and SHALL NOT declare a property with the same local name.

#### Scenario: Shared property declaration
- **WHEN** the commons model is read
- **THEN** `common:weight` has range `xsd:decimal`, labels "Weight (kg)"@en and "Vekt (kg)"@no, and neither `rdfs:domain` nor `schema:domainIncludes`

#### Scenario: Colour is declared with an enumeration range
- **WHEN** the commons model is read
- **THEN** `common:color` has range `common:Color`, labels "Color"@en and "Farge"@no, and neither `rdfs:domain` nor `schema:domainIncludes`

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

## ADDED Requirements

### Requirement: Colour is ISCC–NBS Level 1 plus three extras

Commons SHALL declare the class `common:Color`, whose `rdfs:comment` cites NIST Special
Publication 440, with exactly sixteen individuals: the thirteen ISCC–NBS Level 1 categories in
their published order, then `Metallic`, `Transparent` and `Multicolored`, each with an English and a Norwegian
label and a two-digit zero-padded `skos:notation` "01" to "16". `Metallic`, `Transparent` and
`Multicolored` SHALL each state in `rdfs:comment` that it is not an ISCC–NBS category.

#### Scenario: The sixteen colours in ISCC–NBS order
- **WHEN** the loader returns the enumeration values of `common:Color`
- **THEN** their names are, in order, `Pink`, `Red`, `Orange`, `Brown`, `Yellow`, `Olive`, `YellowGreen`, `Green`, `Blue`, `Purple`, `White`, `Gray`, `Black`, `Metallic`, `Transparent`, `Multicolored`

#### Scenario: Norwegian labels
- **WHEN** the loader returns the enumeration value `YellowGreen`
- **THEN** its English label is "Yellow green" and its Norwegian label is "Gulgrønn"

#### Scenario: Norwegian labels of all sixteen
- **WHEN** the loader returns the enumeration values of `common:Color`
- **THEN** their Norwegian labels are, in order, Rosa, Rød, Oransje, Brun, Gul, Oliven, Gulgrønn, Grønn, Blå, Lilla, Hvit, Grå, Svart, Metallisk, Gjennomsiktig, Flerfarget

#### Scenario: Extras are marked as not ISCC–NBS
- **WHEN** the commons model is read
- **THEN** `common:Metallic`, `common:Transparent` and `common:Multicolored` each have an `rdfs:comment` containing "Not an ISCC–NBS category", and no other colour individual has one

#### Scenario: Colour values reach a domain class that opts in
- **WHEN** a domain Turtle file states `common:color schema:domainIncludes d:TestBuilding` and its loader loads it
- **THEN** `TestBuilding` has exactly one property named `color`, with range `common:Color`, whose enumeration values are the sixteen colours

#### Scenario: Values are local names
- **WHEN** a client stores a colour chosen from those enumeration values
- **THEN** the stored value is the individual's local name, such as `YellowGreen`, and values stored before this change are not rewritten

### Requirement: Colour values carry a swatch display hint

Commons SHALL declare `common:swatch` (range `xsd:string`, labels "Swatch"@en and
"Fargeprøve"@no, no domain) with an `rdfs:comment` stating that it is a display hint, not data.
Every colour except `Transparent` and `Multicolored` SHALL carry one `common:swatch` of
the form `#rrggbb`; those two SHALL carry none. The loader SHALL expose it in the enumeration value's
attributes under `swatch`, with no code specific to colour.

#### Scenario: Swatch reaches the enumeration attributes
- **WHEN** the loader returns the enumeration value `Black`
- **THEN** its attributes contain `swatch` = "#222222"

#### Scenario: Transparent and Multicolored have no swatch
- **WHEN** the loader returns the enumeration values `Transparent` and `Multicolored`
- **THEN** neither has a `swatch` key in its attributes

#### Scenario: Every other swatch is a hex colour
- **WHEN** the loader returns the enumeration values of `common:Color`
- **THEN** each value other than `Transparent` and `Multicolored` has a `swatch` attribute matching `^#[0-9a-f]{6}$`

#### Scenario: Swatch is not a category property
- **WHEN** the commons taxonomy is loaded
- **THEN** no commons category has a property named `swatch`
