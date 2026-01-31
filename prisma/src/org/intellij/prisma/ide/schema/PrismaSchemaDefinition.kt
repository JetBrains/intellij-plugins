package org.intellij.prisma.ide.schema

import com.intellij.psi.PsiElement
import org.intellij.prisma.ide.schema.builder.PrismaEvaluatedSchema
import org.intellij.prisma.ide.schema.builder.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.builder.schema
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_BLOCK_ATTRIBUTES
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_FIELDS
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_FIELD_ATTRIBUTES
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_FUNCTIONS
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_KEYWORDS
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_PRIMITIVE_TYPES
import org.intellij.prisma.ide.schema.definitions.PRISMA_SCHEMA_VALUES

object PrismaSchemaProvider {

  private val PRISMA_SCHEMA_DEFINITION = lazy {
    schema {
      compose(PRISMA_SCHEMA_KEYWORDS)
      compose(PRISMA_SCHEMA_PRIMITIVE_TYPES)
      compose(PRISMA_SCHEMA_FIELDS)
      compose(PRISMA_SCHEMA_FIELD_ATTRIBUTES)
      compose(PRISMA_SCHEMA_BLOCK_ATTRIBUTES)
      compose(PRISMA_SCHEMA_FUNCTIONS)
      compose(PRISMA_SCHEMA_VALUES)
    }
  }

  fun getEvaluatedSchema(evaluationContext: PrismaSchemaEvaluationContext): PrismaEvaluatedSchema =
    PRISMA_SCHEMA_DEFINITION.value.evaluate(evaluationContext)

  fun getEvaluatedSchema(element: PsiElement): PrismaEvaluatedSchema =
    getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(element))
}