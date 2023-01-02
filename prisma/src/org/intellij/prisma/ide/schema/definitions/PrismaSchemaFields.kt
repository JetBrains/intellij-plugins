package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.completion.PrismaInsertHandler
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaRef
import org.intellij.prisma.ide.schema.schema
import org.intellij.prisma.ide.schema.types.PRISMA_BINARY_TARGETS
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.ide.schema.types.PrismaPreviewFeatures
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes

val PRISMA_SCHEMA_FIELDS = schema {
  group(PrismaSchemaKind.DATASOURCE_FIELD) {
    element {
      label = "provider"
      documentation =
        "Describes which datasource connector to use. Can be one of the following datasource providers: `postgresql`, `mysql`, `sqlserver`, `sqlite`, `mongodb` or `cockroachdb`."
      type = PrimitiveTypes.STRING

      variant {
        label = "mysql"
        documentation =
          "Specifies a MySQL datasource. Learn more about this connector [here](https://pris.ly/d/mysql-connector)."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "postgresql"
        documentation =
          "Specifies a PostgreSQL datasource. Learn more about this connector [here](https://pris.ly/d/postgresql-connector)."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "sqlite"
        documentation =
          "Specifies a SQLite datasource. Learn more about this connector [here](https://pris.ly/d/sqlite-connector)."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "sqlserver"
        documentation =
          "Specifies a Microsoft SQL Server datasource. Learn more about this connector [here](https://pris.ly/d/sqlserver-connector)."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "mongodb"
        documentation =
          "Specifies a MongoDB datasource. Learn more about this connector [here](https://pris.ly/d/mongodb-connector)."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cockroachdb"
        documentation =
          "Specifies a CockroachDB datasource. Learn more about this connector [here](https://pris.ly/d/cockroachdb-connector)."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = "url"
      documentation =
        "Connection URL including authentication info. Each datasource provider documents the URL syntax. Most providers use the syntax provided by the database [learn more](https://pris.ly/d/connection-strings)."
      type = PrimitiveTypes.STRING
      insertHandler = PrismaInsertHandler.EQUALS

      variant {
        ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.ENV)
      }
    }
    element {
      label = "relationMode"
      type = PrimitiveTypes.STRING
      datasources = PrismaDatasourceType.except(PrismaDatasourceType.MONGODB)

      variant {
        label = "foreignKeys"
        type = PrimitiveTypes.STRING
        documentation = "Default value. The relation will use the Foreign Keys from the database."
      }
      variant {
        label = "prisma"
        type = PrimitiveTypes.STRING
        documentation = "The relation will not use Foreign Keys from the database. Prisma Client will emulate their behavior for update and delete queries [learn more](https://pris.ly/d/relationMode)"
      }
    }
    element {
      label = "shadowDatabaseUrl"
      documentation =
        "Connection URL including authentication info to use for Migrate's [shadow database](https://pris.ly/d/migrate-shadow). Each datasource provider documents the URL syntax. Most providers use the syntax provided by the database."
      type = PrimitiveTypes.STRING
    }
  }

  group(PrismaSchemaKind.GENERATOR_FIELD) {
    element {
      label = "provider"
      documentation =
        "Describes which generator to use. This can point to a file that implements a generator or specify a built-in generator directly."
      type = PrimitiveTypes.STRING

      variant {
        label = "prisma-client-js"
        documentation = "Built-in generator."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = "output"
      documentation =
        "Determines the location for the generated client [learn more](https://pris.ly/d/prisma-schema)"
      type = PrimitiveTypes.STRING
    }
    element {
      label = "binaryTargets"
      documentation =
        "Specifies the OS on which the Prisma Client will run to ensure binary compatibility of the query engine."
      type = "String[]"

      PRISMA_BINARY_TARGETS.forEach {
        variant {
          label = it
          type = PrimitiveTypes.STRING
        }
      }
    }
    element {
      label = "previewFeatures"
      documentation = "Enables preview feature flags."
      type = "String[]"

      PrismaPreviewFeatures.values().forEach {
        variant {
          label = it.presentation
          type = PrimitiveTypes.STRING
        }
      }
    }
    element {
      label = "engineType"
      documentation = "Defines the query engine type for Prisma Client."
      type = PrimitiveTypes.STRING

      variant {
        label = "library"
        documentation = "Node-API library. (Default)"
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "binary"
        documentation = "Executable binary."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "dataproxy"
        documentation = "Prisma Data Proxy."
        type = PrimitiveTypes.STRING
      }
    }
  }
}