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

## Handoff — Group 2

Changed (sirk-furniture-taxonomy, branch `feature/#6-consolidate-shared-properties`): `pom.xml`
(`taxonomy-commons` 3.14 to 3.19); `furniture-base.ttl` (eleven `furniture:*` declarations removed,
`common:<name> schema:domainIncludes furniture:Furniture` opt-ins appended at the end; the inert
`operationsAndMaintenanceManual` `domainIncludes common:Model` went with its declaration);
`CommonPropertyOptInTest` (`furnitureCarriesSharedPropertiesOnceFromCommons`, with a `findAll`
helper); `README.md` (opt-in paragraph names the eleven shared properties).

Verified: the group tests (`CommonPropertyOptInTest,TaxonomyServiceTest,PropertyDefinitionTest`,
28 tests) pass on 3.19. The new test was seen failing first (furniture IRI on `color`, on 3.14).
The full `mvnq test` (28) passes. Commons 3.19 resolved to `~/.m2` during the build.

Unverified: the furniture merge and the released tag. The tag `3.19` already exists on commons
`main` (merge 4769068). No consumer besides furniture was rebuilt. Groups 6 and 7 still need
this release.

Deviation: test-first ran before the pom bump (2.2 before 2.1), so the failure was seen on 3.14.
No design deviation.

## Handoff — Group 3 (sirk-ict-taxonomy)

- PR sirk-ict-taxonomy#4. taxonomy-commons 3.19; `ict:manufacturer`, `ict:model`, `ict:serialNumber`
  deleted; `common:manufacturer`/`common:model` opted into Hardware and Software, `common:serialNumber`
  into Hardware. `purchaseDate` stays ict.
- `noIctClassCarriesACommonsProperty` renamed and narrowed to allow exactly those three `common#` IRIs.
- 21 tests green. Release tag assigned on merge.

## Handoff — Group 4 (sirk-logistics-taxonomy)

- PR sirk-logistics-taxonomy#6. taxonomy-commons 3.19; `Freight` takes `common:weight`/`common:volume`,
  `ControlPoint` takes `common:controlChecklist` (`CHECKLIST`). `logistics:address` stays (D3).
- Local `checklists` change moved to `archive/2026-10-15-checklists` as superseded (not via
  `openspec archive`: its delta described the deleted `logistics:controlChecklist`).
- 27 tests green. Release tag assigned on merge.

## Handoff — Group 5 (sirk-machine-taxonomy)

- PR sirk-machine-taxonomy#5. taxonomy-commons 3.19; thirteen `machine:` declarations deleted
  (eleven shared names, `manufacturer`, `controlChecklist`); `Machine` opts into all thirteen.
- Local `checklists` change archived as superseded, same way as Group 4.
- 49 tests green. Release tag assigned on merge; Groups 6 and 7 bump to the four domain tags.

## Handoff — Group 6 (sirk-orgadmin-backend)

- PR sirk-orgadmin-backend#222 (merged, deployed): commons 3.19, furniture 3.25, logistics 3.27,
  machine 3.20, ict 3.8, together with orgadmin `checklists` groups 1 and 3 (`PropertyType.CHECKLIST`,
  values stored normalized). Full suite 1794 green.

## Handoff — Group 7 (sirk-mcp-server)

- PR sirk-mcp-server#25 (merged): the same five versions. 113 tests green.
