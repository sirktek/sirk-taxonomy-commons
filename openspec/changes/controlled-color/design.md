# Design — controlled-color

## Precedent

- **Controlled value lists.** `common:condition` is the closest existing thing.
  - It is an `rdf:Property` whose `rdfs:range` is an enumeration class, `common:Condition`. The
    individuals (`New` … `NeedsRepair`) each have an en/no `rdfs:label`, a `skos:notation` and an
    `rdfs:comment`.
  - `RdfsTaxonomyLoader.getEnumValues` gathers the individuals and sorts them by notation.
  - orgadmin-backend types the property `STRING`, with `targetRootClass` `Condition`, and
    attaches the individuals as `options`.
  - A stored value is the individual's local name (`Good`), not an IRI or a label.

  `common:color` follows this exactly.
- **Literal annotations on individuals.** `common:depreciationRate` is an `rdf:Property` with no
  domain, used as a literal on `common:DepreciationGroup` individuals.
  - `getEnumValues` copies into `EnumValue.attributes`, keyed by local name, every literal
    predicate except `rdf:type`, `rdfs:label`, `rdfs:comment`, `skos:notation` and jurisdiction.
  - orgadmin's `toOptions` passes `attributes` through to `EnumOptionDto`, which serialises them.
    The pin test shows `"attributes":{}`.

  `common:swatch` follows this, with no Java change in any service.
- **Shared asset properties** are declared once in commons with no domain, and domain taxonomies
  opt in with `schema:domainIncludes`. furniture (`furniture:Furniture`) and machine
  (`machine:Machine`) already opt into `common:color`. This is unchanged.
- **iris taxonomy loading.**
  - `app/config.py` pins each domain TTL as a raw.githubusercontent URL at a release tag.
    `infra/setup.sh` fetches the same files to local disk for offline operation and overrides
    the sources through the env file.
  - `Taxonomy.load` in `app/taxonomy/loader.py` accepts a URL or a path.
  - `main._load_taxonomies` loads best-effort: a failure logs a WARNING and that taxonomy has no
    classifier.
  - `pipeline/classify.py` then serves full mode as "gate and embedding only". It never answers
    503 and never skips the privacy gate.

  The colour list follows this.
- **iris prompt vocabularies.** The category prompt already asks for "these exact names —
  spelled exactly as listed" (taxonomy local names). Colour follows suit.
- **Existing values are not migrated.** No commons change has rewritten asset or category data.
  The Condition change left free text in place.

Departures, each with its reason:

1. **Notations are zero-padded (`01`–`16`)**, not `1`–`5` as on Condition. Both loaders sort
   notation as a string, so `10` would otherwise sort before `2`.
2. **The swatch hex values are derived, not quoted.** No Level 1 centroids are published (D4).
3. **iris validates colour membership.** `_parse_response` checks no field against a list today,
   but the PO asked for this check (decision 2). State stays unvalidated.
4. **The commons pin lives only in `app/config.py`, not in `.taxonomy-ref`.** `.taxonomy-ref`'s
   URL pattern (`sirk-<name>-taxonomy/.../<name>-base.ttl`) does not fit the commons repo
   (`sirk-taxonomy-commons/.../common-base.ttl`).

## Inventory

