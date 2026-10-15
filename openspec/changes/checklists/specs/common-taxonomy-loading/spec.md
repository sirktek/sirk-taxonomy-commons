## ADDED Requirements

### Requirement: Checklist template range

Commons SHALL declare the range marker class `common:ChecklistTemplateEntry`, whose `rdfs:comment`
describes one JSON-encoded ChecklistTemplate v1. `CommonPropertyDefinition.getPropertyType` SHALL
map the range `common:ChecklistTemplateEntry` to `PropertyType.CHECKLIST`. A property with this
range holds the whole template as one JSON string. The class SHALL NOT appear in a domain tree.

#### Scenario: Checklist type
- **WHEN** `CommonPropertyDefinition.getPropertyType` is called for a property with range `http://taxonomy.sirktek.no/common#ChecklistTemplateEntry`
- **THEN** it returns `CHECKLIST`

#### Scenario: Marker stays out of a domain tree
- **WHEN** a domain loader loads a taxonomy whose property has range `common:ChecklistTemplateEntry`
- **THEN** no category of the tree has the URI `http://taxonomy.sirktek.no/common#ChecklistTemplateEntry`
