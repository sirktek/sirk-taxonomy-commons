# Sirktek Taxonomy Commons

Common base library for Sirktek RDF-S taxonomy projects. This library provides shared model classes, RDF-S loader, and service layer used by domain-specific taxonomy libraries.

## Overview

This library is the foundation for Sirktek's taxonomy suite:
- **taxonomy-commons** (this project): Common base library
- **furniture-taxonomy**: Furniture classification taxonomy
- **logistics-taxonomy**: Logistics and location taxonomy

## Features

- **Common Model Classes**: `CategoryInfo`, `TaxonomyTree`, `PropertyDefinition`
- **Abstract RDF-S Loader**: Base loader using Apache Jena for parsing RDF-S Turtle files
- **Taxonomy Service**: Shared service layer with caching support
- **Bilingual Support**: Norwegian and English labels in RDF-S
- **Stable Negative IDs**: `RdfsCategoryIds` derives stable Long IDs from category URIs
- **W3C Standards**: Based on RDF-S and Apache Jena

## Negative ID Derivation for RDF-S Categories

RDF-S categories live outside any database, so they have no auto-generated primary key. To let API consumers refer to them by a numeric ID — using the same shape as DB-backed category IDs (positive `BIGINT`) — `RdfsCategoryIds` derives a stable **negative** `Long` from the category's URI.

### Algorithm

```
id = (first 8 bytes of SHA-256(uri, UTF-8) as big-endian long) | Long.MIN_VALUE
```

The sign bit is forced on, so the result is always negative.

### Properties

- **Deterministic**: same URI → same ID across services and JVMs. No coordination needed between services.
- **Disjoint from DB IDs**: DB IDs are positive auto-generated `BIGINT`s; RDF-S IDs are always negative. The two ranges never collide.
- **Low collision risk**: 63 bits of entropy from SHA-256. Birthday-bound is ~2³¹·⁵ URIs before 50% collision odds — far beyond any realistic taxonomy size.
- **`null` URI** maps to `-1` for safety.

### Usage

```java
import no.sirktek.taxonomy.model.RdfsCategoryIds;

Long id = RdfsCategoryIds.negativeIdFromUri("http://taxonomy.sirktek.no/furniture#Chair");
// → -331536201201429814

// Or via the convenience method on CategoryInfo:
Long id = categoryInfo.negativeId();
```

### Stability contract

This ID is a **load-bearing contract** across all consumers of this library. Any change to the algorithm is a breaking change: caches, API responses, exported data, and clients holding old IDs will all become stale. Treat algorithm changes as a coordinated multi-service migration.

## Cross-cutting Accounting and Tax Properties

`common-base.ttl` defines these properties for fixed assets; domain taxonomies opt in with
`schema:domainIncludes` in their own Turtle file:

| Property | Label (no) | Range | Jurisdiction |
|----------|------------|-------|--------------|
| `common:ledgerAccount` | Regnskapskonto | `xsd:string` (e.g. `1200`) | any |
| `common:valuations` | Verdsettelser | `common:AssetValuationEntry` | any |
| `common:depreciationGroup` | Saldogruppe | `common:DepreciationGroup` enumeration | any (values tagged) |

All three carry `common:privateSet "accounting"`, and use the names and shape of ledger17's
accounting taxonomy. `common:valuations` holds one entry per fiscal year,
`{year, bookValue, taxValue, wealthValue}` (`bookValue` is the balance-sheet carrying amount,
`taxValue` the tax written-down value the depreciation group's rate is applied to, `wealthValue`
the Norwegian formuesverdi). `CommonPropertyDefinition` maps the range to
`PropertyType.ASSET_VALUATIONS`; `common:depreciationGroup` maps to `PropertyType.DEPRECIATION_GROUP`.

### Private property sets

`common:privateSet` annotates a property with the key of the private property set its values belong
to (`accounting`, `commercial`); the loader surfaces it as `PropertyDefinition.privateSet()`
(`null` for a public property).

### Commercial terms

Jurisdiction-neutral, `common:privateSet "commercial"`, no domain (domains opt in):

| Property | Label (no) | Range |
|----------|------------|-------|
| `common:marketValue` | Markedsverdi | `xsd:decimal` |
| `common:marketValueDate` | Verdsettelsesdato | `xsd:date` |
| `common:monthlyRent` | Månedsleie (eks. mva) | `xsd:decimal` |
| `common:monthlyServiceCharge` | Felleskostnader per måned (eks. mva) | `xsd:decimal` |

