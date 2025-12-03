package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.completion.PrismaInsertHandler
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.builder.schema
import org.intellij.prisma.ide.schema.types.PRISMA_BINARY_TARGETS
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.*
import org.intellij.prisma.ide.schema.types.PrismaPreviewFeature
import org.intellij.prisma.lang.PrismaConstants.DatasourceFields
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.intellij.prisma.lang.PrismaConstants.GeneratorFields
import org.intellij.prisma.lang.PrismaConstants.GeneratorProviderTypes
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.psi.PrismaPsiPatterns
import java.util.*

val PRISMA_SCHEMA_FIELDS = schema {
  group(PrismaSchemaKind.DATASOURCE_FIELD) {
    element {
      label = DatasourceFields.PROVIDER
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
      label = DatasourceFields.URL
      documentation =
        "Connection URL including authentication info. Each datasource provider documents the URL syntax. Most providers use the syntax provided by the database. [Learn more](https://pris.ly/d/connection-strings)."
      type = PrimitiveTypes.STRING
      insertHandler = PrismaInsertHandler.EQUALS
      deprecated = true

      variant {
        ref {
          kind = PrismaSchemaKind.FUNCTION
          label = Functions.ENV
        }
      }
    }
    element {
      label = DatasourceFields.DIRECT_URL
      documentation =
        "Connection URL for direct connection to the database. [Learn more](https://pris.ly/d/data-proxy-cli)."
      type = PrimitiveTypes.STRING
      insertHandler = PrismaInsertHandler.EQUALS
      deprecated = true

      variant {
        ref {
          kind = PrismaSchemaKind.FUNCTION
          label = Functions.ENV
        }
      }
    }
    element {
      label = DatasourceFields.RELATION_MODE
      type = PrimitiveTypes.STRING
      datasources = PrismaDatasourceProviderType.except(MONGODB)
      documentation = "Set the global relation mode for all relations. Values can be either `\"foreignKeys\"` (Default), or `\"prisma\"`. [Learn more](https://pris.ly/d/relationMode)"

      variant {
        label = "foreignKeys"
        type = PrimitiveTypes.STRING
        documentation = "Default value. The relation will use the Foreign Keys from the database."
      }
      variant {
        label = "prisma"
        type = PrimitiveTypes.STRING
        documentation = "The relation will not use Foreign Keys from the database. Prisma Client will emulate their behavior for update and delete queries. [Learn more](https://pris.ly/d/relationMode)"
      }
    }
    element {
      label = DatasourceFields.SHADOW_DATABASE_URL
      documentation =
        "Connection URL including authentication info to use for Migrate's [shadow database](https://pris.ly/d/migrate-shadow)."
      type = PrimitiveTypes.STRING
      insertHandler = PrismaInsertHandler.EQUALS
      deprecated = true

      variant {
        ref {
          kind = PrismaSchemaKind.FUNCTION
          label = Functions.ENV
        }
      }
    }
    element {
      label = DatasourceFields.EXTENSIONS
      insertHandler = PrismaInsertHandler.EQUALS_LIST
      documentation = "Enable PostgreSQL extensions. [Learn more](https://pris.ly/d/postgresql-extensions)"
      datasources = EnumSet.of(POSTGRESQL)
      type = "[]" // a type should be `Any[]` or even better `Extension[]`
    }
    element {
      label = DatasourceFields.SCHEMAS
      insertHandler = PrismaInsertHandler.EQUALS_LIST
      documentation = "The list of database schemas. [Learn More](https://pris.ly/d/multi-schema-configuration)"
      datasources = EnumSet.of(POSTGRESQL, COCKROACHDB, SQLSERVER)
      type = "String[]"
    }
  }

  group(PrismaSchemaKind.GENERATOR_FIELD) {
    element {
      label = GeneratorFields.PROVIDER
      documentation =
        "Describes which generator to use. This can point to a file that implements a generator or specify a built-in generator directly. [Learn more](https://pris.ly/d/generator-fields)."
      type = PrimitiveTypes.STRING

      variant {
        label = GeneratorProviderTypes.PRISMA_CLIENT_JS
        documentation = "Built-in generator."
        type = PrimitiveTypes.STRING
      }

      variant {
        label = GeneratorProviderTypes.PRISMA_CLIENT
        documentation = "Newer and more flexible version of `prisma-client-js` with ESM support; it outputs plain TypeScript code and requires a custom output path. **(Early Access)**"
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = GeneratorFields.OUTPUT
      documentation = "Determines the location for the generated client. [Learn more](https://pris.ly/d/generator-fields)."
      type = PrimitiveTypes.STRING
    }
    element {
      label = GeneratorFields.BINARY_TARGETS
      documentation =
        "Specifies the OS on which the Prisma Client will run to ensure compatibility of the query engine. Default: `native`. [Learn more](https://pris.ly/d/generator-fields)."
      type = "String[]"

      PRISMA_BINARY_TARGETS.forEach {
        variant {
          label = it
          type = PrimitiveTypes.STRING
        }
      }
    }
    element {
      label = GeneratorFields.PREVIEW_FEATURES
      documentation = "Enables preview feature flags."
      type = "String[]"

      PrismaPreviewFeature.entries.forEach {
        variant {
          label = it.presentation
          type = PrimitiveTypes.STRING
          datasources = it.datasources
        }
      }
    }
    element {
      label = GeneratorFields.ENGINE_TYPE
      documentation = "Defines the query engine type for Prisma Client. Default: `library`. [Learn more](https://pris.ly/d/client-engine-type)."
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
        label = "client"
        documentation = "TypeScript based query execution. WebAssembly library for query compilation."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = GeneratorFields.RUNTIME
      documentation = "Target runtime environment. Default: `nodejs`."
      type = PrimitiveTypes.STRING
      pattern = PrismaPsiPatterns.withGeneratorProvider(GeneratorProviderTypes.PRISMA_CLIENT)

      variant {
        label = "nodejs"
        documentation = "Node.js runtime."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "deno"
        documentation = "Deno runtime."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "bun"
        documentation = "Bun runtime."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "workerd"
        documentation = "Cloudflare Workers runtime."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cloudflare"
        documentation = "Alias for `workerd`."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "edge-light"
        documentation = "Alias for `vercel-edge`."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "vercel-edge"
        documentation = "Alias for `edge-light`."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "react-native"
        documentation = "React Native runtime."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = GeneratorFields.MODULE_FORMAT
      documentation = "Determines whether the generated code supports ESM (uses `import`) or CommonJS (uses `require(...)`) modules. Default: inferred from environment."
      type = PrimitiveTypes.STRING
      pattern = PrismaPsiPatterns.withGeneratorProvider(GeneratorProviderTypes.PRISMA_CLIENT)

      variant {
        label = "esm"
        documentation = "ECMAScript modules format."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cjs"
        documentation = "CommonJS modules format."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = GeneratorFields.GENERATED_FILE_EXTENSION
      documentation = "File extension for generated TypeScript files. Default: `ts`."
      type = PrimitiveTypes.STRING
      pattern = PrismaPsiPatterns.withGeneratorProvider(GeneratorProviderTypes.PRISMA_CLIENT)

      variant {
        label = "ts"
        documentation = "TypeScript file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "mts"
        documentation = "TypeScript ESM file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cts"
        documentation = "TypeScript CJS file extension."
        type = PrimitiveTypes.STRING
      }
    }
    element {
      label = GeneratorFields.IMPORT_FILE_EXTENSION
      documentation = "File extension used in import statements. Default: inferred from environment."
      type = PrimitiveTypes.STRING
      pattern = PrismaPsiPatterns.withGeneratorProvider(GeneratorProviderTypes.PRISMA_CLIENT)

      variant {
        label = "ts"
        documentation = "TypeScript file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "mts"
        documentation = "TypeScript ESM file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cts"
        documentation = "TypeScript CJS file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "js"
        documentation = "JavaScript file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "mjs"
        documentation = "JavaScript ESM file extension."
        type = PrimitiveTypes.STRING
      }
      variant {
        label = "cjs"
        documentation = "JavaScript CJS file extension."
        type = PrimitiveTypes.STRING
      }
    }
  }
}