| Kind | Item | Reader in this change |
| --- | --- | --- |
| RDF class | `common:Color` | `common:color`'s range; both loaders |
| RDF individuals | 16 colours (`common:Pink` … `common:Multicolored`) | orgadmin `options`; iris prompt and parser |
| RDF property | `common:swatch` (xsd:string, no domain) | both frontends via `options[].attributes.swatch` |
| Range change | `common:color` `xsd:string` → `common:Color` | orgadmin `processRdfsProperty` |
| iris function | `load_enum_locals(source, class_iri)` in `app/taxonomy/loader.py` | `main._load_colors` |
| iris function | `_load_colors()` in `app/main.py` | `lifespan` → `_load_classifiers` |
| iris config | `color_source` (pinned commons TTL URL, env `COLOR_SOURCE`), `color_class` (`…common#Color`, plain default) | `_load_colors`; `setup.sh` |
| iris setup | `setup.sh` fetches `color_source` to `${TAXONOMY_DIR}/common-base.ttl`, writes `COLOR_SOURCE` | production `.env` |
| iris parameter | `AnthropicClassifier(colors=…)`, `_build_system_prompt(…, colors)`, `_parse_response(text, colors)` | the classifier |
| iris type change | `ClassificationResult.color` becomes `ColorPrediction \| None` | `routes/classify.py` (the response field is already optional) |
| Deleted (iris) | `COLOR_LABELS` | — |
| Frontend component | `OptionSwatch` in orgadmin-frontend (≈15 lines) | `SelectInput`, `CategoryShow` |
| Frontend component | `OptionSwatch` in asset-frontend (≈15 lines) | `EnumPropertyEditor`, `PropertyValueDisplay` |
| Frontend field | optional `swatch?: string` on orgadmin-frontend `SelectOption` | `SelectInput` |
| i18n key | `resources.category.option_selection` (en/no), orgadmin-frontend | generic select in `PropertyValueInput` |
| Deleted (asset-frontend) | the `ColorLabel` and `TopColor` types and the `color.label` i18n block | — (no reader today) |

Not added anywhere:

- tables, columns or migrations;
- dependencies (rdflib is already in iris);
- endpoints or DTO fields;
- Java classes or Java config.

## D1 — Turtle

These go in `common-base.ttl` beside `common:Condition`. Four individuals are shown; the rest
follow the shape of `Pink` (table in D2).

```turtle
common:color a rdf:Property ;
    rdfs:label "Color"@en, "Farge"@no ;
    rdfs:comment "Dominant colour of the item, one of common:Color. Use Multicolored when no single colour dominates."@en ;
    rdfs:range common:Color .

common:Color a rdfs:Class ;
    rdfs:label "Color"@en, "Farge"@no ;
    rdfs:comment "The thirteen ISCC–NBS Level 1 colour categories (Kelly & Judd, NIST Special Publication 440, 1976), in their published order, plus three extras of our own (Metallic, Transparent, Multicolored) that are not ISCC–NBS categories."@en .

common:swatch a rdf:Property ;
    rdfs:label "Swatch"@en, "Fargeprøve"@no ;
    rdfs:comment "Display hint: an sRGB colour, #rrggbb, that a client may draw beside the value. Not data — never stored on an asset or category, and not a precise shade. ISCC–NBS values are the sRGB centroid of the hue's 'strong' Level 3 block (olive: 'moderate'; neutrals: white, medium gray, black) from A. Jaffer, NBS/ISCC Color System Centroids (2003), http://tx4.us/nbs-iscc.htm."@en ;
    rdfs:range xsd:string .

common:Pink a common:Color ;
    rdfs:label "Pink"@en, "Rosa"@no ;
    skos:notation "01" ;
    common:swatch "#ea9399" .

common:Metallic a common:Color ;
    rdfs:label "Metallic"@en, "Metallisk"@no ;
    rdfs:comment "Not an ISCC–NBS category. Polished or brushed metal whose apparent colour is mostly reflection."@en ;
    skos:notation "14" ;
    common:swatch "#c0c0c0" .

common:Transparent a common:Color ;
    rdfs:label "Transparent"@en, "Gjennomsiktig"@no ;
    rdfs:comment "Not an ISCC–NBS category. Clear glass or plastic whose apparent colour is what lies behind it."@en ;
    skos:notation "15" .

common:Multicolored a common:Color ;
    rdfs:label "Multicolored"@en, "Flerfarget"@no ;
    rdfs:comment "Not an ISCC–NBS category. No single colour dominates, or the surface is patterned."@en ;
    skos:notation "16" .
```

## D2 — The sixteen values

The first thirteen are in ISCC–NBS Level 1 order (NIST SP 440). The extras come last, with
`Multicolored` (the fallback) at the end.

