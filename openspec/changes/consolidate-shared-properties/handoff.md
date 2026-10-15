# Handoff

## Handoff — Group 1

Changed: `common-base.ttl` (new "Shared asset properties" section after `common:resourceVariant`
with the eleven terms and `common:controlChecklist`; `common:manufacturer` now
`schema:domainIncludes common:Model` plus `skos:altLabel "Vendor"@en`), `CommonTaxonomyServiceTest`
(`sharedAssetPropertiesAreDeclaredWithoutDomain`, no-commons-category check), `test-domain-optin.ttl`
and `DomainOptInLoaderTest` (`weight` opt-in), `README.md` (Shared asset properties table).

Verified: the group tests (21) and the full `mvnq test` (25) pass; `openspec validate --strict` valid.
Tests were seen failing first.

Unverified: the released tag (`3.<run>`) is unknown until merge; Groups 2-5 must bump to it.
No design deviation.
