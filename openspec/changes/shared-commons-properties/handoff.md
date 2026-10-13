
## Handoff — Group 1

Changed: `RdfsTaxonomyLoader` (reads `/taxonomy/common-base.ttl` via `RdfsTaxonomyLoader.class` before own TTL; namespace test and the now-unused `namespace` parameter removed from `getPropertiesForClass`; `COMMON_PRIVATE_SET` read into `privateSet`), `PropertyDefinition` (`privateSet` after `jurisdiction`), `common-base.ttl` (`common:privateSet` declaration, `"accounting"` on `ledgerAccount` and `depreciationGroup`). New: `test-domain-optin.ttl`, `TestDomainLoader`, `DomainOptInLoaderTest`.

Verified: `DomainOptInLoaderTest`, `CommonTaxonomyServiceTest` and the full `mvnq test` (20 tests) green. No count changes needed in `CommonTaxonomyServiceTest`.

Unverified/deferred: test uses `depreciationGroup` only; `valuations` and `marketValue` opt-ins and assertions (privateSet `commercial`, `ASSET_VALUATIONS`) wait for Group 2. Tests were written together with the implementation, not observed failing first.
