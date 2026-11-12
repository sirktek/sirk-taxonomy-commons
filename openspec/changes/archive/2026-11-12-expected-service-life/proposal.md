# Proposal — expected-service-life

## Why

A kommune tender requires reporting the money and climate saved by reuse. The agreed method
(sirk-orgadmin-backend `docs/reuse-savings/README.md`, pinned 2026-11-11) credits an order with
`share = min(1, years the order secures / category's expected service life)` against a new item's
price (`newPriceExVAT`) and A-module emission (`emissionPerUnit`), and reports avoided waste as
weight × quantity. No taxonomy declares an expected service life, ICT has no weight, machine has no
new price, and neither machine nor ICT has an emission factor. The calculation is out of scope;
this change only adds the vocabulary it will read, the way `asset-age-and-new-price` (archived
2026-11-10) added `yearOfManufacture` and `newPriceExVAT`.

## What changes

- **Commons (`common-base.ttl`):** new `common:expectedServiceLife` ("Expected service life
  (years)"/"Forventet levetid (år)"), `xsd:integer`, no domain, public (no `privateSet`), in
  "Shared asset properties" after `installationYear` (design D1).
- **furniture:** opt `furniture:Furniture` into `expectedServiceLife`.
- **machine:** opt `machine:Machine` into `expectedServiceLife`, `newPriceExVAT` and
  `emissionPerUnit`. It already carries `common:weight` (`machine-base.ttl:453`).
- **ict:** opt `ict:Hardware` into `expectedServiceLife`, `common:weight` and `emissionPerUnit`.
- **Consumers:** sirk-orgadmin-backend and sirk-mcp-server bump commons, furniture, machine and ict
  in one commit each; orgadmin updates two test pins. logistics is unchanged and keeps its version.
  vardly-frontend and sirk-asset-frontend need nothing (design D4).

The value is set on a category in orgadmin (inherited down the tree) and on an individual asset in
sirk-app-backend, which stores asset properties by local name; both work for any opted-in property
today, as for `weight` and `newPriceExVAT`. No loader, Java production code, migration, dependency
or config is added.

**Inventory note.** Nothing inside this change reads the new property or opt-ins: the reader is
the later reuse-savings calculation in orgadmin/sirk-service-backend. The method requires them and
the product owner asked for them (2026-11-12), so they stay.

## Impact

- New public category and asset property on furniture, machine and ICT hardware; orgadmin types it
  `INTEGER` (design D3). Machine gains a private `commercial` new price; machine and ICT hardware
  gain the `EMISSION` editor.
- Not breaking: nothing is renamed or removed. Consumers should still bump commons with the domains
  (version skew, design Risks).

## Non-goals

- The savings calculation, its dashboard, CSV export or method description.
- Category values (e.g. office chair = 8 years), and any shared or global default set (design D6).
- Raising an asset's value after a service (sirk-app-backend, later change).
- `expectedServiceLife` on `common:Model`, `ict:Software` or logistics classes (design D2).
- `common:consistsOf` on machine or ICT (furniture pairs it with `emissionPerUnit`, but the method
  does not read a bill of materials).

## Decisions (product owner, 2026-11-12)

1. Add `newPriceExVAT` on `machine:Machine` and `emissionPerUnit` on `machine:Machine` and
   `ict:Hardware` to this change.
2. Category default values are per tenant, entered in orgadmin's category property editor; a
   shared-defaults mechanism is out of scope.
3. Whole years: `xsd:integer`.
4. Not on `common:Model`, not on `ict:Software`.

## Open questions for the product owner

None.
