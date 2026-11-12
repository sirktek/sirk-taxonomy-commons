
## Handoff — Group 1
Changed: `common-base.ttl` (common:Color + 16 individuals, common:swatch, color range now common:Color), `CommonTaxonomyServiceTest`, `DomainOptInLoaderTest`, `test-domain-optin.ttl` (color opt-in), `README.md` (color row, Colour section).
Verified: full `mvnq test` green, 30 tests, 0 failures. Loader puts `swatch` into `EnumValue.attributes`; color remains an enum property (DomainOptInLoaderTest).
Unverified: nothing known. Released tag: pending merge.

## Handoff — Group 5 (sirk-orgadmin-frontend)
Changed: new `src/components/category/OptionSwatch.tsx` (`OptionSwatch`, plus `OptionValueLabel` = swatch + `enumValueLabel`), `inputs/SelectInput.tsx` (`swatch?`), `PropertyValueInput.tsx` (any non-empty `options` -> select; DEPRECIATION_GROUP keeps its label key; other types `option_selection`), `PropertyEditor.tsx` (`isImmediate` = immediate type or has options), `CategoryShow.tsx` (options -> `OptionValueLabel`), i18n en/no `option_selection`, new `__tests__/propertyOptions.test.tsx` (7 tests).
Verified: `vitest run src/components/category` 15 files / 116 tests green; `tsc --noEmit` and eslint on components/category clean; no `color` in the diff.
Unverified: full `yarn test` suite not run (timed out in the tool; category dir only). CategoryShow swatch is tested via `OptionValueLabel`, not a full CategoryShow render. In CategoryShow the options branch sits after the typed branches (DATE/URL/CATEGORY/BOOLEAN...), so only plain-text types get the label/swatch there.

## Handoff — Group 2 (sirk-asset-classifier)
Changed (branch `feature/#11-controlled-color`, uncommitted): `app/config.py` (`color_source` pinned to tag 3.30, `color_class`, `color_property`), `app/taxonomy/loader.py` (`_parse`, `load_enum_locals`, `Taxonomy.has_property`, domainIncludes collected in `_from_graph`; `from_ttl`/`load` kept), `app/main.py` (`_load_colors`, `_load_classifiers(taxonomies, colors)`), `app/integrations/anthropic_client.py` (COLOR_LABELS deleted, colours passed to prompt/parse, colour rules, off-list/missing colour -> None, has_property gate), `app/routes/classify.py` (None colour -> null), `app/schemas.py`, `infra/setup.sh` (fetch common-base.ttl, `COLOR_SOURCE=` in env file), `docs/backend-integration.md` ("Black"/"Gray"), tests (`test_taxonomy_loader`, `test_taxonomy_config`, `test_anthropic_client`, `conftest`, `test_routes`), new fixture `tests/fixtures/mini-common.ttl`, `mini-taxonomy.ttl` extended with `common:color schema:domainIncludes furniture:Furniture`. The existing `test_lifespan_loads_the_rest_when_one_fails` now sets `color_source` to the fixture (it would otherwise fetch the URL).
Verified: `make test` (402 pass), `make lint`, `make typecheck` green; `bash -n infra/setup.sh` only.
Unverified: `infra/setup.sh` was not run; 3.30 URL fetched nowhere in tests.
Deploy note: deploying iris means rerunning `infra/setup.sh` (after Group 3). That run fetches `common-base.ttl` to `${TAXONOMY_DIR}` and writes `COLOR_SOURCE` into the env file. Without it iris fetches the default URL at every startup and loses offline operation. Until iris is redeployed it still emits lowercase colour words.

## Handoff — Group 3 (sirk-orgadmin-backend)
Changed: `pom.xml` (taxonomy-commons 3.29 -> **3.31**; the tag with common:Color is 3.31, not 3.30, which predates it), `CategoryInheritancePinTest.EXPECTED_JSON` (color entry: targetRootClass Color + 16 options; descriptions only on Metallic/Transparent/Multicolored), `RdfsCategoryPropertyInheritanceTest` (new `colorIsAStringWithSixteenOrderedOptionsOnChairFurnitureAndMachine`: STRING, 16 options in order, Black swatch #222222). No production code changes.
Verified (branch feature/#259-controlled-color, uncommitted): targeted 3 classes 50 tests green; `mvnq -DskipITs=false verify` 2184 tests, 0 failures, BUILD SUCCESS; `mvnq -U pmd:check` BUILD SUCCESS.
Unverified: nothing known.

## Handoff — Group 6 (sirk-asset-frontend)
Changed (branch `feature/#107-controlled-color`, uncommitted): new `src/components/assets/edit/properties/OptionSwatch.tsx` (renders only for `^#[0-9a-fA-F]{6}$`, 12x12 aria-hidden span, divider border, `data-testid="option-swatch"`); `editors/EnumPropertyEditor.tsx` and `PropertyValueDisplay.tsx` draw it before `optionLabel`; deleted `ColorLabel`/`TopColor` (`src/types/asset.ts`) and the `color.label` block in `en.json`/`no.json` (grep showed no readers); 3 new swatch tests in `PropertiesSection.options.test.tsx` (box in select and read view, none for invalid/missing swatch, none for out-of-list value).
Verified: `vitest run tests/component/assets/edit/properties` 11 files / 84 tests green; `tsc --noEmit` and eslint on changed files clean. Full `yarn test`: 5 files / 11 tests failed under load (ConsumeFromLotDialog, useBatchRegisterSession, AssetDetailPage.consumeButton, AssetsPage.activeFilters/filterFold/mapError — none touch this change); rerun of those 6 files alone: 44/44 green.
Unverified: nothing else. `ASSET_LIST_PROPERTIES`, `EditableOptionChip`, `PropertyValueChip` untouched. Not committed or pushed.
