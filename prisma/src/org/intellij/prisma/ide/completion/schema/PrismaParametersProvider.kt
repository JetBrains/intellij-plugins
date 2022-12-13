package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.PrismaSchemaFakeElement
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.psi.*

object PrismaParametersProvider : PrismaCompletionProvider() {
  override val pattern: ElementPattern<out PsiElement> =
    psiElement().withParent(
      psiElement(PrismaPathExpression::class.java).withParent(PrismaValueArgument::class.java)
    )

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    val file = parameters.originalFile as? PrismaFile
    val position = parameters.originalPosition ?: parameters.position
    val datasource = file?.datasourceType
    var argumentsOwner = position.parentOfType<PrismaArgumentsOwner>() ?: return
    val isFieldArgument =
      argumentsOwner is PrismaFunctionCall && argumentsOwner.parent is PrismaArrayExpression
    if (isFieldArgument) {
      argumentsOwner = argumentsOwner.parentOfType() ?: return
    }
    val schemaDeclaration =
      PrismaSchemaProvider.getSchema().match(argumentsOwner) as? PrismaSchemaDeclaration ?: return
    val parent = PrismaSchemaFakeElement.createForCompletion(parameters, schemaDeclaration)
    val usedParams = argumentsOwner.getArgumentsList()?.arguments
                       ?.asSequence()
                       ?.filterIsInstance<PrismaNamedArgument>()
                       ?.map { it.referenceName }
                       ?.toSet()
                     ?: emptySet()

    schemaDeclaration.getAvailableParams(datasource, isFieldArgument)
      .asSequence()
      .filter { it.label !in usedParams && !it.skipInCompletion }
      .map { createLookupElement(it.label, it, PrismaSchemaFakeElement.createForCompletion(parent, it)) }
      .forEach { result.addElement(it) }
  }
}

