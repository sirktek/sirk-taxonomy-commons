# Proposal — shared-commons-properties

## Why

Commons declares shared properties once and each domain opts a class in with
`schema:domainIncludes` in its own Turtle file (commons and furniture TTL comments, README,
`CommonPropertyOptInTest` in four repos). The loader never honoured this: a domain loader reads
only its own TTL, and `RdfsTaxonomyLoader.getPropertiesForClass` keeps only properties in the
domain's own namespace. Every opt-in is dead today; logistics `RealEstate` loads with
`builtYear, propertyId, marketValue, floorArea, landArea` and none of its five accounting opt-ins.

The asset frontends need private property definitions (accounting, commercial terms) served from
the taxonomy, marked with the private set they belong to. The product owner chose (2026-10-13):
make the opt-ins work instead of redeclaring names per namespace; use ledger17's names and
valuation shape for accounting; move the commercial terms from sirk-commercial-taxonomy into
commons.

GitHub issue: sirktek/sirk-taxonomy-commons#1.

## What changes

- **Loader:** every loader reads `/taxonomy/common-base.ttl` into its model before its own TTL,
  and properties attach to a class whatever their namespace. Classes stay filtered to the
  loader's own namespace; property keys stay local names.
- **`common:privateSet`:** a string annotation on a property naming the private set it belongs to,
  surfaced as `PropertyDefinition.privateSet`, read exactly like `common:jurisdiction`.
- **Accounting, ledger17 shape (BREAKING for the data model):** `common:ledgerAccount`,
  `common:depreciationGroup` and a new `common:valuations` carry `common:privateSet "accounting"`.
  `valuations` ranges over a new marker `common:AssetValuationEntry`, mapped to a new
  `PropertyType.ASSET_VALUATIONS`: a list of `{year, bookValue, taxValue, wealthValue}`, as
  ledger17's `acct:valuations` / `acct:AssetValuation`. `common:bookValue`, `common:taxValue`,
  `common:wealthTaxValue`, `common:AssetValueEntry` and `PropertyType.ASSET_VALUE` are deleted.
- **Commercial terms:** `common:marketValue`, `common:marketValueDate`, `common:monthlyRent`,
  `common:monthlyServiceCharge`, `common:privateSet "commercial"`, no domain. Datatypes and labels
  from sirk-commercial-taxonomy 1.1.

Release: next 3.x (the repo releases breaking changes as 3.<build>, precedent 3.13 `feat!`).
Removing the `ASSET_VALUE` enum constant breaks compilation of sirk-orgadmin-backend's exhaustive
switch in `CategoryPropertyInheritanceService`; that repo adapts when it bumps (follow-up).

## Impact

- Domain taxonomies that bump gain their existing opt-ins (see design.md "Inventory"). Their
  opt-ins to the deleted properties become inert triples until they are removed.
- sirk-orgadmin-backend, sirk-orgadmin-frontend: own `ASSET_VALUE` handling must follow (below).
- sirk-app-backend, ledger17: no change in this repo's scope.

## Not in scope (follow-up changes, other repos)

- **sirk-logistics-taxonomy:** opt `RealEstate` into `common:valuations` and the four commercial
  terms, `OperatedProperty` into the four commercial terms; drop the `bookValue`, `taxValue`,
  `wealthTaxValue` opt-ins; delete `logistics:marketValue` (same key as `common:marketValue`);
  `CommonPropertyOptInTest` asserts through the loader.
- **sirk-machine-taxonomy:** opt `Machine` into `common:monthlyRent`; delete the four accounting
  opt-ins.
- **sirk-furniture-taxonomy:** delete the four accounting opt-ins; keep `emissionPerUnit`,
  `consistsOf`.
- **sirk-ict-taxonomy:** delete the eight accounting opt-ins; fix the `assetValue` / 
  `AssetValueEntry` comments in `pom.xml` and `IctPropertyDefinition`.
- **sirk-orgadmin-backend** (`private-property-definitions`, `remove-category-accounting`): bump;
  `privateSet` on the DTOs; replace the commons `ASSET_VALUE` case with `ASSET_VALUATIONS`; its own
  `ASSET_VALUE` type, `AssetValueCategoryProperty`, `AssetValueEntry` DTO and stored rows go with
  remove-category-accounting; the `wealthTaxValue` jurisdiction-filter scenario no longer applies
  (the wealth value is a field of each valuation).
- **sirk-orgadmin-frontend:** `privateSet?`; drop `ASSET_VALUE` (type menu, `AssetValueInput`,
  display, payload helpers, i18n).
- **sirk-frontend-commons, sirk-asset-frontend, vardly-frontend** (`edit-private-properties`):
  `privateSet?`; a valuations editor (`ASSET_VALUATIONS`) instead of three `ASSET_VALUE` lists;
  value names `valuations[].wealthValue`, not `wealthTaxValue`.
- **vardly-homepage:** remove "Markedsverdi" from the logistics page.
- **sirk-commercial-taxonomy:** retire (archive); nothing consumes 1.1.

## Open questions for the product owner

None raised by this change. (`disposedAt` was declined in round 3.)
