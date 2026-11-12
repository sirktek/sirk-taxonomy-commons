# Tasks — controlled-color

Six groups, one per repo. Each group is one implementer in a fresh context with this file,
design.md and the delta spec of its repo. In groups 2–6 the repo is a sibling under `~/dev`, and
the implementer reads this change from sirk-taxonomy-commons.

Rules for every implementer:

- Touch only the listed files and run only the listed tests.
- Tick the tasks, then append a short `## Handoff — Group N` note to `handoff.md` in this
  directory: what changed, what is unverified, and the released tag. Stop when the tests pass.
- Maven goes through `mvnq` (`~/.local/bin/mvnq`, same arguments as `./mvnw`); never `clean`.
- Frontends: `yarn test --silent 2>&1 | tail -n 40`.
- No Java production code anywhere. The TS production code in groups 5 and 6 names no colour.

Branching:

- Each group starts with a GitHub issue in its repo and the branch
  `feature/#<id>-controlled-color`, from `origin/main`.
- The local commons main is behind origin/main (3.29).
- The local sirk-mcp-server checkout is on `feature/asset-age-and-new-price` (commons 3.26).
  Branch from origin/main, which is on 3.29.
- In each frontend, the untracked `openspec/changes/controlled-color/` goes onto the group's
  branch.

Suggested models:

| Model | Groups |
| --- | --- |
| sonnet | 1, 2, 5 and 6 |
| haiku | 3 and 4 |

Design D1–D7 pin every name, label, notation, swatch, prompt rule, config key and display site.

Order:

1. Group 1 merges first.
2. Groups 2–4 need its tag.
3. Groups 5 and 6 can run at any time.
4. Group 2 (iris) deploys after Group 3 (design, Releases and order).

### Group 1: sirk-taxonomy-commons — Color, the sixteen values and swatch

Files:
- `src/main/resources/taxonomy/common-base.ttl`
- `src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java`
- `src/test/java/no/sirktek/taxonomy/loader/DomainOptInLoaderTest.java`
- `src/test/resources/taxonomy/test-domain-optin.ttl`
- `README.md`

Tests: `mvnq test -Dtest='CommonTaxonomyServiceTest,DomainOptInLoaderTest'`

- [x] 1.1 Test first, in `CommonTaxonomyServiceTest`.
  - In `sharedAssetPropertiesAreDeclaredWithoutDomain`, change the `color` term's range from
    `xsd:string` to `common:Color`.
  - Add a test covering:
    - the class comment citing NIST SP 440;
    - the sixteen individuals with their en/no labels and notations `01`–`16`;
    - the "Not an ISCC–NBS category" comment, on exactly `Metallic`, `Transparent` and
      `Multicolored`;
    - `common:swatch`: range, labels, a comment citing Jaffer, and no domain;
    - every swatch matching `^#[0-9a-f]{6}$`, with `Transparent` and `Multicolored` having none.
  - In `sharedAssetPropertiesAttachToNoCommonsCategory`, add that no category has `swatch`.
- [x] 1.2 Test first, in `DomainOptInLoaderTest`.
  - Add `common:color schema:domainIncludes testdomain:TestBuilding` to the fixture's opt-in
    block, and `color` to the asserted opt-in key set.
  - Add `colorValuesRunInIsccNbsOrderWithSwatches`, modelled on
    `conditionGradesRunFromBestToWorstBySkosNotation`. It asserts:
    - the sixteen names in order;
    - `YellowGreen` is labelled "Yellow green" / "Gulgrønn";
    - `Black`'s attributes have `swatch` = `#222222`;
    - `Transparent` and `Multicolored` have no `swatch` in their attributes.
- [x] 1.3 Write D1 into `common-base.ttl`, with the D2 table.
  - `common:color` gets `rdfs:range common:Color` and a comment.
  - `common:Color` goes beside `common:Condition`.
  - `common:swatch` goes beside `common:depreciationRate`.
  - Add the 16 individuals.
- [x] 1.4 README:
  - set the `common:color` row's range to `common:Color`;
  - list the sixteen values and `common:swatch` where `Condition` is described.
