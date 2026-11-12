# Tasks — expected-service-life

Six groups, one per repo. Each group is one implementer in a fresh context with this file,
design.md and the delta spec of its repo. In groups 2–6 the repo is a sibling under `~/dev`, and
the implementer reads this change from sirk-taxonomy-commons. Touch only the listed files and run
only the listed tests. Tick the tasks, then append a short `## Handoff — Group N` note (what
changed, what is unverified, the released tag) to `handoff.md` in this directory, and stop when
the tests pass. Maven goes through `mvnq` (`~/.local/bin/mvnq`, same arguments as `./mvnw`);
never `clean`. No Java production code in any group.

Each group starts with a GitHub issue in its repo and the branch
`feature/#<id>-expected-service-life` from `origin/main`.

Suggested model: haiku for all groups; design D1–D2 pin every name, range, label and line.

Order: Group 1 merges first. Groups 2–4 follow in any order. Groups 5 and 6 after all of 2–4
have released (design D5).

### Group 1: sirk-taxonomy-commons — declare

Files: `src/main/resources/taxonomy/common-base.ttl`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonTaxonomyServiceTest,DomainOptInLoaderTest'`

- [ ] 1.1 Test first. In `sharedAssetPropertiesAreDeclaredWithoutDomain` add
      `new Term("expectedServiceLife", xsd + "integer", "Expected service life (years)", "Forventet levetid (år)")`;
      in `sharedAssetPropertiesAttachToNoCommonsCategory` add `expectedServiceLife` to the names.
      See it fail.
- [ ] 1.2 `common-base.ttl`: add the term after `common:installationYear` (design D1).
- [ ] 1.3 `README.md`: add the term to the "Shared asset properties" table.
- [ ] 1.4 Run the group's tests, then `mvnq test` once, then
      `openspec validate expected-service-life --strict`. Note the merge tag in the handoff.

### Group 2: sirk-furniture-taxonomy

Files: `src/main/resources/taxonomy/furniture-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`,
`openspec/changes/expected-service-life/` (pointer `proposal.md` and delta spec for
`furniture-common-properties`, design D2).
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 2.1 Write the pointer proposal and delta spec; bump `taxonomy-commons` (3.26) to Group 1's tag.
- [ ] 2.2 Test first: `Furniture` carries `expectedServiceLife` once, IRI
      `common#expectedServiceLife`, range `xsd:integer`, no `privateSet`. See it fail.
- [ ] 2.3 Add the opt-in after the age opt-ins (design D2).
- [ ] 2.4 `README.md` opt-in paragraph: name it.
- [ ] 2.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 3: sirk-machine-taxonomy

Files: `src/main/resources/taxonomy/machine-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`,
`openspec/changes/expected-service-life/` (pointer proposal, delta for `machine-common-properties`).
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 3.1 Write the pointer proposal and delta spec; bump `taxonomy-commons` (3.26) to Group 1's tag.
- [ ] 3.2 Test first: `Machine` carries, once each with `common#` IRIs, `expectedServiceLife`
      (`xsd:integer`, no `privateSet`), `newPriceExVAT` (`xsd:decimal`, `commercial`) and
      `emissionPerUnit` (range `common#EmissionEntry`, no `privateSet`). See it fail.
- [ ] 3.3 Add the three opt-ins where design D2 places them, with their comments. No weight
      change: `common:weight` is already on `Machine`.
- [ ] 3.4 `README.md`: name the three opt-ins.
- [ ] 3.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 4: sirk-ict-taxonomy

Files: `src/main/resources/taxonomy/ict-base.ttl`, `pom.xml` (only the `taxonomy-commons`
version), `src/test/java/no/sirktek/taxonomy/CommonPropertyOptInTest.java`, `README.md`,
`openspec/changes/expected-service-life/` (pointer proposal, MODIFIED delta for
`ict-common-properties`).
Tests: `mvnq test -Dtest='CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest'`

- [ ] 4.1 Write the pointer proposal and delta spec; bump `taxonomy-commons` (3.26) to Group 1's tag.
- [ ] 4.2 Test first: add `expectedServiceLife`, `weight` and `emissionPerUnit` to `SHARED`;
      `Hardware` carries each once with a `common#` IRI and no `privateSet` (ranges `xsd:integer`,
      `xsd:decimal`, `common#EmissionEntry`); `Software` carries none of them. See it fail.
- [ ] 4.3 Add the three opt-ins on `ict:Hardware` after `yearOfManufacture` (design D2).
- [ ] 4.4 `README.md`: name the three with the Hardware opt-ins.
- [ ] 4.5 Run the tests above, then `mvnq test` once. Merge; note the tag in the handoff.

### Group 5: sirk-orgadmin-backend — bump four in one commit

Prerequisite: Groups 2–4 released.

Files: `pom.xml` (only the commons, furniture, machine and ict versions, ~lines 214-240),
`src/test/java/no/sirktek/orgadmin/service/category/CategoryInheritancePinTest.java`,
`src/test/java/no/sirktek/orgadmin/service/category/PrivatePropertyTreeIT.java`.
Tests: `mvnq test -Dtest='CategoryPropertyInheritanceServiceTest,CategoryInheritancePinTest,CategoryServiceTest'`
and `mvnq -DskipITs=false verify -Dit.test=PrivatePropertyTreeIT -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false`

- [ ] 5.1 Set the four versions to the tags from Groups 1–4; logistics stays 3.29.
- [ ] 5.2 `CategoryInheritancePinTest`: add
      `{"name":"expectedServiceLife","type":"INTEGER","source":"INHERITED","label":"Expected service life (years)"}`
      to the fixture JSON in name order; `globalFurnitureHasExactlyTheNineteenDefinitions` becomes
      twenty (name, set, count). The `privateSet` count stays two.
- [ ] 5.2a `PrivatePropertyTreeIT.machineCarriesRentAndAccountingAndSubclassesInheritThem`: add
      `newPriceExVAT` to the expected private names, in the order the service returns them
      (alphabetical today: after `monthlyRentExVAT`), and
      `assertPrivate(all, "newPriceExVAT", PropertyType.DECIMAL, "commercial", source)` to
      `assertMachineSet`.
- [ ] 5.3 Run the tests above, then `mvnq -DskipITs=false verify` and `./mvnw clean -U pmd:check`
      once.

### Group 6: sirk-mcp-server — bump four in one commit

Prerequisite: Groups 2–4 released.

Files: `pom.xml` (only the `taxonomy-commons`, furniture, machine and ict `*.version`
properties, lines ~30-34).
Tests: `mvnq test`

- [ ] 6.1 Set the four versions to the tags from Groups 1–4, in one commit.
- [ ] 6.2 Run `mvnq test` once.

## Not in scope

The savings calculation; sirk-logistics-taxonomy, sirk-app-backend, vardly-frontend,
sirk-asset-frontend, sirk-orgadmin-frontend and sirk-service-backend (design D4); category values.
