package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.builder.schema
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.SQLITE

val PRISMA_SCHEMA_KEYWORDS = schema {
  group(PrismaSchemaKind.KEYWORD) {
    element {
      label = "datasource"
      documentation = "The datasource block tells the schema where the models are backed."
    }
    element {
      label = "generator"
      documentation =
        "Generator blocks configure which clients are generated and how they're generated. Language preferences and binary configuration will go in here."
    }
    element {
      label = "model"
      documentation =
        "Models represent the entities of your application domain. They are defined using model blocks in the data model."
    }
    element {
      label = "enum"
      documentation =
        "Enums are defined via the enum block. You can define enums in your data model if they're supported by the datasource you use (e.g SQLite: not supported)."
      datasources = PrismaDatasourceProviderType.except(SQLITE)
    }
    element {
      label = "type"
      documentation = "Composite types (known as [embedded documents](https://www.mongodb.com/docs/manual/core/data-model-design/#std-label-data-modeling-embedding) in MongoDB) provide support for embedding records inside other records. [Learn more](https://www.prisma.io/docs/concepts/components/prisma-schema/data-model#defining-composite-types)"
    }
  }
}
