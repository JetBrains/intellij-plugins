package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.collectExistingMemberNames
import org.intellij.prisma.ide.schema.PrismaSchemaElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.lang.psi.PrismaPsiPatterns
import org.intellij.prisma.lang.psi.afterNewLine

object PrismaGeneratorFieldsProvider : PrismaSchemaCompletionProvider() {
  override val kind: PrismaSchemaKind = PrismaSchemaKind.GENERATOR_FIELD

  override val pattern: ElementPattern<out PsiElement> = PrismaPsiPatterns.generatorField.afterNewLine()

  override fun collectSchemaElements(
    parameters: CompletionParameters,
    context: ProcessingContext
  ): Collection<PrismaSchemaElement> {
    val memberNames = collectExistingMemberNames(parameters)
    return super.collectSchemaElements(parameters, context).filter { it.label !in memberNames }
  }
}