### Opting in from a domain

`RdfsTaxonomyLoader` merges `common-base.ttl` into the model before the domain's own Turtle file, so
a `common:<property> schema:domainIncludes <domain>:<Class>` triple in the domain file attaches the
property to that class. The property keeps its local name as `name` and the full IRI as `uri`.
Only classes of the loader's own namespace appear in the tree. A domain must not opt a class into a
commons property whose local name it also declares on that class.

`common:depreciationGroup` is one global property whose value list is per country: every
instance of `common:DepreciationGroup` carries `common:jurisdiction`. Today the instances are the
Norwegian saldogrupper `common:DepreciationGroupA` … `J` (skatteloven § 14-41, tag `NO`), each
with a `skos:notation` ("a" … "j"), bilingual labels and the statutory maximum yearly rate in
`common:depreciationRate`. Another country adds its own instances with its own tag.

### Enumeration values

When a property's range is a class with instances in the loaded model, the loader exposes them
as `PropertyDefinition.enumValues()`: a list of `EnumValue(uri, name, notation, englishLabel,
norwegianLabel, description, jurisdiction, attributes)` sorted by notation then name.
`attributes` holds the remaining literal statements keyed by local name, e.g.
`depreciationRate=4`. A consumer shows the values whose `jurisdiction` is `null` or equals the
organisation's country, and needs no hardcoded list. This applies to every enumeration-typed
property, e.g. `logistics:type` → `logistics:LocationType`.

### Jurisdiction tagging

Country-specific definitions stay in the `common:` namespace but carry
`common:jurisdiction "<ISO 3166-1 alpha-2>"`. The tag works at two levels. On a property it means the whole property only applies in that
country (no commons property uses it today). On an enumeration value it means only that value
applies there: currently the saldogruppe instances.
`RdfsTaxonomyLoader` surfaces the property-level annotation as `PropertyDefinition.jurisdiction()`
(`null` when the property applies everywhere) so a consumer can hide the property for
organisations outside that country, and the value-level one as `EnumValue.jurisdiction()`.

Typical defaults per domain (set on the category in the consumer, not in the taxonomy):
`logistics:RealEstate` → h (i for office buildings, j for fixed technical installations),
`machine:Machine` → d, `ict:Hardware`/`ict:Software` → a, `furniture:Furniture` → d.

## Maven Dependency

```xml
<dependency>
    <groupId>no.sirktek</groupId>
    <artifactId>taxonomy-commons</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

This library is not meant to be used directly. Instead, use domain-specific taxonomy libraries that extend this commons library:

- **furniture-taxonomy**: For furniture classification
- **logistics-taxonomy**: For logistics and location hierarchies

## For Library Developers

To create a new taxonomy library using this commons:

1. Add dependency on `taxonomy-commons`
2. Extend `RdfsTaxonomyLoader` and implement:
   - `getNamespace()`: Return your taxonomy namespace URI
   - `getResourcePath()`: Return path to your RDF-S Turtle file
3. Create a service class that instantiates `TaxonomyService` with your loader
4. Define your taxonomy in RDF-S Turtle format

Example:

```java
public class MyTaxonomyLoader extends RdfsTaxonomyLoader {
    @Override
    protected String getNamespace() {
        return "http://taxonomy.sirktek.no/my-domain#";
    }

    @Override
    protected String getResourcePath() {
        return "/taxonomy/my-taxonomy.ttl";
    }
}

public class MyTaxonomyService extends TaxonomyService {
    public MyTaxonomyService() {
        super(new MyTaxonomyLoader());
    }
}
```

## Architecture

- **Model Layer**: Core data structures for taxonomy representation
- **Loader Layer**: Abstract RDF-S loader using Apache Jena
- **Service Layer**: Caching and high-level API

## Technology Stack

- Java 17
- Apache Maven 3.9+
- Apache Jena 5.5.0 for RDF processing
- Lombok 1.18.36 for code generation
- JUnit Jupiter 5.11.3 for testing

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Releases

Every merge to `main` tags `3.<run number>` and publishes it to GitHub Packages. Maven Central
(monthly publishing limit) is manual: run "Publish release to Maven Central" with the tag when a
Central consumer, such as sirk-app-backend, needs that version.
