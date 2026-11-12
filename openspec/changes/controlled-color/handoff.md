
## Handoff — Group 1
Changed: `common-base.ttl` (common:Color + 16 individuals, common:swatch, color range now common:Color), `CommonTaxonomyServiceTest`, `DomainOptInLoaderTest`, `test-domain-optin.ttl` (color opt-in), `README.md` (color row, Colour section).
Verified: full `mvnq test` green, 30 tests, 0 failures. Loader puts `swatch` into `EnumValue.attributes`; color remains an enum property (DomainOptInLoaderTest).
Unverified: nothing known. Released tag: pending merge.
