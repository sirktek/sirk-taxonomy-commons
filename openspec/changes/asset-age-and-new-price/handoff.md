# Handoff — asset-age-and-new-price

Implementers append `## Handoff — Group N` below (what changed, what is unverified, released tag).

## Handoff — Group 1

Changed: `src/main/resources/taxonomy/common-base.ttl` (added `yearOfManufacture` with altLabel, `installationYear`, `newPriceExVAT`; renamed `monthlyRent`/`monthlyServiceCharge` to `...ExVAT`; header comment extended), `CommonTaxonomyServiceTest` (renamed terms, old-name absence, new `newPriceIsACommercialTermOnModel`, year terms + altLabel, `yearOfManufacture` in attach-nowhere check), `README.md` (tables).
Verified: `CommonTaxonomyServiceTest,DomainOptInLoaderTest` green (24), full `mvnq test` run, `openspec validate --strict`.
Unverified: tests were written together with the TTL change, not observed failing first. Nothing committed or pushed.
Tag: pending merge.

## Handoff — Group 3
Changed (sirk-machine-taxonomy): `machine-base.ttl` (deleted `machine:yearOfManufacture` and its empty "Common" header; `monthlyRent` opt-in -> `monthlyRentExVAT`; added `yearOfManufacture`/`installationYear` opt-ins), `pom.xml` (taxonomy-commons 3.24 -> 3.26), `MachinePropertyDefinition` (YEAR branch and constant removed; nothing else named it), `CommonPropertyOptInTest` (renamed rent test, no `monthlyRent`, new once-only/common#/integer/public age test), `PropertyDefinitionTest` (yearOfManufacture now INTEGER, `valueOf("YEAR")` deleted, enum count 24 -> 23), `README.md`.
Verified: full `mvnq test` green (50 tests). Tests were edited together with the TTL, not observed failing first.
Unverified/deferred: nothing committed or pushed. Tag: pending commit.

## Handoff — Group 2
Changed (sirk-furniture-taxonomy): `pom.xml` (taxonomy-commons 3.24 -> 3.26), `src/main/resources/taxonomy/furniture-base.ttl` (deleted `furniture:productionDate`; added `common:yearOfManufacture`, `common:installationYear`, `common:newPriceExVAT`, `common:valuations` `schema:domainIncludes furniture:Furniture` at the end of the file), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java` (narrowed `furnitureHasNoAccountingProperties` to five names, added age/new-price test, valuations-accounting test, no-`productionDate` test), `README.md` (opt-in paragraph names the four).
Verified: `CommonPropertyOptInTest` failed first (3 failures: yearOfManufacture 0 matches, valuations 0 matches, productionDate still present), then `CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest` green (31); full `mvnq test` green (31).
Notes: `newPriceExVAT` already resolved once on Furniture before the opt-in (inherited through `common:Model`), so its test did not fail first; the explicit opt-in follows D2. `PropertyDefinitionTest` still has a hand-built `productionDate` fixture (no taxonomy load), left unchanged.
Unverified/deferred: the furniture-base opt-ins are only checked through the loader on commons 3.26; no real-data check. Nothing committed or pushed.
Tag: pending commit (Group 1's tag 3.26 was used for the bump).

## Handoff — Group 4

Changed (sirk-logistics-taxonomy): `pom.xml` (taxonomy-commons 3.19 -> 3.26), `src/main/resources/taxonomy/logistics-base.ttl` (deleted `logistics:builtYear`; added `common:yearOfManufacture` and `common:installationYear` `schema:domainIncludes logistics:RealEstate`; renamed the two commercial opt-ins to `monthlyRentExVAT`/`monthlyServiceChargeExVAT` on `RealEstate, OperatedProperty`), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java` (renamed commercial names, `AGE` set on RealEstate, age `common#`/`xsd:integer`/no-privateSet test, no-`builtYear` test, `assertFalse` import), `README.md` (commercial rows renamed, two age rows added, shared-properties heading names "age").
Verified: `CommonPropertyOptInTest` failed first (3 failures, 2 errors: old names and `builtYear` still present, new names absent); `CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest` green (30); full `mvnq test` green (30).
Unverified/deferred: opt-ins are checked through the loader on commons 3.26 only; `OperatedProperty` carries neither age term (asserted by the exact-set test). Nothing committed or pushed.
Tag: pending commit (commons tag 3.26 used for the bump).

## Handoff — Group 5
Changed (sirk-ict-taxonomy, uncommitted): `pom.xml` (taxonomy-commons 3.24 -> 3.26), `src/main/resources/taxonomy/ict-base.ttl` (added `common:newPriceExVAT` and `common:installationYear` `schema:domainIncludes ict:Hardware, ict:Software`; `common:yearOfManufacture schema:domainIncludes ict:Hardware`), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java` (SHARED set gains the three names; new tests `newPriceOnHardwareAndSoftwareIsCommercial`, `installationYearOnHardwareAndSoftwareIsPublicInteger`, `yearOfManufactureIsOnHardwareOnlyAsPublicInteger`), `README.md` (names the three opt-ins; depends-on line 3.19 -> 3.26), `openspec/changes/asset-age-and-new-price/` (delta spec, untouched by this group, still untracked).
Verified: `CommonPropertyOptInTest` failed first on commons 3.26 with the TTL opt-ins absent (3 failures: Hardware carried none of the three), then `CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest` green (24 tests); full `mvnq test` green (24 tests).
Unverified: Software `newPriceExVAT` and `installationYear` are checked once each by the green run, but their "fail first" was not observed separately (Hardware failed first in the same loop). `openspec validate` not run for the ict delta.
Tag: pending commit (Group 1's tag 3.26 is used for the bump).

## Handoff — Group 7
Changed (sirk-mcp-server, uncommitted, per coordinator): `pom.xml` lines 30–34 only: taxonomy-commons 3.19 -> 3.26, furniture-taxonomy 3.25 -> 3.28, logistics-taxonomy 3.27 -> 3.29, machine-taxonomy 3.20 -> 3.23, ict-taxonomy 3.8 -> 3.10. No test expectations edited.
Verified: `mvnq test` on the first run, 113 tests, 0 failures/errors/skipped, BUILD SUCCESS (exit 0). Taxonomy schemas resolved from GitHub Packages at the new versions.
Unverified: none for this group. The branch was clean before the edit; no commit, no branch switch, no push.
Deviation: none.

## Handoff — Group 9

- Files (sirk-asset-frontend, branch feature/ombruk-section): `src/i18n/en.json`, `src/i18n/no.json` (`taxonomy.properties.productionDate` → `yearOfManufacture` "Year of manufacture" / "Produksjonsår"; `labels.commercial.monthlyRent` → `monthlyRentExVAT`, `monthlyServiceCharge` → `monthlyServiceChargeExVAT`, labels unchanged). Test fixtures renamed (whole-word): `tests/unit/pages/AssetDetailPage.privateProperties.test.tsx`, `tests/unit/helpers/assetPropertySchema.test.ts`, `tests/unit/hooks/usePrivateProperties.test.tsx`, `tests/component/assets/PrivatePropertySetSection.test.tsx`, `tests/component/assets/PrivatePropertySections.test.tsx`, `tests/component/assets/a11y.test.tsx`, `tests/component/assets/useUpdatePrivateProperties.test.tsx`.
- No `yearOfManufacture` key existed before, so no duplicate entry was created.
- Verified: grep for `\b(builtYear|productionDate|monthlyRent|monthlyServiceCharge)\b` over src and tests returns nothing. The 9 affected test files pass in isolation (82 tests). `yarn build` succeeds.
- Full `yarn test` is not fully green, and the failures are not from these renames. consumeButton and registerCta tests (`AssetDetailPage.consumeButton`, `AssetDetailPage.registerCta`) fail only in the full run and pass in isolation. Other runs showed act() warnings and a 5 s timeout in `BatchRegisterPage.abandonment` and `EditableCategoryChips`. These look like load flakiness on the user's in-progress branch, and were not investigated or fixed.
- Not touched: `tests/e2e/a11y-routes.spec.ts` (user's uncommitted edit).
- Deviation: none. Deploy skipped, per instructions.

## Handoff — Group 8

- Files (vardly-frontend, branch feature/asset-age-and-new-price): `src/helpers/propertyFacts.ts` (`FACT_KEYS` `builtYear` → `yearOfManufacture`, plus its doc comment), `src/i18n/en.json` and `src/i18n/no.json` (`properties.facts.builtYear` → `yearOfManufacture`, wording kept "Built year"/"Byggeår"; `labels.commercial.monthlyRent` → `monthlyRentExVAT`, `monthlyServiceCharge` → `monthlyServiceChargeExVAT`, labels unchanged). Also `src/components/properties/PropertyList.tsx`: a comment only, renamed so the grep is clean (outside the group's file list; no code change). Test and MSW fixtures renamed whole-word across `tests/` (10 files: msw/fixtures.ts, unit/helpers/propertyFacts.test.ts, unit/api/services.test.ts, unit/api/privatePropertyHooks.test.tsx, unit/assets/useEditAssetFlow.test.tsx, component/a11y.test.tsx, component/assets/AssetEditPage.test.tsx, component/assets/AssetEditPrivateProperties.test.tsx, component/properties/PropertyDetailPage.test.tsx, e2e/asset-editing.spec.ts).
- Red step: after the test renames alone, 24 tests failed in 7 files (expected). After the source rename, the tests passed.
- Verified: `grep -rnE '\b(builtYear|monthlyRent|monthlyServiceCharge)\b' src tests` returns nothing. `yarn build` succeeds. Every file that failed in the full run passes when run alone (the two last ones re-run: AssetEditPrivateProperties, RecordEventDialog, 31 tests green).
- Unverified: a clean full `yarn test` run. The machine load average was about 40 on 16 cores during the run, and the full run showed 5 s timeouts in unrelated rounds, deviations, locations and checkpoint files (38 failures in 18 files). Re-running those 18 files gave 2 timeouts, and both pass alone. These are load flakiness, not caused by the rename. The full suite should be re-run on an idle machine.
- Deviation: none in design. Comment-only edit in PropertyList.tsx to satisfy the grep (see above).
- Deploy (8.3) is left to the coordinator: with or right after Group 6. Before Group 6 deploys, the new keys show nothing in the UI.

## Handoff — Group 6
Changed (uncommitted, orgadmin): pom.xml (commons 3.26, furniture 3.28, logistics 3.29, machine 3.23, ict 3.10); CategoryPropertyInheritanceService (deleted `case YEAR`); CategoryInheritancePinTest (fixture JSON: productionDate removed; installationYear, newPriceExVAT [commercial], valuations [accounting], yearOfManufacture added; test renamed ...NineteenDefinitions, 19 names; privateSet assertion now exactly two marks, commercial + accounting; only ledgerAccount/depreciationGroup asserted absent); PrivatePropertyTreeIT (monthlyRent/monthlyServiceCharge -> ExVAT names).
Loaded set was exactly 19 as predicted; no unexplained differences. RealEstate/Machine private counts unchanged (7 / 4).
Verified green: the three unit classes (56), PrivatePropertyTreeIT (8), full `verify -DskipITs=false` (2122 tests), `clean -U pmd:check` clean. No unrelated failures.
