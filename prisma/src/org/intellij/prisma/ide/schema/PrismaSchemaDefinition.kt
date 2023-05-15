package org.intellij.prisma.ide.schema

import org.intellij.prisma.ide.schema.definitions.*

object PrismaSchemaProvider {

  private val PRISMA_SCHEMA_DEFINITION = lazy {
    schema {
      compose(PRISMA_SCHEMA_KEYWORDS)
      compose(PRISMA_SCHEMA_PRIMITIVE_TYPES)
      compose(PRISMA_SCHEMA_FIELDS)
      compose(PRISMA_SCHEMA_FIELD_ATTRIBUTES)
      compose(PRISMA_SCHEMA_BLOCK_ATTRIBUTES)
      compose(PRISMA_SCHEMA_FUNCTIONS)
    }
  }

  fun getEvaluatedSchema(evaluationContext: PrismaSchemaEvaluationContext): PrismaEvaluatedSchema =
    PRISMA_SCHEMA_DEFINITION.value.evaluate(evaluationContext)
}