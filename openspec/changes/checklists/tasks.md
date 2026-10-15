# Tasks — checklists (taxonomy-commons slice)

One group, one implementer in a fresh context with this file, design.md and
`specs/common-taxonomy-loading/spec.md`. Touch only the listed files, run only the listed tests,
tick the tasks, append a short `## Handoff — Group 1` note to `handoff.md` in this directory, and
stop when the tests pass. Maven through `mvnq` (`~/.local/bin/mvnq`, same arguments as
`./mvnw`); never `clean`. No Java code specific to a taxonomy class or property beyond the range
mapping in `CommonPropertyDefinition`.

Before starting: a GitHub issue; branch `feature/#<id>-checklists`.

Suggested model: haiku or sonnet (design pins the class, comment and mapping).

### Group 1: `common:ChecklistTemplateEntry` → `CHECKLIST`

Design C1, C2.

Files: `src/main/resources/taxonomy/common-base.ttl`,
`src/main/java/no/sirktek/taxonomy/model/CommonPropertyDefinition.java`,
`src/test/java/no/sirktek/taxonomy/model/CommonPropertyDefinitionTest.java`,
`src/test/java/no/sirktek/taxonomy/CommonTaxonomyServiceTest.java` (only if a class count changes).
Tests: `mvnq test -Dtest='CommonPropertyDefinitionTest,CommonTaxonomyServiceTest'`

- [x] 1.1 Test first: `mapsChecklistTemplateEntryToChecklist` in `CommonPropertyDefinitionTest`
      (range `http://taxonomy.sirktek.no/common#ChecklistTemplateEntry` → `CHECKLIST`). See it fail.
- [x] 1.2 Add the class to `common-base.ttl` exactly as design C1, after `AssetValuationEntry`.
- [x] 1.3 Add the mapping and the enum constant (design C2); update the class javadoc's marker list.
- [x] 1.4 Run the tests above, then `mvnq test` once, and `openspec validate checklists --strict`.
      Merge releases `3.<build>`; note the released version in the handoff, because
      sirk-logistics-taxonomy, sirk-machine-taxonomy and sirk-orgadmin-backend bump to it.
