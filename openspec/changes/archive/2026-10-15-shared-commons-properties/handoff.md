
## Handoff — Group 1

Changed: `RdfsTaxonomyLoader` (reads `/taxonomy/common-base.ttl` via `RdfsTaxonomyLoader.class` before own TTL; namespace test and the now-unused `namespace` parameter removed from `getPropertiesForClass`; `COMMON_PRIVATE_SET` read into `privateSet`), `PropertyDefinition` (`privateSet` after `jurisdiction`), `common-base.ttl` (`common:privateSet` declaration, `"accounting"` on `ledgerAccount` and `depreciationGroup`). New: `test-domain-optin.ttl`, `TestDomainLoader`, `DomainOptInLoaderTest`.

Verified: `DomainOptInLoaderTest`, `CommonTaxonomyServiceTest` and the full `mvnq test` (20 tests) green. No count changes needed in `CommonTaxonomyServiceTest`.

Unverified/deferred: test uses `depreciationGroup` only; `valuations` and `marketValue` opt-ins and assertions (privateSet `commercial`, `ASSET_VALUATIONS`) wait for Group 2. Tests were written together with the implementation, not observed failing first.

## Handoff — Group 2

Changed: `common-base.ttl` (added `AssetValuationEntry`, `valuations`, commercial section with `marketValue`, `marketValueDate`, `monthlyRent`, `monthlyServiceCharge`; deleted `bookValue`, `taxValue`, `wealthTaxValue`, `AssetValueEntry`; rewrote the two financial comment blocks), `CommonPropertyDefinition` (`ASSET_VALUATIONS`, `ASSET_VALUE` removed), README (accounting table, private sets, commercial terms, opt-in section, jurisdiction prose), `test-domain-optin.ttl` (opts into `valuations`, `marketValue`), `CommonTaxonomyServiceTest`, `CommonPropertyDefinitionTest`, `DomainOptInLoaderTest`.

Verified: group tests (22) and full `mvnq test` green. Grep of src/ and README for the removed names finds nothing outside tests that assert their absence.

Unverified: tests were written together with the implementation, not observed failing first. No deviations from design.md.

## Fix pass — review findings F2, F4, F6

Changed: `RdfsTaxonomyLoader` (F2: `catch (TaxonomyLoadException e) { throw e; }` before the generic catch in `loadTaxonomyFromResource`; F4: `getJurisdiction` generalised to `getStringAnnotation(Resource, Property)`, used for jurisdiction, privateSet and enum individuals), `DomainOptInLoaderTest` (F6: imports for `Set`, `Stream`, `PropertyType`, `CommonPropertyDefinition`; redundant containsKey test merged into the key-set test; new `missingResourceReportsTheResourceNotAWrapper`, observed failing before the fix).

Unverified: commons-resource-missing case not exercised (needs removing the resource); the domain-missing case shares the code path.
