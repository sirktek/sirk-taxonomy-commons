
## Handoff — Group 1
Changed: `common-base.ttl` (common:Color + 16 individuals, common:swatch, color range now common:Color), `CommonTaxonomyServiceTest`, `DomainOptInLoaderTest`, `test-domain-optin.ttl` (color opt-in), `README.md` (color row, Colour section).
Verified: full `mvnq test` green, 30 tests, 0 failures. Loader puts `swatch` into `EnumValue.attributes`; color remains an enum property (DomainOptInLoaderTest).
Unverified: nothing known. Released tag: pending merge.

## Handoff — Group 5 (sirk-orgadmin-frontend)
Changed: new `src/components/category/OptionSwatch.tsx` (`OptionSwatch`, plus `OptionValueLabel` = swatch + `enumValueLabel`), `inputs/SelectInput.tsx` (`swatch?`), `PropertyValueInput.tsx` (any non-empty `options` -> select; DEPRECIATION_GROUP keeps its label key; other types `option_selection`), `PropertyEditor.tsx` (`isImmediate` = immediate type or has options), `CategoryShow.tsx` (options -> `OptionValueLabel`), i18n en/no `option_selection`, new `__tests__/propertyOptions.test.tsx` (7 tests).
Verified: `vitest run src/components/category` 15 files / 116 tests green; `tsc --noEmit` and eslint on components/category clean; no `color` in the diff.
Unverified: full `yarn test` suite not run (timed out in the tool; category dir only). CategoryShow swatch is tested via `OptionValueLabel`, not a full CategoryShow render. In CategoryShow the options branch sits after the typed branches (DATE/URL/CATEGORY/BOOLEAN...), so only plain-text types get the label/swatch there.
