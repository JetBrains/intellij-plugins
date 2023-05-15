package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.prisma.PrismaIcons
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.types.PrismaNativeType
import org.intellij.prisma.ide.schema.types.PrismaNativeTypeConstructor
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.unwrapType


object PrismaNativeTypeProvider : PrismaCompletionProvider() {
  override val pattern: ElementPattern<out PsiElement> = psiElement().withParent(
    psiElement(PrismaPathExpression::class.java)
      .withParent(PrismaFieldAttribute::class.java)
      .with("withQualifier") { el ->
        val datasourceName = (el.containingFile as? PrismaFile)?.datasourceName
        val qualifier = el.qualifier

        if (qualifier != null && datasourceName != null) {
          qualifier.textMatches(datasourceName)
        }
        else {
          false
        }
      }
  )

  override fun addCompletions(
    parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
  ) {
    val file = parameters.originalFile as? PrismaFile ?: return
    val datasourceType = file.datasourceType ?: return
    val constructors = PrismaNativeType.findConstructorsByType(datasourceType)
    val fieldType = parameters.position.parentOfType<PrismaFieldDeclaration>()?.type?.unwrapType() ?: return

    constructors
      .filter { fieldType in it.types }
      .map {
        LookupElementBuilder.create(it.name)
          .withIcon(PrismaIcons.ATTRIBUTE)
          .withTailText(it.tailText)
          .withInsertHandler(it.insertHandler)
      }
      .forEach { result.addElement(it) }
  }

  private val PrismaNativeTypeConstructor.tailText: String?
    get() = if (numberOfArgs > 0 || numberOfOptionalArgs > 0) {
      "()"
    }
    else null

  private val PrismaNativeTypeConstructor.insertHandler: InsertHandler<LookupElement>?
    get() = if (numberOfArgs > 0 || numberOfOptionalArgs > 0) {
      ParenthesesInsertHandler.WITH_PARAMETERS
    }
    else null
}