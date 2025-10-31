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
- **W3C Standards**: Based on RDF-S and Apache Jena

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
