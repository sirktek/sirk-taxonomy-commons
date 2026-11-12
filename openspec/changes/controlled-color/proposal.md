# Proposal — controlled-color

## Why

`common:color` is free text today (`rdfs:range xsd:string`, `common-base.ttl:186`):

- People type their own word: "svart", "Black", "sort", "grey".
- The iris classifier writes its own 15-label English vocabulary into
  `asset.properties["color"]`: `black`, `beige`, `patterned`, …

So nobody can filter or count on colour, and the classifier's labels match nothing a person picks.
The product owner decided on 2026-11-12 to make colour a controlled value list, the way
`common:condition` already is.

## What changes

- **Commons (`common-base.ttl`):**
  - New enumeration class `common:Color` with 16 individuals:
    - the 13 ISCC–NBS Level 1 categories (NIST SP 440): `Pink`, `Red`, `Orange`, `Brown`,
      `Yellow`, `Olive`, `YellowGreen`, `Green`, `Blue`, `Purple`, `White`, `Gray`, `Black`;
    - then three extras of our own, marked as not ISCC–NBS: `Metallic`, `Transparent`,
      `Multicolored`.

    Each has en/no labels and a `skos:notation` from `01` to `16`.
  - `common:color` gets `rdfs:range common:Color` (it was `xsd:string`). Its key, labels and
    opt-ins stay the same.
  - New `common:swatch` (`xsd:string`, no domain): a display hint, `#rrggbb`, on every colour
    except `Transparent` and `Multicolored` (design D4).
- **iris (sirk-asset-classifier):**
  - Reads the colour list at startup from a pinned commons `common-base.ttl`, the same way it
    reads domain TTLs. The `COLOR_LABELS` constant goes away.
  - The prompt asks the model for the nearest value from that list.
  - `_parse_response` drops any colour outside the list, both the primary and in `top_colors`.
  - `beige` and `patterned` are gone.
- **sirk-orgadmin-frontend:**
  - Any property whose definition carries `options` is edited in the existing select. Today
    only `DEPRECIATION_GROUP` is.
  - The select and the category view draw a swatch box for an option that has a valid
    `attributes.swatch`.
  - Nothing in this is specific to colour.
- **sirk-asset-frontend:**
  - The asset editor's select and its read view draw the same swatch box.
  - The editor already offers a select for any property that has options, so only the box is
    new (design D6).
  - Colour is not added to the asset list: no column, no filter.
- **Bumps:** orgadmin-backend and sirk-mcp-server take the new commons version. Both load
  `common-base.ttl` from the commons jar they pin directly. orgadmin-backend also updates its
  pin tests.

No Java production code, migration, dependency, endpoint or DTO field is added. sirk-app-backend
does not change: `ClassificationApplier` already passes `color.value` through, and writes nothing
when `color` is null.

## Impact

- **Stored values.** New values are local names (`Black`, `YellowGreen`), as `condition` stores
  `Good`. Existing free-text values stay as they are (PO decision 5). Both editors show a stored
  value that is not in the list as an extra entry, which stays visible and selectable.
- **orgadmin type is unchanged.** `color` stays `STRING`. It gains `options` and the
  `targetRootClass` `Color`, exactly as `condition` did (design Precedent).
- **iris output changes.**
  - From the deploy on, iris writes `Black` instead of `black`. Assets classified earlier keep
    their lower-case value, which shows as an out-of-list entry.
  - A colour the model invents is dropped, so no colour is written for that asset.
- **Not breaking for Java consumers.** No key, class or Java type changes.

## Non-goals

- Migrating or mapping existing colour values (PO decision 5).
- Multi-valued colour (PO decision 4). `Multicolored` is the fallback.
- Any RAL, NCS or precise-shade property (PO decision 7).
- Colour as a column or filter in the asset list (PO decision B).
- Validating iris `state` against a list, or aligning `STATE_LABELS` (`damaged`) with
  `common:Condition` (`NeedsRepair`).
- **Public DPP.** sirk-app-backend's `templates/dppTemplate.html` renders no asset properties.
- **sirk-service-frontend / marketplace.** Neither shows asset colour.
- **sirk-service-backend order lines.** The free-text `SpecificationAttributeDto("Farge", …)` is
  unrelated to `common:color`.
- **Bumping sirk-furniture-taxonomy and sirk-machine-taxonomy.** Nothing reads commons through
  them at runtime.

## Decisions (product owner, 2026-11-12)

1. **Vocabulary.** ISCC–NBS Level 1 (NIST SP 440), the 13 categories, plus our own `Metallic`,
   `Transparent` and `Multicolored`, explicitly marked as not ISCC–NBS.
   - `skos:notation` follows the ISCC–NBS order, with the extras last.
   - NIST SP 440 is cited in `rdfs:comment`.
2. **iris emits exactly this list.**
   - It picks the nearest value.
   - `_parse_response` drops anything outside the list.
   - The labels with no target (`beige`, `patterned`) go away.
3. **iris loads the list from the commons TTL** at a pinned tag, following its own precedent for
   domain TTLs. It has no vocabulary constant of its own.
4. **Stored value.** The local name, as for Condition. iris emits the local names directly
   (design D3).
5. **Cardinality.** Single-valued, with `Multicolored` as the fallback. `top_colors` keeps its
   current form, restricted to the list.
6. **Existing free-text values are left as they are.** No migration, no mapping.
7. **Norwegian labels:** Rosa, Rød, Oransje, Brun, Gul, Oliven, Gulgrønn, Grønn, Blå, Lilla, Hvit,
   Grå, Svart, Metallisk, Gjennomsiktig, Flerfarget.
8. **No precise shades.** No RAL, NCS or precise-shade property.
9. **(A) Model.** Colour stays an enumeration value list, not CATEGORY-typed classes.
10. **(B) Asset frontend.** A dropdown in the asset editor only. `color` does not join
    `ASSET_LIST_PROPERTIES`.
11. **(C) Orgadmin frontend.** A generic select for any property that has options. Condition
    becoming a select is accepted.
12. **(D) Swatch box.** The picker and the display both show a small coloured box.
    - It is driven by a `common:swatch` annotation, which reaches clients through the existing
      `EnumValue.attributes`.
    - The source is the Jaffer centroid table (accepted).
    - `Transparent` and `Multicolored` have no box.
13. **Furniture and machine bumps cut.** They have no runtime reader.

## Open questions for the product owner

None remain open. All of the earlier questions were decided on 2026-11-12 (decisions 1, 2, 3,
12 and 13, and the non-goals above).