- [x] 1.5 Run the tests. Merge, then record the released tag in the handoff.

### Group 2: sirk-asset-classifier (iris) — load, prompt and filter the colour list

Prerequisite: Group 1 released (its tag goes into `color_source`).

Do not read iris `docs/design.md`. It is old SigLIP history and is not touched.

Files:
- `app/config.py`
- `app/taxonomy/loader.py`
- `app/main.py`
- `app/integrations/anthropic_client.py`
- `app/routes/classify.py` (only the None-colour mapping)
- `app/schemas.py` (only the `ColorPrediction.value` description)
- `infra/setup.sh` (step 4b and the env file only)
- `tests/test_taxonomy_loader.py`
- `tests/test_taxonomy_config.py`
- `tests/test_anthropic_client.py`
- `tests/conftest.py`
- `tests/test_routes.py`
- a new fixture `tests/fixtures/mini-common.ttl`
- `docs/backend-integration.md`

Tests: `make test` (pytest), plus `make lint` and `make typecheck`.

- [ ] 2.1 Test first, in `test_taxonomy_loader.py`, with the fixture `mini-common.ttl`: three
  `common:Color` individuals with notations "02", "10", "01", plus one `common:Condition`
  individual. Cover:
  - `load_enum_locals(path, "http://taxonomy.sirktek.no/common#Color")` returns them in notation
    order and leaves out the Condition individual;
  - a missing file raises `TaxonomyError`;
  - a class with no individuals raises `TaxonomyError`;
  - the existing `Taxonomy` tests stay green unchanged.
- [ ] 2.1a Test first, in `test_taxonomy_loader.py`, for D3 "Which classes get a colour": with a
  fixture domain TTL whose root carries `common:color schema:domainIncludes`, `has_property` is
  true for the root's grandchild, false for a class in a taxonomy without the statement, and false
  for an unknown URI.
- [ ] 2.2 Test first, in `test_taxonomy_config.py`.
  - Assert the default `color_source` points at `sirk-taxonomy-commons/refs/tags/<Group 1
    tag>/…/common-base.ttl`. Env override is pydantic's behaviour; do not test it.
  - Add a lifespan test, modelled on `test_lifespan_loads_the_rest_when_one_fails`: when
    `color_source` does not exist, `state.classifiers` is empty and the log contains "colour list
    not loaded".
- [ ] 2.3 Test first, in `test_anthropic_client.py`.
  - The prompt lists exactly the colours passed in, contains no `beige` and no `patterned`, and
    states the nearest-value, `Multicolored` and `Transparent` rules.
  - `_parse_response` sets `color` to None for a value outside the list, drops out-of-list
    `top_colors` entries, and keeps exact matches.
  - A response whose `color` is missing, null or not an object parses with `color` None. The
    category, state and description are kept, and no `AnthropicClassifierError` is raised.
  - Change `"black"` to `"Black"` in the fixtures and assertions.
  - `test_state_and_color_vocabulary_is_shared` loops over `STATE_LABELS` plus the passed colour
    list.
- [ ] 2.4 Implement D3, Loading.
  - In `loader.py`: extract `_parse(source)` and add `load_enum_locals`. `Taxonomy` behaviour is
    unchanged.
  - In `config.py`: add `color_source` and `color_class`.
  - In `main.py`: add `_load_colors`, and make `_load_classifiers(taxonomies, colors)` return `{}`
    with a warning when `colors` is None.
- [ ] 2.5 Implement D3, Prompt and parse.
  - Delete `COLOR_LABELS`.
  - Read `data.get("color")`. A missing, null or non-dict `color`, or one with an off-list
    `value`, becomes None through one path.
  - Pass `colors` through `AnthropicClassifier` into `_build_system_prompt` and
    `_parse_response`.
  - `ClassificationResult.color` becomes optional, and `routes/classify.py` maps None to `null`.
  - Update the `ColorPrediction.value` description to "one of the commons colour local names",
    and the `"black"` fixtures in `conftest.py` and `test_routes.py`.
