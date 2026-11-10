# Tasks — asset-age-and-new-price

Nine groups, one per repo. Each group is one implementer in a fresh context with this file,
design.md and the delta spec of its repo. In groups 2–9 the repo is a sibling under `~/dev`, and
the implementer reads this change from sirk-taxonomy-commons. Touch only the listed files and run
only the listed tests. Tick the tasks, then append a short `## Handoff — Group N` note (what
changed, what is unverified, the released tag) to `handoff.md` in this directory, and stop when
the tests pass. Maven goes through `mvnq` (`~/.local/bin/mvnq`, same arguments as `./mvnw`);
never `clean`. Frontends: `yarn test --silent 2>&1 | tail -n 40`. No Java/TS production code
except the one Vardly fact key (Group 8) and the `YEAR` deletions (Groups 3 and 6).

Each group starts with a GitHub issue in its repo and the branch
`feature/#<id>-asset-age-and-new-price`, from `origin/main` (the local furniture, machine and ict
checkouts are on `feature/asset-condition`).

Suggested model: sonnet for groups 1, 3 and 6; haiku for groups 2, 4, 5, 7, 8 and 9. Design D1–D2
pin every name, range, label and line.

Order: Group 1 merges first. Groups 2–5 follow in any order. Groups 6 and 7 after all of 2–5
have released. Groups 8 and 9 deploy with or right after Group 6 (design D6).

### Group 1: sirk-taxonomy-commons — declare and rename

Files: `src/main/resources/taxonomy/common-base.ttl`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonTaxonomyServiceTest,DomainOptInLoaderTest'`

- [x] 1.1 Test first. In `commercialTermsAreDeclaredWithRangesLabelsAndPrivateSet` rename the
      `monthlyRent`/`monthlyServiceCharge` terms to `monthlyRentExVAT`/`monthlyServiceChargeExVAT`
      (labels unchanged) and assert `common:monthlyRent` and `common:monthlyServiceCharge` have
      no `rdf:type`. Add `newPriceExVAT` assertions in a separate test
      `newPriceIsACommercialTermOnModel`: range `xsd:decimal`, labels "New price (excl. VAT)"/
      "Nypris (eks. mva)", `privateSet` `commercial`, no `rdfs:domain`, `schema:domainIncludes`
      `common:Model`; and the loaded `Model` category has `newPriceExVAT` with `privateSet`
      `commercial`. In `sharedAssetPropertiesAreDeclaredWithoutDomain` add `yearOfManufacture`
      and `installationYear` (`xsd:integer`, labels per design D1, no domain, no `privateSet`), and
      `yearOfManufacture`'s `skos:altLabel` "Built year"@en / "Byggeår"@no (as for "Vendor").
      In `sharedAssetPropertiesAttachToNoCommonsCategory` add `yearOfManufacture`. See them fail.
- [x] 1.2 `common-base.ttl`: add the three terms and rename the two (design D1), and extend the
      "Commercial terms" header comment.
- [x] 1.3 `README.md`: add the three terms to the "Commercial terms" and "Shared asset
      properties" tables; rename the two rows.
- [x] 1.4 Run the group's tests, then `mvnq test` once, then
      `openspec validate asset-age-and-new-price --strict`. Note the merge tag in the handoff.

### Group 2: sirk-furniture-taxonomy

Files: `src/main/resources/taxonomy/furniture-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`,
`openspec/changes/asset-age-and-new-price/` (delta spec, already written).
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 2.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 2.2 Test first, in `CommonPropertyOptInTest`: `Furniture` carries `yearOfManufacture` and
      `installationYear` once each with `common#` IRIs, range `xsd:integer` and no `privateSet`;
      `newPriceExVAT` once with `privateSet` `commercial`; `valuations` once with `privateSet`
      `accounting`; no property named `productionDate`. Narrow `furnitureHasNoAccountingProperties`
      to `ledgerAccount`, `depreciationGroup`, `bookValue`, `taxValue`, `wealthTaxValue`. See it fail.
- [ ] 2.3 Delete `furniture:productionDate`; add the four opt-ins (design D2) next to the existing
      ones at the end of the file.
- [ ] 2.4 `README.md` opt-in paragraph (~line 92-97): name the four opt-ins.
- [ ] 2.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 3: sirk-machine-taxonomy

