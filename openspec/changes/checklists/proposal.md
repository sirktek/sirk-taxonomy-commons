# Proposal — checklists (taxonomy-commons slice)

## Why

A Vardly round point's checklist (`logistics:controlChecklist` on ControlPoint,
`machine:controlChecklist` on Machine) is a JSON-encoded ChecklistTemplate v1, but both are
declared with range `xsd:string`. sirk-orgadmin-backend therefore validates a category default as
a generic string: at most 2000 characters and a screen for "suspicious" substrings. A default
fits about 15 short checks instead of the contract's 100, and normal check text such as "Sjekk
at window. er lukket" or "data: se skilt" is refused.

The product owner chose (2026-10-15, "option B") to validate a property by the type its
definition declares. The taxonomy has to say which properties hold a checklist. That is data, so
it belongs here: one range marker class, mapped to one property type, like every other complex
value.

Canonical cross-repo design: sirk-service-backend `openspec/changes/checklists/design.md`
(contract `ChecklistTemplate` v1, rule R7). No GitHub issue yet; open one before branching.

## What changes

- `common-base.ttl`: a new range marker class `common:ChecklistTemplateEntry`, described in its
  `rdfs:comment` (the contract's shape and limits). No properties of its own, like the other
  `*Entry` markers.
- `CommonPropertyDefinition`: the range `common:ChecklistTemplateEntry` maps to a new
  `PropertyType.CHECKLIST`.

Nothing else. The domain taxonomies switch their `controlChecklist` range to the new class in
their own `checklists` changes (sirk-logistics-taxonomy, sirk-machine-taxonomy), and
sirk-orgadmin-backend validates `CHECKLIST` values in its `checklists` change.

## Impact

- **Consumers:** the four domain taxonomies and sirk-orgadmin-backend, through the next release
  (`3.<build>`). A consumer that does not know the new enum constant is unaffected until it
  bumps; `getPropertyType` returns it only for the new range, which no released TTL uses yet.
- **Breaking:** no. One added class, one added enum constant.

## Non-goals

- A shared `common:controlChecklist` term replacing the two domain declarations (see open
  questions).
- Field-level properties for the template (`key`, `text`, …). The contract lives in the canonical
  design; the marker's comment points to it, as `AssetValuationEntry`'s does.

## Open questions for the product owner

Decided 2026-10-15: **no.** Keep the two domain declarations; Vardly and stored data already use
`controlChecklist` and `machine:controlChecklist`.

- **One shared `common:controlChecklist`?** machine-base.ttl says `machine:controlChecklist` is
  "declared in the machine namespace until commons has a shared controlChecklist term". Declaring
  `common:controlChecklist` (range `common:ChecklistTemplateEntry`) and opting ControlPoint and
  Machine in with `schema:domainIncludes` would delete one of the two declarations. The local
  name, and so every stored value, stays `controlChecklist`; only the property URI changes. Not
  part of this change unless you ask for it.