- [ ] 2.5a Implement D3, "Which classes get a colour": `Taxonomy.has_property`, the
  `color_property` setting, and the gate in `AnthropicClassifier.classify`. Test first in
  `test_anthropic_client.py`: a primary class without `common:color` (and an unresolved primary
  IRI) yields `color` None and empty `top_colors`; a furniture class keeps both.
- [ ] 2.6 In `infra/setup.sh` step 4b:
  - fetch `color_source`'s default to `${TAXONOMY_DIR}/common-base.ttl`;
  - write `COLOR_SOURCE=` into the env file.

  Also change the example in `docs/backend-integration.md:135` to `"Black"`. `docs/design.md`'s
  SigLIP history stays.
- [ ] 2.7 Run the tests. Deploy after Group 3. Deploying iris means rerunning `infra/setup.sh`:
  that run fetches `common-base.ttl` and writes `COLOR_SOURCE`. Without it, iris fetches the
  default URL at every startup and loses offline operation. Say this in the handoff.

### Group 3: sirk-orgadmin-backend — bump and pin colour

Prerequisite: Group 1 released.

Files:
- `pom.xml` (only the commons version)
- `src/test/java/no/sirktek/orgadmin/service/category/CategoryInheritancePinTest.java`
- `src/test/java/no/sirktek/orgadmin/RdfsCategoryPropertyInheritanceTest.java`

Tests:
- `mvnq test -Dtest='CategoryInheritancePinTest,RdfsCategoryPropertyInheritanceTest,CategoryPropertyInheritanceServiceTest'`
- then once, at the end: `mvnq -DskipITs=false verify` and `mvnq -U pmd:check`

- [ ] 3.1 Set the commons version to the Group 1 tag. The furniture and machine versions are
  unchanged: the backend loads `common-base.ttl` from the commons jar it pins.
- [ ] 3.2 In `CategoryInheritancePinTest.EXPECTED_JSON`, the `color` entry gains
  `"targetRootClass":"Color"` and the sixteen options in notation order, each in the shape the
  `condition` entry uses.
  - `attributes` is `{"swatch":"#rrggbb"}`, or `{}` for `Transparent` and `Multicolored`.
  - `notation` is "01" to "16", zero-padded strings.
  - The 13 ISCC–NBS options have no `rdfs:comment`, so they carry **no `description` key**
    (`EnumOptionDto` is NON_NULL). `Metallic`, `Transparent` and `Multicolored` carry their
    `description` (the D1 comment text). Do not invent descriptions for the others.
  - Take the values from design D2; do not regenerate the JSON blindly.
- [ ] 3.3 Add a test after `conditionIsAStringWithFiveOrderedGradesOnFurnitureIctHardwareAndMachine`
  asserting that `color` on Chair, Furniture and Machine:
  - is `STRING` with `targetRootClass` `Color`;
  - has the sixteen options in order;
  - has `Black`'s `attributes.swatch` = `#222222`.
- [ ] 3.4 Run the tests.
  - `V1.4.1__CategoryData.sql`'s null `color` seed stays.
  - No production code changes.

### Group 4: sirk-mcp-server — bump

Prerequisite: Group 1 released.

Files: `pom.xml` (only the commons version property, ~lines 30-34).
Tests: `mvnq test`

