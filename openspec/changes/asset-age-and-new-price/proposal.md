# Proposal — asset-age-and-new-price

## Why

We will later report "NOK saved" on ombruk (reuse) service orders: the price paid at ombruk
(already on sirk-service-backend order lines) against the price of a new equivalent item, and the
item's age. That calculation is out of scope. This change only adds the taxonomy vocabulary it
will read, decided by the product owner on 2026-11-09.

Today the age of an item is three different properties with three ranges:
`furniture:productionDate` (`xsd:date`), `machine:yearOfManufacture` (`xsd:integer`) and
`logistics:builtYear` (`xsd:gYear`, on `RealEstate`). orgadmin types them `DATE`, `INTEGER` and
`STRING` (design D3). There is no property for when an item was put into use, and none for a new
item's price.

## What changes

- **Commons (`common-base.ttl`):**
  - New `common:yearOfManufacture` ("Year of manufacture"/"Produksjonsår") and
    `common:installationYear` ("Installation year"/"Installasjonsår"), `xsd:integer`, no domain,
    public (no `privateSet`), in "Shared asset properties". `yearOfManufacture` carries
    `skos:altLabel "Built year"@en, "Byggeår"@no`, like `manufacturer`'s "Vendor".
  - New `common:newPriceExVAT` ("New price (excl. VAT)"/"Nypris (eks. mva)"), `xsd:decimal`,
    `common:privateSet "commercial"`, `schema:domainIncludes common:Model`, in "Commercial terms".
  - Renamed: `common:monthlyRent` → `common:monthlyRentExVAT`, `common:monthlyServiceCharge` →
    `common:monthlyServiceChargeExVAT`. Labels unchanged.
- **furniture:** delete `furniture:productionDate`; opt `furniture:Furniture` into
  `yearOfManufacture`, `installationYear`, `newPriceExVAT` and `common:valuations`.
- **machine:** delete `machine:yearOfManufacture`; opt `machine:Machine` into `yearOfManufacture`
  and `installationYear`; the `monthlyRent` opt-in becomes `monthlyRentExVAT`. Delete the
  now-dead `yearOfManufacture` → `YEAR` name heuristic and the `PropertyType.YEAR` constant
  (design D3).
- **ict:** opt `ict:Hardware, ict:Software` into `newPriceExVAT` and `installationYear`, and
  `ict:Hardware` only into `yearOfManufacture`.
- **logistics:** delete `logistics:builtYear`; opt `logistics:RealEstate` into
  `yearOfManufacture` and `installationYear`; the rent and service-charge opt-ins on `RealEstate`
  and `OperatedProperty` take the new names.
- **Consumers:** orgadmin-backend and mcp-server bump commons, furniture, machine, logistics and
  ict in one commit each; orgadmin updates two test pins and deletes its `case YEAR`. vardly-frontend and sirk-asset-frontend rename
  the i18n keys (and one Vardly fact key) that are keyed by the old local names (design D5).

No loader, Java production code, migration, dependency or config is added; the only Java
production changes are the two `YEAR` deletions.

**Inventory note.** Nothing inside this change reads `yearOfManufacture`, `installationYear`,
`newPriceExVAT`, the `Byggeår` altLabel or the furniture `valuations` opt-in: the reader is the later "NOK saved"
calculation. The product owner asked for them explicitly (2026-11-09), so they stay.

## Impact

- **Stored keys change.** orgadmin `category_property.property_name` and sirk-app-backend
  `asset.properties` / private-property keys are local names, so values under `productionDate`,
  `builtYear`, `monthlyRent` and `monthlyServiceCharge` would be orphaned. Machine values keep
  their key: `machine:yearOfManufacture` and `common:yearOfManufacture` share the local name. The product owner confirms none are held (2026-11-09). No data migration,
  following the `furniture:state` → `common:condition` precedent (design D4).
- **Types in orgadmin:** furniture age `DATE` → `INTEGER`, real-estate age `STRING` → `INTEGER`,
  machine unchanged (design D3).
- **Real estate label:** the age of a building reads "Produksjonsår" wherever the taxonomy label is
  shown. The "Byggeår" altLabel reaches sirk-mcp-server (which serves the TTL) but not orgadmin:
  the loader reads altLabels for classes only, exactly as for "Vendor" (design Precedent). Vardly's
  key-facts card keeps its own "Byggeår" label (design D5).
- **Breaking:** yes for keys, by PO decision; consumers must bump all five taxonomy jars together.
  The machine jar drops `PropertyType.YEAR`; orgadmin's switch over it is fixed in its bump commit.

## Non-goals

- The "NOK saved" calculation, report, or any reader of the new properties.
- Choosing between `yearOfManufacture` and `installationYear` (the later calculation does).
- `newPriceExVAT` on machine or real estate (PO 2026-11-09).
- Surfacing property altLabels through the loader.
- Accounting opt-ins on furniture beyond `valuations` (`ledgerAccount`, `depreciationGroup`).

## Decisions (product owner, 2026-11-09)

1. One `common:yearOfManufacture` replaces the three domain age properties.
2. New `common:installationYear`, same classes as `yearOfManufacture`.
3. `common:valuations` opts in `furniture:Furniture`.
4. New price is `common:newPriceExVAT`, private (`commercial`), excl. VAT, settable on categories
   such as `furniture:OfficeChair` and on `common:Model`, attached like `common:emissionPerUnit`;
   also on `ict:Hardware` and `ict:Software`. Not on `machine:Machine` or `logistics:RealEstate`.
5. `monthlyRent`/`monthlyServiceCharge` are renamed with the `ExVAT` suffix; nobody uses them yet.
6. No data migration.
7. `common:installationYear` is public.
8. `common:yearOfManufacture` gets `skos:altLabel "Built year"@en, "Byggeår"@no`.
9. The null-valued `productionDate` rows that orgadmin `V1.4.1`/`V1.6.0` seeded on the root
   `furniture` category stay, as for `state`.
10. `ict:Hardware` also gets `yearOfManufacture` and `installationYear` (2026-11-10).
11. `ict:Software` gets `installationYear` and `newPriceExVAT`, not `yearOfManufacture`
    (2026-11-10, amended).

## Open questions for the product owner

None.
