# Tasks — consolidate-shared-properties

Seven groups, one per repo. Each group is one implementer in a fresh context with this file,
design.md and `specs/common-taxonomy-loading/spec.md`. In groups 2–7 the repo is a sibling under
`~/dev`, and the implementer reads this change from sirk-taxonomy-commons. Touch only the listed
files and run only the listed tests. Tick the tasks, then append a short `## Handoff — Group N`
note (what changed, what is unverified, the released tag) to `handoff.md` in this directory, and
stop when the tests pass. Maven goes through `mvnq` (`~/.local/bin/mvnq`, same arguments as
`./mvnw`); never `clean`. No Java production code in any group.

Each group starts with a GitHub issue in its repo and the branch
`feature/#<id>-consolidate-shared-properties`.

Suggested model: haiku for groups 2, 3, 6 and 7; sonnet for groups 1, 4 and 5. Design D2 pins
every name, range and label, and D7 covers the `checklists` interplay.

Order: Group 1 merges first. Groups 2–5 follow in any order, each one merge. Groups 6 and 7
come last, after all of Groups 2–5 have released.

**Tasks marked (PO)** depend on the `controlChecklist` OK (proposal open question 1). Without it,
skip them and follow design D7's fallback.

### Group 1: sirk-taxonomy-commons — declare the shared terms

Prerequisite: commons `checklists` Group 1 merged (`common:ChecklistTemplateEntry` exists).

Files: `src/main/resources/taxonomy/common-base.ttl`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java`,
`src/test/resources/taxonomy/test-domain-optin.ttl`,
`src/test/java/no/sirktek/taxonomy/loader/DomainOptInLoaderTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonTaxonomyServiceTest,DomainOptInLoaderTest'`

- [ ] 1.1 Test first. `CommonTaxonomyServiceTest`: add `sharedAssetPropertiesAreDeclaredWithoutDomain`,
      in the style of `commercialTermsAreDeclaredWithRangesLabelsAndPrivateSet`. It covers the
      eleven names, their ranges and en/no labels (design D2 "Commons term" column), no
      `rdfs:domain`, no `schema:domainIncludes` and no `privateSet`. Assert that
      `common:manufacturer` has `skos:altLabel "Vendor"@en` and `schema:domainIncludes
      common:Model` but no `rdfs:domain`. Assert that no commons category has `weight`, `color`
      or `serialNumber`. `modelHasExpectedProperties` stays as is. Fixture: add
      `common:weight schema:domainIncludes testdomain:TestBuilding .`. `DomainOptInLoaderTest`:
      add `weight` to the expected key set (`toMap` fails on a duplicate key) and assert its `uri`
      is `common#weight`. See them fail.
- [ ] 1.2 `common-base.ttl`: add the "Shared asset properties" section (design "Commons TTL")
      after `common:resourceVariant`, and rewrite `common:manufacturer` as shown there.
- [ ] 1.3 (PO) Add `common:controlChecklist` (design D5 text) to the same section. In 1.1, assert
      its range `common:ChecklistTemplateEntry`, its labels "Checklist"/"Sjekkliste" and no domain.
- [ ] 1.4 `README.md`: a "Shared asset properties" table (property, label (no), range) next to
      "Commercial terms". Note that domains opt in and that `address` stays per domain (D3).
- [ ] 1.5 Run the group's tests, then `mvnq test` once, then
      `openspec validate consolidate-shared-properties --strict`. Note the merge tag in the handoff.

### Group 2: sirk-furniture-taxonomy

Files: `src/main/resources/taxonomy/furniture-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 2.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 2.2 Test first, in `CommonPropertyOptInTest`: `Furniture` carries each of the eleven names
      exactly once, with `uri` `http://taxonomy.sirktek.no/common#<name>`, and no property has a
      `furniture#` IRI with one of those names. See it fail.
- [ ] 2.3 Delete the eleven `furniture:*` declarations (design D2 lines). That includes
      `operationsAndMaintenanceManual`'s inert `domainIncludes common:Model`. Next to the
      existing opt-ins at the end of the file, add
      `common:<name> schema:domainIncludes furniture:Furniture .` for each name.
- [ ] 2.4 `README.md`: one line naming the shared properties in the opt-in paragraph (~line 93).
- [ ] 2.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 3: sirk-ict-taxonomy

Files: `src/main/resources/taxonomy/ict-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 3.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 3.2 Test first: `Hardware` and `Software` each carry `manufacturer` and `model` once, with
      `common#` IRIs. `Hardware` carries `serialNumber` once (`common#serialNumber`), and
      `Software` does not carry it. See it fail.
