package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.*
import org.intellij.prisma.lang.psi.PrismaFile

abstract class PrismaSchemaCompletionProvider : PrismaCompletionProvider() {
  abstract val kind: PrismaSchemaKind

  final override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    populateProcessingContext(parameters, context)

    collectSchemaElements(parameters, context).forEach { schemaElement ->
      createLookupElement(schemaElement, parameters, context).let { builder ->
        result.addElement(builder)
      }
    }
  }

  protected open fun collectSchemaElements(
    parameters: CompletionParameters,
    context: ProcessingContext
  ): Collection<PrismaSchemaElement> {
    val file = parameters.originalFile as? PrismaFile ?: return emptyList()
    val datasourceType = file.datasourceType
    val position = parameters.originalPosition ?: parameters.position

    return PrismaSchemaProvider
      .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(position))
      .getElementsByKind(kind)
      .asSequence()
      .filter { it.isAvailableForDatasource(datasourceType) }
      .filter { it.isAcceptedByPattern(position, context) }
      .toList()
  }

  protected open fun createLookupElement(
    schemaElement: PrismaSchemaElement,
    parameters: CompletionParameters,
    context: ProcessingContext,
  ): LookupElementBuilder {
    return createLookupElement(
      schemaElement.label,
      schemaElement,
      PrismaSchemaFakeElement.createForCompletion(parameters, schemaElement),
    )
  }
}