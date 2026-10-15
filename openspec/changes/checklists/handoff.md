## Handoff — Group 1

Changed: `common-base.ttl` (class `common:ChecklistTemplateEntry` after `AssetValuationEntry`),
`CommonPropertyDefinition` (mapping, `CHECKLIST` constant, javadoc), `CommonPropertyDefinitionTest`
(`mapsChecklistTemplateEntryToChecklist`, seen failing first). `CommonTaxonomyServiceTest` unchanged.

Verified: targeted tests, full `mvnq test`, `openspec validate checklists --strict`.

Unverified: the released version (`3.<build>`) is unknown until merge; downstream repos bump to it.
The "marker stays out of a domain tree" scenario is covered by existing loader behaviour, not a new test.