- [ ] 3.3 Delete `ict:manufacturer`, `ict:model` and `ict:serialNumber` (ict-base.ttl:134, 141,
      157). Add `common:manufacturer` and `common:model` `schema:domainIncludes ict:Hardware,
      ict:Software`, and `common:serialNumber schema:domainIncludes ict:Hardware`.
- [ ] 3.4 `README.md` line ~26: the shared properties come from commons; `purchaseDate` stays ict.
- [ ] 3.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 4: sirk-logistics-taxonomy

Files: `src/main/resources/taxonomy/logistics-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`,
`src/test/java/no/sirktek/taxonomy/TaxonomyServiceTest.java` (only with 4.3), `README.md`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 4.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 4.2 Test first: `Freight` carries `weight` and `volume` once each, with `common#` IRIs and
      labels "Weight (kg)"/"Vekt (kg)" and "Volume (m³)"/"Volum (m³)". `address` on `Location`
      keeps the IRI `logistics#address`. Delete `logistics:weight` and `logistics:volume`
      (logistics-base.ttl:371, 377). Add `common:weight` and `common:volume`
      `schema:domainIncludes logistics:Freight` next to the existing opt-ins (~line 226).
- [ ] 4.3 (PO) Replaces this repo's `checklists` slice. In `TaxonomyServiceTest` (~129-137) and
      the opt-in test, `ControlPoint` carries `controlChecklist` once, with IRI
      `common#controlChecklist`, range `common#ChecklistTemplateEntry`, labels unchanged, and
      `CommonPropertyDefinition.getPropertyType` = `CHECKLIST`. Delete `logistics:controlChecklist`
      (line 260) and add `common:controlChecklist schema:domainIncludes logistics:ControlPoint`.
      Archive `openspec/changes/checklists` as superseded.
- [ ] 4.4 `README.md` opt-in paragraph (~line 93): list `weight` and `volume` (and
      `controlChecklist` with 4.3).
- [ ] 4.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 5: sirk-machine-taxonomy

Files: `src/main/resources/taxonomy/machine-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`,
`src/test/java/no/sirktek/taxonomy/ControlChecklistPropertyTest.java` (only with 5.3), `README.md`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,ControlChecklistPropertyTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 5.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 5.2 Test first: `Machine` carries the eleven names and `manufacturer` once each, with
      `common#` IRIs, and no `machine#` IRI for them. `monthlyRent` and the no-accounting
      assertions stay. Delete the twelve `machine:*` declarations (design D2 lines). Add
      `common:<name> schema:domainIncludes machine:Machine .` in the "Cross-cutting property
      domain extensions" section.
- [ ] 5.3 (PO) Replaces this repo's `checklists` slice. `ControlChecklistPropertyTest`: the IRI is
      `common#controlChecklist`, the range `common#ChecklistTemplateEntry`, and
      `CommonPropertyDefinition.getPropertyType` = `CHECKLIST`. Delete `machine:controlChecklist`
      and its "until commons has a shared controlChecklist term" comment (machine-base.ttl:334-345).
      Add the opt-in. Archive `openspec/changes/checklists` as superseded.
- [ ] 5.4 `README.md` opt-in paragraph (~line 90): list the shared properties.
- [ ] 5.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 6: sirk-orgadmin-backend — bump all five in one commit

Prerequisite: Groups 2–5 released. orgadmin's own `checklists` change has its `CHECKLIST` case
in `CategoryPropertyInheritanceService.convertToPropertyType` (design D6 step 3).

Files: `pom.xml` (only the five taxonomy versions, lines ~214-240).
Tests: `mvnq test -Dtest='CategoryPropertyInheritanceServiceTest,CategoryInheritancePinTest,CategoryServiceTest'`

- [ ] 6.1 Set `taxonomy-commons` and the four domain versions to the tags from Groups 1–5, in
      one commit.
- [ ] 6.2 Run the tests above, then the full suite once. A failing type assertion for logistics
      `Freight.weight`/`volume` is expected (`DECIMAL_KG`/`DECIMAL_M3`, design D4). Report it in
      the handoff; do not change Java.

### Group 7: sirk-mcp-server — bump all five in one commit

Prerequisite: Groups 2–5 released.

Files: `pom.xml` (only the `*-taxonomy.version` and `taxonomy-commons.version` properties, lines
~30-34).
Tests: `mvnq test`

- [ ] 7.1 Set the five versions to the tags from Groups 1–5, in one commit.
- [ ] 7.2 Run `mvnq test` once.

## Not in scope

The `address` consolidation (design D3), ledger17-accounting overlaps, and sirk-app-backend: it
uses furniture class names only and needs no bump or Maven Central publish (design D6).
