package org.intellij.prisma.ide.schema.definitions

import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import org.intellij.prisma.ide.completion.PrismaInsertHandler
import org.intellij.prisma.ide.schema.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaRef
import org.intellij.prisma.ide.schema.schema
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.intellij.prisma.lang.PrismaConstants.FieldAttributes
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.intellij.prisma.lang.PrismaConstants.ParameterNames
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.PrismaConstants.Types
import org.intellij.prisma.lang.psi.PrismaPsiPatterns
import org.intellij.prisma.lang.types.PrismaBooleanType
import java.util.*

val PRISMA_SCHEMA_ATTRIBUTES = schema {
  group(PrismaSchemaKind.BLOCK_ATTRIBUTE) {
    element {
      label = BlockAttributes.MAP
      insertHandler = PrismaInsertHandler.PARENS_QUOTED_ARGUMENT
      documentation = "Maps a model name from the Prisma schema to a different table name."

      param {
        label = ParameterNames.NAME
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "The name of the target database table."
        type = "String"
      }
    }

    element {
      label = BlockAttributes.ID
      insertHandler = PrismaInsertHandler.PARENS_LIST_ARGUMENT
      documentation = "Defines a multi-field ID on the model."

      param {
        label = ParameterNames.FIELDS
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of references."
        type = "FieldReference[]"
      }
      param {
        label = ParameterNames.NAME
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines the name in your Prisma Client API."
        type = "String?"
      }
      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom name for the primary key in the database."
        type = "String?"
      }
      length(true)
      sort(true, datasourceTypes = EnumSet.of(PrismaDatasourceType.SQLSERVER))
      clustered()
    }

    element {
      label = BlockAttributes.UNIQUE
      insertHandler = PrismaInsertHandler.PARENS_LIST_ARGUMENT
      documentation = "Defines a compound unique constraint for the specified fields."

      param {
        label = ParameterNames.FIELDS
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of references."
        type = "FieldReference[]"
      }
      param {
        label = ParameterNames.NAME
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines the name in your Prisma Client API."
        type = "String?"
      }
      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom constraint name in the database."
        type = "String?"
      }
      length(true)
      sort(true)
      clustered()
    }

    element {
      label = BlockAttributes.INDEX
      insertHandler = PrismaInsertHandler.PARENS_LIST_ARGUMENT
      documentation = "Defines an index on the model."

      param {
        label = ParameterNames.FIELDS
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of references."
        type = "FieldReference[]"
      }
      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom index name in the database."
        type = "String?"
      }
      param {
        label = ParameterNames.TYPE
        documentation = "Defines the access type of indexes: BTree (default) or Hash."
        type = Types.INDEX_TYPE.optional()
        datasources = EnumSet.of(PrismaDatasourceType.POSTGRESQL)

        variantsForType(Types.INDEX_TYPE)
      }
      param {
        label = ParameterNames.OPS
        documentation = "Specify the operator class for an indexed field."
        type = Types.OPERATOR_CLASS.optional()
        datasources = EnumSet.of(PrismaDatasourceType.POSTGRESQL)
        isOnFieldLevel = true

        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.RAW) }
      }
      length(true)
      sort(true)
      clustered()
    }

    element {
      label = BlockAttributes.FULLTEXT
      insertHandler = PrismaInsertHandler.PARENS_LIST_ARGUMENT
      documentation = "Defines a full-text index on the model."
      datasources = EnumSet.of(PrismaDatasourceType.MYSQL, PrismaDatasourceType.MONGODB)

      param {
        label = ParameterNames.FIELDS
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of references."
        type = "FieldReference[]"
      }
      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom index name in the database."
        type = "String?"
      }
    }

    element {
      label = BlockAttributes.IGNORE
      documentation =
        "A model with an `@@ignore` attribute can be kept in sync with the database schema using Prisma Migrate and Introspection, but won't be exposed in Prisma Client."
    }
  }

  group(PrismaSchemaKind.FIELD_ATTRIBUTE) {
    element {
      label = FieldAttributes.ID
      documentation = "Defines a single-field ID on the model."

      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom primary key name in the database."
        type = "String?"
      }
      length()
      sort(datasourceTypes = EnumSet.of(PrismaDatasourceType.SQLSERVER))
      clustered()
    }

    element {
      label = FieldAttributes.UNIQUE
      documentation = "Defines a unique constraint for this field."

      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom name for the unique constraint in the database."
        type = "String?"
      }
      length()
      sort()
      clustered()
    }

    element {
      label = FieldAttributes.MAP
      insertHandler = PrismaInsertHandler.PARENS_QUOTED_ARGUMENT
      documentation = "Maps a field name from the Prisma schema to a different column name."

      param {
        label = ParameterNames.NAME
        documentation = "The name of the target database column."
        type = "String"
      }
    }

    element {
      label = FieldAttributes.DEFAULT
      insertHandler = ParenthesesInsertHandler.WITH_PARAMETERS
      documentation = "Defines a default value for this field. `@default` takes an expression as an argument."

      param {
        label = ParameterNames.EXPRESSION
        documentation = "An expression (e.g. `5`, `true`, `now()`)."
        type = "Expression"
        skipInCompletion = true

        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.DBGENERATED) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.AUTO) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.SEQUENCE) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.AUTOINCREMENT) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.NOW) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.CUID) }
        variant { ref = PrismaSchemaRef(PrismaSchemaKind.FUNCTION, Functions.UUID) }

        booleanTypeValues(PrismaPsiPatterns.withFieldType { type, _ -> type is PrismaBooleanType })
      }
    }

    element {
      label = FieldAttributes.RELATION
      insertHandler = ParenthesesInsertHandler.WITH_PARAMETERS
      documentation =
        "Defines a connection between two models. [Learn more](https://pris.ly/d/relation-attribute)."

      param {
        label = ParameterNames.NAME
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation =
          "Defines the name of the relationship. In an m-n-relation, it also determines the name of the underlying relation table."
        type = "String?"
      }
      param {
        label = ParameterNames.MAP
        insertHandler = PrismaInsertHandler.COLON_QUOTED_ARGUMENT
        documentation = "Defines a custom name for the foreign key in the database."
        type = "String?"
        datasources = PrismaDatasourceType.except(PrismaDatasourceType.MONGODB)
      }
      param {
        label = ParameterNames.FIELDS
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of field references of the *current* model."
        type = "FieldReference[]?"
      }
      param {
        label = ParameterNames.REFERENCES
        insertHandler = PrismaInsertHandler.COLON_LIST_ARGUMENT
        documentation = "A list of field references of the model on *the other side of the relation*."
        type = "FieldReference[]?"
      }
      param {
        label = ParameterNames.ON_DELETE
        documentation =
          "Specifies the action to perform when a referenced entry in the referenced model is being deleted. [Learn more](https://pris.ly/d/referential-actions)."
        type = Types.REFERENTIAL_ACTION.optional()
        datasources = PrismaDatasourceType.except(PrismaDatasourceType.MONGODB)

        variantsForType(Types.REFERENTIAL_ACTION)
      }
      param {
        label = ParameterNames.ON_UPDATE
        documentation =
          "Specifies the action to perform when a referenced field in the referenced model is being updated to a new value. [Learn more](https://pris.ly/d/referential-actions)."
        type = Types.REFERENTIAL_ACTION.optional()
        datasources = PrismaDatasourceType.except(PrismaDatasourceType.MONGODB)

        variantsForType(Types.REFERENTIAL_ACTION)
      }
    }

    element {
      label = FieldAttributes.UPDATED_AT
      documentation = "Automatically stores the time when a record was last updated."
    }

    element {
      label = FieldAttributes.IGNORE
      documentation =
        "A field with an `@ignore` attribute can be kept in sync with the database schema using Prisma Migrate and Introspection, but won't be exposed in Prisma Client."
    }

    element {
      label = FieldAttributes.DB
      documentation =
        "Defines a native database type that should be used for this field. See https://www.prisma.io/docs/concepts/components/prisma-schema/data-model#native-types-mapping."
      insertHandler = PrismaInsertHandler.QUALIFIED_NAME
      datasources = PrismaDatasourceType.except(PrismaDatasourceType.SQLITE)
    }
  }
}