| Notation | Local name | en | no | swatch | Swatch source (D4) |
| --- | --- | --- | --- | --- | --- |
| 01 | Pink | Pink | Rosa | `#ea9399` | strong pink |
| 02 | Red | Red | Rød | `#bc3f4a` | strong red |
| 03 | Orange | Orange | Oransje | `#ed872d` | strong orange |
| 04 | Brown | Brown | Brun | `#80461b` | strong brown |
| 05 | Yellow | Yellow | Gul | `#d4af37` | strong yellow |
| 06 | Olive | Olive | Oliven | `#665d1e` | moderate olive |
| 07 | YellowGreen | Yellow green | Gulgrønn | `#7e9f2e` | strong yellow green |
| 08 | Green | Green | Grønn | `#007959` | strong green |
| 09 | Blue | Blue | Blå | `#0067a5` | strong blue |
| 10 | Purple | Purple | Lilla | `#875692` | strong purple |
| 11 | White | White | Hvit | `#f2f3f4` | white |
| 12 | Gray | Gray | Grå | `#848482` | medium gray |
| 13 | Black | Black | Svart | `#222222` | black |
| 14 | Metallic | Metallic | Metallisk | `#c0c0c0` | CSS `silver`; not a centroid |
| 15 | Transparent | Transparent | Gjennomsiktig | — | none |
| 16 | Multicolored | Multicolored | Flerfarget | — | none |

## D3 — iris loads, prompts and filters the colour list (cross-repo contract)

### Why iris emits local names

iris writes the stored value itself. `ClassificationApplier` (sirk-app-backend) copies
`response.color().value()` into `properties["color"]` unchanged when no value is set yet, and
writes nothing when `color` is null.

Mapping in app-backend would put colour-specific Java into a service, which "Taxonomies are data"
forbids. **So iris emits the local name**, as it already does for categories.

sirk-asset-classifier has no openspec, so its contract is stated here.

### Loading

This is the smallest extension of `app/taxonomy/loader.py`, `app/config.py` and `app/main.py`.

- **Config.** `Settings` gains two fields:
  - `color_source: str`, defaulting to
    `f"{_RAW}/sirk-taxonomy-commons/refs/tags/<commons tag>/src/main/resources/taxonomy/common-base.ttl"`;
  - `color_class: str = "http://taxonomy.sirktek.no/common#Color"`.

  `color_source` is read from env `COLOR_SOURCE` like any `Settings` field, which is how
  `setup.sh` points production at the local file. `color_class` is a plain default.
  `TaxonomyConfig` and `.taxonomy-ref` are unchanged. The tag is the Group 1 release.
- **Loader.** A module function `load_enum_locals(source, class_iri) -> list[str]` in `loader.py`.
  - It parses `source` by the same URL-or-path rule as `Taxonomy.load`. Extract the parse into a
    small `_parse(source) -> Graph` that both use, and keep `Taxonomy`'s behaviour identical.
  - It returns the local names (after the last `#`) of every `?x rdf:type <class_iri>`, sorted
    by `skos:notation` as a string, then by local name. This is the same order commons'
    `getEnumValues` produces.
  - A fetch or parse failure raises `TaxonomyError`, and so does an empty result.
  - Labels are not read: the prompt and the stored value both use local names.
- **Startup.** `main._load_colors() -> list[str] | None` calls `load_enum_locals(settings.color_source,
  settings.color_class)`.
  - On any exception it logs `WARNING "colour list not loaded: …"` (plus DEBUG detail, as
    `_load_taxonomies` does) and returns None.
  - `_load_classifiers(taxonomies, colors)`, when `colors` is None, logs `WARNING "Anthropic
    classifiers not loaded: colour list not loaded"` and returns `{}`. This mirrors the missing
    `ANTHROPIC_API_KEY` branch.
- **Load-failure behaviour** is therefore today's taxonomy-failure behaviour, applied to all
  taxonomies at once:
  - full mode serves "gate and embedding only" (`pipeline/classify.py:151`);
  - the privacy gate and the embedding are unaffected;
  - nothing answers 503;
  - the app boots.

  A missing list must not let the model fall back to free colour words.
- **Production (`infra/setup.sh` step 4b).**
  - Read `Settings().color_source` from `app.config` in the existing Python heredoc.
  - Fetch it with the same `curl --fail` to `${TAXONOMY_DIR}/common-base.ttl`.
  - Write `COLOR_SOURCE=${TAXONOMY_DIR}/common-base.ttl` into the env file next to `TAXONOMIES`.

  Production then runs offline as it does today. A failed fetch fails setup, as a failed domain
  fetch does. The `Makefile` is unchanged: `make run` uses the URL default.

### Prompt and parse