- [ ] 4.1 Set the commons version to the Group 1 tag. Run the tests. In the handoff, name the
  version range the bump spans from the base you branched from. origin/main is already on
  3.29, so it should bring only the new tag. If the base pins an older commons, list every
  version it skips (3.27–3.29 from the local branch's 3.26).

### Group 5: sirk-orgadmin-frontend — options decide the input; swatch box

Files:
- `src/components/category/PropertyValueInput.tsx`
- `src/components/category/PropertyEditor.tsx`
- `src/components/category/CategoryShow.tsx`
- `src/components/category/inputs/SelectInput.tsx`
- new `src/components/category/OptionSwatch.tsx`
- `src/i18n/en/category.ts`
- `src/i18n/no/category.ts`
- new `src/components/category/__tests__/propertyOptions.test.tsx`
- `src/components/category/__tests__/depreciationGroup.test.tsx` (only if it breaks)
- `openspec/changes/controlled-color/` (already written; commit it with the branch)

Tests: `yarn test --silent 2>&1 | tail -n 40`, plus `yarn lint` and `yarn tsc --noEmit` if the
repo has them.

- [x] 5.1 Test first, in `propertyOptions.test.tsx`, following the spec `category-property-options`:
  - a STRING property with options renders a select with option labels;
  - a stored value outside the options is shown as the selected extra entry;
  - with no options, the property renders a text input;
  - an option whose `attributes.swatch` is `#222222` draws a box, both in the menu and in
    `CategoryShow`;
  - an invalid or missing swatch draws none.
- [x] 5.2 Add `OptionSwatch`, per design D7.
- [x] 5.3 `SelectInput`:
  - `SelectOption` gains `swatch?: string`;
  - each `MenuItem` renders `<OptionSwatch swatch={o.swatch} />` before its text.
- [x] 5.4 `PropertyValueInput`: before the `switch`, any property with non-empty `options` renders
  `SelectInput`, from `options` plus the existing out-of-options extra entry, with the swatch
  taken from `attributes?.swatch`.
  - `DEPRECIATION_GROUP` keeps `depreciation_group_selection`.
  - Other types use the new `option_selection` key: en "Select a value", no "Velg en verdi".
- [x] 5.5 `PropertyEditor.isImmediate`: immediate type, or `!!property.options?.length`.
- [x] 5.6 `CategoryShow` (~line 399): when the property has options, display through
  `enumValueLabel`, with the matching option's swatch before it.
- [x] 5.7 Run the tests. Production code contains no string `color`.

### Group 6: sirk-asset-frontend — swatch box in the editor

Files:
- new `src/components/assets/edit/properties/OptionSwatch.tsx`
- `src/components/assets/edit/properties/editors/EnumPropertyEditor.tsx`
- `src/components/assets/edit/properties/PropertyValueDisplay.tsx`
- `src/types/asset.ts` (only delete `ColorLabel` and `TopColor`)
- `src/i18n/en.json` and `src/i18n/no.json` (only delete the `color.label` block)
- `tests/component/assets/edit/properties/PropertiesSection.options.test.tsx`
- `openspec/changes/controlled-color/` (already written and untracked; carry it to the new branch)

Tests: `yarn test --silent 2>&1 | tail -n 40`

- [ ] 6.1 Test first, in `PropertiesSection.options.test.tsx`, following the spec
  `asset-property-swatches`:
  - an option whose `attributes.swatch` is `#222222` draws a box, both in the select and in the
    read display;
  - an option without a swatch draws none;
  - an out-of-list stored value appears as the marked extra entry, with no box.
- [ ] 6.2 Add `OptionSwatch`, per design D7.
- [ ] 6.3 Render it before `optionLabel(...)`, both in `EnumPropertyEditor`'s `MenuItem`s and in
  `PropertyValueDisplay`'s option branch.
- [ ] 6.4 Grep to confirm nothing reads `ColorLabel`, `TopColor` or the `color.label` i18n blocks,
  then delete them.
- [ ] 6.5 Run the tests. `ASSET_LIST_PROPERTIES`, `EditableOptionChip` and `PropertyValueChip`
  stay untouched.

## Not in scope

These repos and changes need no work:

- **sirk-furniture-taxonomy and sirk-machine-taxonomy.** Their bumps are cut: nothing reads
  commons through them at runtime.
- **sirk-app-backend.** `ClassificationApplier` passes the colour through and skips a null one.
  Its public DPP renders no asset properties.
- **sirk-app-backend-office.** It pins commons 3.19, for class names only.
- **sirk-logistics-taxonomy and sirk-ict-taxonomy.** Neither opts into `common:color`.
- **vardly-frontend.** No colour usage.
- **sirk-frontend-commons.** See design D7.
- **sirk-service-frontend and the marketplace.** Neither shows asset colour.
- **sirk-service-backend.** Its free-text `"Farge"` specification attribute is unrelated.
- **Migrating stored colours.**
- **Validating iris `state`.**
