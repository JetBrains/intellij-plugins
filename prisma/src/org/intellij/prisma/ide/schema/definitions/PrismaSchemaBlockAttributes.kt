package org.intellij.prisma.ide.schema.definitions

import com.intellij.patterns.PlatformPatterns.psiElement
import org.intellij.prisma.ide.completion.PrismaInsertHandler
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaRef
import org.intellij.prisma.ide.schema.schema
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.lang.PrismaConstants.BlockAttributes
import org.intellij.prisma.lang.PrismaConstants.Functions
import org.intellij.prisma.lang.PrismaConstants.ParameterNames
import org.intellij.prisma.lang.PrismaConstants.Types
import org.intellij.prisma.lang.psi.PrismaEnumDeclaration
import org.intellij.prisma.lang.psi.PrismaModelDeclaration
import org.intellij.prisma.lang.psi.PrismaPsiPatterns
import java.util.*

val PRISMA_SCHEMA_BLOCK_ATTRIBUTES = schema {
  group(PrismaSchemaKind.BLOCK_ATTRIBUTE) {
    element {
      label = BlockAttributes.MAP
      insertHandler = PrismaInsertHandler.PARENS_QUOTED_ARGUMENT
      documentation = "Maps a model name from the Prisma schema to a different table name."
      pattern = PrismaPsiPatterns.insideEntityDeclaration(
        psiElement().andOr(psiElement(PrismaModelDeclaration::class.java), psiElement(PrismaEnumDeclaration::class.java)))

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
      pattern = PrismaPsiPatterns.insideEntityDeclaration(psiElement(PrismaModelDeclaration::class.java))

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
      pattern = PrismaPsiPatterns.insideEntityDeclaration(psiElement(PrismaModelDeclaration::class.java))

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
      pattern = PrismaPsiPatterns.insideEntityDeclaration(psiElement(PrismaModelDeclaration::class.java))

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
      pattern = PrismaPsiPatterns.insideEntityDeclaration(psiElement(PrismaModelDeclaration::class.java))

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
      pattern = PrismaPsiPatterns.insideEntityDeclaration(psiElement(PrismaModelDeclaration::class.java))
    }
  }
}