- `AnthropicClassifier.__init__` takes `colors: list[str]` and passes it to
  `_build_system_prompt(taxonomy, cfg, colors)`, whose colour bullets are `- {c}` for each
  colour. `COLOR_LABELS` is deleted.
- The prompt states:
  - pick the **nearest** colour from the list, spelled exactly as listed, and never a word that
    is not in it;
  - `Multicolored` when no single colour dominates or the surface is patterned;
  - `Transparent` for clear glass or plastic;
  - `Metallic` for chrome, brushed steel or aluminium;
  - `top_colors` values come from the same list.

  The JSON examples use `"Black"` and `"White"`.
- `_parse_response(text, colors)`:
  - If the primary `color.value` is not in `colors`, `color` is None. A `color` that is
    missing, null or not an object takes the same path to None. Today
    `data["color"]["value"]` raises, which becomes `AnthropicClassifierError` and loses the
    category, state and description as well.
  - Each `top_colors` entry not in `colors` is dropped.
  - Matching is exact. There is no case-folding or mapping, since that would be a mapping
    table.
  - `ClassificationResult.color` becomes `ColorPrediction | None`.
  - `routes/classify.py` maps a None colour to `color: null`. The response schema already
    allows it.
- **The `/classify` contract:**
  - `color` is null or `{value, confidence}` with `value` one of the loaded local names;
  - `top_colors` is a list, possibly empty, of the same shape, with every value from the list;
  - `state` is unchanged and still unvalidated.
### Which classes get a colour

iris returns a colour only for a class that carries `common:color`. Today that is every furniture
and machine class (`common:color schema:domainIncludes furniture:Furniture` /
`machine:Machine`) and no ICT class. The rule is read from the taxonomy data, not hard-coded per
taxonomy.

- **Loader.** `Taxonomy` gains `has_property(class_uri, property_iri) -> bool`: true when the
  class or any ancestor, the taxonomy root included, is an object of
  `<property_iri> schema:domainIncludes ?c` (`schema:` = `https://schema.org/`) in the graph the
  taxonomy was parsed from. The domain statements are collected in `_from_graph` from the same
  domain TTL iris already loads; no extra fetch. A class unknown to the taxonomy is false.
- **Config.** `Settings` gains `color_property: str = "http://taxonomy.sirktek.no/common#color"`,
  a plain default beside `color_class`.
- **Gate.** In `AnthropicClassifier.classify`, after the IRIs are attached: when the primary
  category's IRI is None or `has_property` is false, `color` is None and `top_colors` is `[]`.
  The prompt is unchanged (the class is chosen in the same call, so the gate is after the fact).
- **app-backend needs no change.** `ClassificationApplier` already writes nothing when `color`
  is null. The gate follows iris's predicted class; an asset whose user-set category differs from
  the prediction is not re-checked (accepted).

- `beige` and `patterned` no longer appear anywhere in iris. The lower-case `"black"` examples
  in tests and `docs/backend-integration.md` become `"Black"`.

## D4 — Swatch values

- **Source (accepted by the PO).** ISCC–NBS defines Level 1 categories as unions of Level 3
  blocks and publishes no Level 1 centroid. The sRGB centroids of the Level 3 blocks come from
  A. Jaffer, "NBS/ISCC Color System Centroids" (2003), http://tx4.us/nbs-iscc.htm, which is
  derived from the NBS/ISCC TN-2 (1976) Munsell centroids. The values were checked in the
  `NBS-ISCC-rgb.txt` copy that ships with the R package rolocISCCNBS. The citation is in
  `common:swatch`'s comment (D1).
- **Derivation.**
  - Each chromatic category takes the centroid of its "strong <hue>" Level 3 block.
  - Olive has no "strong olive" block, so it uses "moderate olive".
  - The neutrals use the "white", "medium gray" and "black" blocks.
- **Metallic** is CSS `silver` (`#c0c0c0`), not a centroid.
- **Transparent and Multicolored have no swatch.**
  - Transparent has no colour of its own.
  - Multicolored would need list parsing and a stripe renderer in two frontends for one value.
  - In both cases the label already says it.
- The swatch is a hint, never a precise shade (PO decision 8).

## D5 — orgadmin-frontend: options decide the input