private fun PrismaSchemaDeclaration.Builder.sort(
  isOnField: Boolean = false,
  datasourceTypes: Set<PrismaDatasourceType>? = null
) {
  param {
    label = ParameterNames.SORT
    documentation =
      "Specify in which order the entries of the index are stored in the database. This can have an effect on whether the database is able to use an index for specific queries."
    type = Types.SORT_ORDER.optional()
    datasources = datasourceTypes
    isOnFieldLevel = isOnField

    variantsForType(Types.SORT_ORDER)
  }
}

private fun PrismaSchemaDeclaration.Builder.length(isOnField: Boolean = false) {
  param {
    label = ParameterNames.LENGTH
    documentation =
      "Defines a maximum length for the subpart of the value to be indexed in cases where the full value would exceed MySQL's limits for index sizes. See https://dev.mysql.com/doc/refman/8.0/en/innodb-limits.html"
    type = "Int?"
    datasources = EnumSet.of(PrismaDatasourceType.MYSQL)
    isOnFieldLevel = isOnField
  }
}

private fun PrismaSchemaDeclaration.Builder.clustered() {
  param {
    label = ParameterNames.CLUSTERED
    documentation =
      "An index, unique constraint or primary key can be created as clustered or non-clustered; altering the storage and retrieve behavior of the index."
    type = PrimitiveTypes.BOOLEAN.optional()
    datasources = EnumSet.of(PrismaDatasourceType.SQLSERVER)

    variantsForType(PrimitiveTypes.BOOLEAN)
  }
}