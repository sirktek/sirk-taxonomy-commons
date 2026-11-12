# Handoff — expected-service-life

Implementers append `## Handoff — Group N` below (what changed, what is unverified, released tag).

## Handoff — Group 1 (sirk-taxonomy-commons)

`common:expectedServiceLife` declared in `common-base.ttl` (D1), test and README updated.
Merged as #14, released as **3.29**.

## Handoff — Groups 2–4 (domain taxonomies)

| Repo | PR | Released tag | Opt-ins |
|---|---|---|---|
| sirk-furniture-taxonomy | #11 | 3.30 | `expectedServiceLife` on `Furniture` |
| sirk-machine-taxonomy | #9 | 3.25 | `expectedServiceLife`, `newPriceExVAT`, `emissionPerUnit` on `Machine` |
| sirk-ict-taxonomy | #7 | 3.12 | `expectedServiceLife`, `weight`, `emissionPerUnit` on `Hardware` |

The opt-in tests were written before commons 3.29 existed, so the "see it fail" step was not
observed; each suite was run green against 3.29 before merge.

## Handoff — Groups 5–6 (consumers)

| Repo | PR | Versions pinned |
|---|---|---|
| sirk-orgadmin-backend | #258 | commons 3.29, furniture 3.30, machine 3.25, ict 3.12 |
| sirk-mcp-server | #28 | the same four |

orgadmin: `CategoryInheritancePinTest` (Furniture has twenty definitions) and
`PrivatePropertyTreeIT` (Machine's private names include `newPriceExVAT`) follow the new
definitions; full verify and PMD passed. sirk-mcp-server needed no test change.
Logistics stays at 3.29 in both.
