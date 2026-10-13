# Tasks — shared-commons-properties

Two groups, run in order, each one implementer in a fresh context with this file, design.md and
`specs/common-taxonomy-loading/spec.md`. Touch only the listed files, run only the listed tests,
tick the tasks, append a short `## Handoff — Group N` note (what changed, what is unverified) to
`handoff.md` in this directory, and stop when the tests pass. Maven through `mvnq`
(`~/.local/bin/mvnq`, same arguments as `./mvnw`); never `clean`. No Java code specific to a
taxonomy class or property beyond the range mapping in `CommonPropertyDefinition`.

Before starting: a GitHub issue; branch `feature/#<id>-shared-commons-properties`.

Suggested model: sonnet for both (design pins every name, range and label).

### Group 1: Loader merges commons; `privateSet`

Design D1, D2, D5 (loader parts).

Files: `src/main/java/no/sirktek/taxonomy/loader/RdfsTaxonomyLoader.java`,
`src/main/java/no/sirktek/taxonomy/model/PropertyDefinition.java`,
`src/main/resources/taxonomy/common-base.ttl` (only the `common:privateSet` declaration and
`common:privateSet` on `ledgerAccount` / `depreciationGroup`),
new `src/test/resources/taxonomy/test-domain-optin.ttl`,
new `src/test/java/no/sirktek/taxonomy/loader/TestDomainLoader.java`,
new `src/test/java/no/sirktek/taxonomy/loader/DomainOptInLoaderTest.java`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java` (only if a count changes).
Tests: `mvnq test -Dtest='DomainOptInLoaderTest,CommonTaxonomyServiceTest'`

- [ ] 1.1 Test first: fixture TTL (`testdomain:TestBuilding`, own `testdomain:floors`, opt-in
      `common:depreciationGroup`; `common:ledgerAccount` deliberately not opted in), `TestDomainLoader`,
      and `DomainOptInLoaderTest` covering the spec scenarios "Opted-in commons property
      attaches" (use `depreciationGroup` until Group 2 adds `marketValue`), "not opted in stays
      absent", "Commons classes do not leak", "Commons enumeration values reach a domain class",
      and `privateSet` `accounting` / `null`. See them fail.
- [ ] 1.2 `loadTaxonomyFromResource`: read `/taxonomy/common-base.ttl` via
      `RdfsTaxonomyLoader.class.getResourceAsStream` into the model, then the requested resource.
- [ ] 1.3 `getPropertiesForClass`: drop the `startsWith(namespace)` test; leave the
      `namespace` parameter only if still used, otherwise remove it.
- [ ] 1.4 `common:privateSet` declaration in `common-base.ttl` (D2 text); `"accounting"` on
      `ledgerAccount` and `depreciationGroup`; `COMMON_PRIVATE_SET` in the loader;
      `PropertyDefinition.privateSet` after `jurisdiction` with a javadoc `@param`.
- [ ] 1.5 Run the group's tests; then `mvnq test` once.

### Group 2: Accounting in ledger17 shape; commercial terms

Design D3, D4, D5 (vocabulary parts).

Files: `src/main/resources/taxonomy/common-base.ttl`,
`src/main/java/no/sirktek/taxonomy/model/CommonPropertyDefinition.java`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java`,
`src/test/java/no/sirktek/taxonomy/model/CommonPropertyDefinitionTest.java`,
`src/test/resources/taxonomy/test-domain-optin.ttl`,
`src/test/java/no/sirktek/taxonomy/loader/DomainOptInLoaderTest.java`, `README.md`.
Tests: `mvnq test -Dtest='CommonTaxonomyServiceTest,CommonPropertyDefinitionTest,DomainOptInLoaderTest'`

- [ ] 2.1 Test first. `CommonPropertyDefinitionTest`: `AssetValuationEntry` → `ASSET_VALUATIONS`;
      drop the `ASSET_VALUE` test. `CommonTaxonomyServiceTest`: rewrite
      `accountingPropertiesAreDeclared…` for `ledgerAccount`, `depreciationGroup`, `valuations`
      (ranges, `privateSet` `accounting`, no domain) and the absence of `bookValue`, `taxValue`,
      `wealthTaxValue`, `AssetValueEntry`; add a commercial-terms test (spec "Commercial term
      declaration" for all four: ranges, en/no labels per design D4, `privateSet` `commercial`, no
      domain); `rangeMarkerClassesHaveNoProperties` lists `AssetValuationEntry`. Fixture: opt
      `TestBuilding` into `common:valuations` and `common:marketValue`; `DomainOptInLoaderTest`
      asserts the full key set `floors, depreciationGroup, valuations, marketValue`,
      `marketValue`'s `uri`, `privateSet` `commercial`, and `ASSET_VALUATIONS` for `valuations`.
      See them fail.
- [ ] 2.2 `common-base.ttl`: add `AssetValuationEntry` and `valuations` (D3 text); delete
      `bookValue`, `taxValue`, `wealthTaxValue`, `AssetValueEntry`; rewrite the two financial
      comment blocks around `valuations`; add the commercial section (D4).
- [ ] 2.3 `CommonPropertyDefinition`: `AssetValuationEntry` → `ASSET_VALUATIONS`; remove
      `AssetValueEntry` and `ASSET_VALUE`; update the class javadoc.
- [ ] 2.4 `README.md`: replace the bookValue/taxValue/wealthTaxValue table and prose with
      `valuations` / `ASSET_VALUATIONS`, add `privateSet` and the commercial terms, state that
      the loader merges commons so `domainIncludes` opt-ins attach.
- [ ] 2.5 Run the group's tests; then `mvnq test` once.

## Not in scope

Follow-up changes in other repos, listed in proposal.md "Not in scope": logistics / machine /
furniture / ict opt-in edits (each bundled with its commons bump), sirk-orgadmin-backend
(`private-property-definitions`, `remove-category-accounting`), sirk-orgadmin-frontend,
sirk-frontend-commons / sirk-asset-frontend / vardly-frontend (`edit-private-properties`),
vardly-homepage ("Markedsverdi"), retiring sirk-commercial-taxonomy.
