package org.intellij.prisma.ide.completion.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.*
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.PrismaElementTypes.*
import org.intellij.prisma.lang.types.isListType


object PrismaValuesProvider : PrismaCompletionProvider() {
  private val keyValue = psiElement()
    .withElementType(TokenSet.create(STRING_LITERAL, IDENTIFIER))
    .withParent(PrismaExpression::class.java)
    .afterLeafSkipping(
      psiElement().andOr(psiElement().whitespaceCommentEmptyOrError(), psiElement(LBRACKET)),
      psiElement().withElementType(TokenSet.create(EQ, COMMA)),
    )
    .inside(psiElement().withElementType(TokenSet.create(DATASOURCE_DECLARATION, GENERATOR_DECLARATION)))

  private val paramValue = psiElement()
    .withSuperParent(2, psiElement().withElementType(TokenSet.create(NAMED_ARGUMENT, VALUE_ARGUMENT)))

  override val pattern: ElementPattern<out PsiElement> =
    StandardPatterns.or(keyValue, paramValue)

  override fun addCompletions(
    parameters: CompletionParameters,
    context: ProcessingContext,
    result: CompletionResultSet
  ) {
    val file = parameters.originalFile as? PrismaFile ?: return
    val parent =
      parameters.position.parentOfTypes(PrismaArgument::class, PrismaMemberDeclaration::class) ?: return
    val schemaElement = PrismaSchemaProvider.getSchema().match(parent) ?: return

    val isInString = parameters.position.elementType == STRING_LITERAL
    val listExpression = findListExpression(parameters)
    val isListExpected = isListType(schemaElement.type)
    if (isListExpected && listExpression == null) {
      return
    }

    val datasourceType = file.datasourceType
    val usedValues = mutableSetOf<String>()
    listExpression?.expressionList?.mapNotNullTo(usedValues) { PrismaSchemaContext.getSchemaLabel(it) }

    schemaElement.variants
      .expandRefs()
      .asSequence()
      .filter { it.label !in usedValues }
      .filterNot { isInString && it is PrismaSchemaDeclaration }
      .filter { it.isAvailableForDatasource(datasourceType) && it.isAcceptedByPattern(parameters.position) }
      .map {
        val label = computeLabel(it, parameters)
        createLookupElement(label, it, PrismaSchemaFakeElement.createForCompletion(parameters, it))
          .withPresentableText(it.label)
          .withLookupString(it.label)
      }
      .forEach { result.addElement(it) }
  }

  private fun findListExpression(parameters: CompletionParameters): PrismaArrayExpression? {
    var position: PsiElement? = parameters.originalPosition ?: parameters.position
    if (position.elementType == RBRACKET) {
      position = PsiTreeUtil.skipWhitespacesAndCommentsBackward(position)
    }
    return position?.parentOfType()
  }

  private fun computeLabel(schemaElement: PrismaSchemaElement, parameters: CompletionParameters): String {
    return when (schemaElement) {
      is PrismaSchemaVariant -> {
        val wrapInQuotes =
          schemaElement.type == PrimitiveTypes.STRING && parameters.position.elementType != STRING_LITERAL
        return if (wrapInQuotes) StringUtil.wrapWithDoubleQuote(schemaElement.label) else schemaElement.label
      }

      else -> schemaElement.label
    }
  }
}