- **`PropertyValueInput`:** before the `switch (type)`, any property with non-empty `options`
  renders the existing `SelectInput`.
  - Out-of-options handling is unchanged: a stored value outside `options` is offered as an extra
    entry labelled by `enumValueLabel`, so it stays visible and selectable.
  - `DEPRECIATION_GROUP` keeps its label key. Other types use the new
    `resources.category.option_selection`.
  - Without options, every type renders as today.
- **`PropertyEditor.isImmediate`** becomes "immediate type, or has non-empty `options`".
- **`CategoryShow`** displays the value through `enumValueLabel` whenever the property has options
  (generalising its `DEPRECIATION_GROUP` check), and draws the option's swatch before the label.
- **`SelectInput`:** `SelectOption` gains an optional `swatch`, and each `MenuItem` draws an
  `OptionSwatch` before its text.
- Nothing names `color`. Condition gains a select too, which is accepted (decision 11). It has no
  swatches, so it gets no box.

## D6 — asset-frontend: box only

- **The select already exists.**
  - `AssetDetailPage` asks `usePropertyOptions` for every schema property name.
  - `EditableProperty` renders `EnumPropertyEditor` whenever options exist.
  - `PropertiesSection.options.test.tsx` already pins the select and the out-of-options extra
    item.

  So no dropdown change is needed. `ASSET_LIST_PROPERTIES` stays `['condition']`.
- **The box.** `EnumPropertyEditor`'s `MenuItem`s, and the option branch of
  `PropertyValueDisplay` (used by `EditableProperty` and `ReadOnlyProperty`), draw an
  `OptionSwatch` before the label. `EditableOptionChip` and `PropertyValueChip` are not touched.
- **Deletions.** The unused `ColorLabel` and `TopColor` types (`src/types/asset.ts`) and the
  unused `color.label` i18n blocks (`en.json`, `no.json`) go. They name the old iris vocabulary.

## D7 — Swatch rendering convention (both frontends)

- **`OptionSwatch({ swatch })`**:
  - renders nothing unless `swatch` matches `^#[0-9a-fA-F]{6}$`;
  - otherwise renders an inline 12×12 px box, `aria-hidden`, with a 1 px border in the theme's
    divider colour (so White is visible) and a small right margin.

  The label is always rendered as text.
- **Duplicated per frontend rather than shared.** `@sirktek/frontend-commons` has no component of
  this kind, and the two frontends pin different versions (0.14.1 and 0.17.0). Sharing it would
  mean a commons release and two bumps for about 15 lines.

## Alternatives considered

- **CATEGORY-typed colour classes (id-valued).** Rejected for three reasons:
  - id-valued storage contradicts decision 4 (local-name values);
  - it would put 16 colours into every tenant's category tree;
  - it breaks the existing free-text values.
- **iris keeps a hard-coded `COLOR_LABELS`.** Rejected by the PO (decision 3): the copy would
  drift from commons.

## Releases and order

1. Commons merges, which releases tag `3.<run>` (the next after 3.29).
2. In any order:
   - orgadmin-backend and sirk-mcp-server bump commons;
   - orgadmin-frontend and asset-frontend ship. Their code is generic and harmless before the
     backend bump: with no options, there is no box.
   - iris pins the commons tag in `color_source`. It needs the tag from step 1.
3. iris should deploy after orgadmin-backend, so that the colours it writes are options orgadmin
   already serves. Deploying out of order is harmless: a new value then shows as plain text until
   the bump.

## Risks

- **Mixed stored values.** Old free text (`svart`), old iris values (`black`) and new local names
  will coexist. This is accepted (decision 6). Both editors keep unknown values visible.
- **Pin-test churn.** orgadmin-backend's `CategoryInheritancePinTest` JSON grows by 16 options on
  `color`.
- **Condition gets a select in orgadmin-frontend** as a side effect of decision C. A user can no
  longer type a new condition there. Accepted.
- **A commons fetch failure at iris startup** turns off category, state, colour and description
  for every taxonomy until restart. This is today's behaviour for one failed taxonomy, widened to
  all of them. It shows as the WARNING line and as `classifiers=[]` in the lifespan log.
- **Model drift.** A colour the model invents, or a mis-spelling, now costs the colour for that
  asset instead of storing a wrong one.
