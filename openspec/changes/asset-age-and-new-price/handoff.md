# Handoff — asset-age-and-new-price

Implementers append `## Handoff — Group N` below (what changed, what is unverified, released tag).

## Handoff — Group 1

Changed: `src/main/resources/taxonomy/common-base.ttl` (added `yearOfManufacture` with altLabel, `installationYear`, `newPriceExVAT`; renamed `monthlyRent`/`monthlyServiceCharge` to `...ExVAT`; header comment extended), `CommonTaxonomyServiceTest` (renamed terms, old-name absence, new `newPriceIsACommercialTermOnModel`, year terms + altLabel, `yearOfManufacture` in attach-nowhere check), `README.md` (tables).
Verified: `CommonTaxonomyServiceTest,DomainOptInLoaderTest` green (24), full `mvnq test` run, `openspec validate --strict`.
Unverified: tests were written together with the TTL change, not observed failing first. Nothing committed or pushed.
Tag: pending merge.
