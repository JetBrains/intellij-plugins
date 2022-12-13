package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.completion.PrismaInsertHandler
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.schema
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes

val PRISMA_SCHEMA_PRIMITIVE_TYPES = schema {
  group(PrismaSchemaKind.PRIMITIVE_TYPE) {
    element {
      label = PrimitiveTypes.STRING
      documentation = "Variable length text"
    }
    element {
      label = PrimitiveTypes.BOOLEAN
      documentation = "True or false value"
    }
    element {
      label = PrimitiveTypes.INT
      documentation = "Integer value"
    }
    element {
      label = PrimitiveTypes.FLOAT
      documentation = "Floating point number"
    }
    element {
      label = PrimitiveTypes.DATETIME
      documentation = "Timestamp"
    }
    element {
      label = PrimitiveTypes.JSON
      documentation = "A JSON object"
    }
    element {
      label = PrimitiveTypes.BYTES
    }
    element {
      label = PrimitiveTypes.DECIMAL
      documentation = "Decimal value"
      datasources = PrismaDatasourceType.except(PrismaDatasourceType.MONGODB)
    }
    element {
      label = PrimitiveTypes.BIGINT
      documentation = "Integer values that may be greater than 2^53"
    }
    element {
      label = PrimitiveTypes.UNSUPPORTED
      documentation =
        "An arbitrary database column type, for which Prisma has no syntax. Fields of type `Unsupported` work with Prisma Migrate and introspection, but are not exposed in Prisma Client."
      signature = "Unsupported(name: String)"
      insertHandler = PrismaInsertHandler.PARENS_QUOTED_ARGUMENT

      param {
        label = "name"
        type = "String"
        documentation =
          "Name of the column type as expected by the underlying database, e.g. Unsupported(\"GEOGRAPHY(POINT,4326)\"). This string is not validated by Prisma Migrate and will be used by Prisma Migrate to generate the DDL statements to evolve the database schema. Prisma Introspect will overwrite this when re-introspecting if the type does not match."
      }
    }
  }
}