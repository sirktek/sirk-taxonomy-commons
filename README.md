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