Files: `src/main/resources/taxonomy/machine-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`,
`src/main/java/no/sirktek/taxonomy/model/MachinePropertyDefinition.java`,
`src/test/java/no/sirktek/taxonomy/model/PropertyDefinitionTest.java`, `README.md`,
`openspec/changes/asset-age-and-new-price/`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 3.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 3.2 Test first: `machineOptsIntoMonthlyRent` asserts `monthlyRentExVAT`
      (`common#monthlyRentExVAT`, `commercial`) and no `monthlyRent`; `Machine` carries
      `yearOfManufacture` once with IRI `common#yearOfManufacture` (not `machine#`) and
      `installationYear` once, both `xsd:integer`, no `privateSet`. In `PropertyDefinitionTest`
      the hand-built `yearOfManufacture` case (`:224`) now expects `INTEGER`, and the
      `valueOf("YEAR")` assertion (`:365`) is deleted. See it fail.
- [ ] 3.3 Delete `machine:yearOfManufacture` (and the then-empty "Property Definitions - Common"
      header); rename the `monthlyRent` opt-in; add the two opt-ins in "Cross-cutting property
      domain extensions".
- [ ] 3.3a `MachinePropertyDefinition`: delete the `yearOfManufacture` → `YEAR` line (`:53`) and the
      `YEAR` constant (`:86`); grep the repo for `YEAR` first, nothing else may name it (design D3).
- [ ] 3.4 `README.md` (~line 89): `monthlyRentExVAT`, and the two age opt-ins.
- [ ] 3.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 4: sirk-logistics-taxonomy

Files: `src/main/resources/taxonomy/logistics-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`,
`openspec/changes/asset-age-and-new-price/`.
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 4.1 Bump `taxonomy-commons` (3.19 today) to Group 1's tag.
- [ ] 4.2 Test first: `COMMERCIAL` becomes `marketValue`, `marketValueDate`, `monthlyRentExVAT`,
      `monthlyServiceChargeExVAT`; `REAL_ESTATE_OWN` loses `builtYear`; `RealEstate` additionally
      carries `yearOfManufacture` and `installationYear` once each with `common#` IRIs, range
      `xsd:integer`, no `privateSet`; `OperatedProperty` carries neither. See it fail.
- [ ] 4.3 Delete `logistics:builtYear`; add the two opt-ins on `RealEstate`; rename the two
      commercial opt-ins (design D2).
- [ ] 4.4 `README.md` table (~line 108-109): new names; add the two age opt-ins.
- [ ] 4.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 5: sirk-ict-taxonomy

Files: `src/main/resources/taxonomy/ict-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version, 3.24 today), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`,
`README.md`, `openspec/changes/asset-age-and-new-price/` (delta spec, already written).
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 5.1 Bump `taxonomy-commons` to Group 1's tag.
- [ ] 5.2 Test first: add `newPriceExVAT`, `yearOfManufacture` and `installationYear` to the allowed commons names in
      `noIctClassCarriesACommonsPropertyOtherThanTheSharedOnes`; `Hardware` carries
      `newPriceExVAT` once with IRI `common#newPriceExVAT` and `privateSet` `commercial`, and
      `yearOfManufacture` and `installationYear` once each with `common#` IRIs, `xsd:integer`, no
      `privateSet`; `Software` carries `newPriceExVAT` and `installationYear` once each (same
      checks) and no `yearOfManufacture`. See it fail.
- [ ] 5.3 `ict-base.ttl`: add `common:newPriceExVAT` and `common:installationYear`
      `schema:domainIncludes ict:Hardware, ict:Software` next to `manufacturer`/`model`, and
      `common:yearOfManufacture schema:domainIncludes ict:Hardware` after the
      `serialNumber`/`condition` opt-ins (design D2).
- [ ] 5.4 `README.md` (~line 26): name the three opt-ins and their classes.
- [ ] 5.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 6: sirk-orgadmin-backend — bump five in one commit

Prerequisite: Groups 2–5 released.

