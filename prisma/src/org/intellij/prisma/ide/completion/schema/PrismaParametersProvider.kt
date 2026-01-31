package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.PrismaSchemaFakeElement
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.builder.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.builder.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.builder.PrismaSchemaParameterLocation
import org.intellij.prisma.lang.psi.PrismaArgumentsOwner
import org.intellij.prisma.lang.psi.PrismaArrayExpression
import org.intellij.prisma.lang.psi.PrismaFile
import org.intellij.prisma.lang.psi.PrismaFunctionCall
import org.intellij.prisma.lang.psi.PrismaNamedArgument
import org.intellij.prisma.lang.psi.PrismaPathExpression
import org.intellij.prisma.lang.psi.PrismaValueArgument

object PrismaParametersProvider : PrismaCompletionProvider() {
  override val pattern: ElementPattern<out PsiElement> =
    psiElement().withParent(
      psiElement(PrismaPathExpression::class.java).withParent(PrismaValueArgument::class.java)
    )

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet,
  ) {
    val file = parameters.originalFile as? PrismaFile ?: return
    val position = parameters.originalPosition ?: parameters.position
    val datasourceTypes = file.metadata.datasourceTypes
    var argumentsOwner = position.parentOfType<PrismaArgumentsOwner>() ?: return
    val location = if (argumentsOwner is PrismaFunctionCall && argumentsOwner.parent is PrismaArrayExpression)
      PrismaSchemaParameterLocation.FIELD
    else
      PrismaSchemaParameterLocation.DEFAULT
    if (location == PrismaSchemaParameterLocation.FIELD) {
      argumentsOwner = argumentsOwner.parentOfType() ?: return
    }
    val schema = PrismaSchemaProvider.getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(position))
    val schemaDeclaration =
      schema.match(argumentsOwner) as? PrismaSchemaDeclaration ?: return
    val parent = PrismaSchemaFakeElement.createForCompletion(parameters, schemaDeclaration) ?: return
    val usedParams = argumentsOwner.getArgumentsList()?.arguments
                       ?.asSequence()
                       ?.filterIsInstance<PrismaNamedArgument>()
                       ?.map { it.referenceName }
                       ?.toSet()
                     ?: emptySet()

    schemaDeclaration.getAvailableParams(datasourceTypes, location)
      .asSequence()
      .filter { it.label !in usedParams && !it.skipInCompletion }
      .map { createLookupElement(it.label, it, PrismaSchemaFakeElement.createForCompletion(parent, it)) }
      .forEach { result.addElement(it) }
  }
}