Files: `pom.xml` (only the commons, furniture, logistics, machine and ict versions, ~lines 214-240),
`src/main/java/no/sirktek/orgadmin/service/category/CategoryPropertyInheritanceService.java` (only `case YEAR`, `:974`),
`src/test/java/no/sirktek/orgadmin/service/category/CategoryInheritancePinTest.java`,
`src/test/java/no/sirktek/orgadmin/service/category/PrivatePropertyTreeIT.java`.
Tests: `mvnq test -Dtest='CategoryPropertyInheritanceServiceTest,CategoryInheritancePinTest,CategoryServiceTest'`
and `mvnq -DskipITs=false verify -Dit.test=PrivatePropertyTreeIT -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] 6.1 Set the five versions to the tags from Groups 1–5 and delete `case YEAR ->
      PropertyType.INTEGER` (it no longer compiles against the new machine jar), in one commit.
- [ ] 6.2 `CategoryInheritancePinTest`: `productionDate`/`DATE`/"Production Date" becomes
      `yearOfManufacture`/`INTEGER`/"Year of manufacture"; add `installationYear` (`INTEGER`),
      `newPriceExVAT` (`DECIMAL`, `privateSet` `commercial`) and `valuations`
      (`ASSET_VALUATIONS`, `privateSet` `accounting`). `globalFurnitureHasExactlyTheSixteenDefinitions`
      becomes nineteen; its "no `privateSet`" and "no `valuations`" assertions narrow to the
      remaining accounting names. Expected changes only; no Java production change.
- [ ] 6.3 `PrivatePropertyTreeIT`: rename `monthlyRent`/`monthlyServiceCharge` to the `ExVAT`
      names.
- [ ] 6.4 Run the tests above, then `mvnq -DskipITs=false verify` and `./mvnw clean -U pmd:check`
      once.

### Group 7: sirk-mcp-server — bump five in one commit

Prerequisite: Groups 2–5 released.

Files: `pom.xml` (only the `taxonomy-commons`, furniture, logistics and machine `*.version`
properties, lines ~30-34).
Tests: `mvnq test`

- [ ] 7.1 Set the five versions to the tags from Groups 1–5, in one commit.
- [ ] 7.2 Run `mvnq test` once.

### Group 8: vardly-frontend — follow the renamed keys

Files: `src/helpers/propertyFacts.ts` (only `FACT_KEYS`), `src/i18n/en.json`, `src/i18n/no.json`,
and the tests and MSW fixtures that name `builtYear`, `monthlyRent` or `monthlyServiceCharge`
(design D4: `tests/msw/fixtures.ts`, `tests/unit/helpers/propertyFacts.test.ts`,
`tests/unit/api/*`, `tests/component/**`, `tests/e2e/asset-editing.spec.ts`).
Tests: `yarn test --silent 2>&1 | tail -n 40`

- [ ] 8.1 Rename fixtures and test expectations first; see them fail.
- [ ] 8.2 `FACT_KEYS` `'builtYear'` → `'yearOfManufacture'`; rename the i18n keys per design D5,
      keeping the "Built year"/"Byggeår" wording.
- [ ] 8.3 Run the tests. Deploy with or right after Group 6.

### Group 9: sirk-asset-frontend — follow the renamed keys

Files: `src/i18n/en.json`, `src/i18n/no.json`, and tests naming `builtYear`, `monthlyRent` or
`monthlyServiceCharge` (`tests/unit/hooks/usePrivateProperties.test.tsx`,
`tests/unit/helpers/assetPropertySchema.test.ts`,
`tests/unit/pages/AssetDetailPage.privateProperties.test.tsx`).
Tests: `yarn test --silent 2>&1 | tail -n 40`

- [ ] 9.1 Rename the i18n keys per design D5 (`productionDate` → `yearOfManufacture` with
      "Year of manufacture"/"Produksjonsår"; the two commercial labels to the `ExVAT` names).
- [ ] 9.2 Rename the test fixtures (`builtYear` → `yearOfManufacture`, `monthlyRent` →
      `monthlyRentExVAT`). Run the tests. Deploy with or right after Group 6.

## Not in scope

The "NOK saved" calculation; sirk-app-backend, sirk-orgadmin-frontend, sirk-frontend-commons,
sirk-service-backend and ledger17 (no reference outside hand-built test fixtures, design D4);
`yearOfManufacture` on `ict:Software`.
