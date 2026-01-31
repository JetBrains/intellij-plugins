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
import com.intellij.util.asSafely
import org.intellij.prisma.ide.completion.PrismaCompletionProvider
import org.intellij.prisma.ide.schema.PrismaSchemaFakeElement
import org.intellij.prisma.ide.schema.PrismaSchemaPath
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.ide.schema.builder.PrismaSchemaDeclaration
import org.intellij.prisma.ide.schema.builder.PrismaSchemaElement
import org.intellij.prisma.ide.schema.builder.PrismaSchemaEvaluationContext
import org.intellij.prisma.ide.schema.builder.PrismaSchemaVariantsCapability
import org.intellij.prisma.ide.schema.builder.isAcceptedByPattern
import org.intellij.prisma.ide.schema.builder.isAvailableForDatasources
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.psi.PrismaArgument
import org.intellij.prisma.lang.psi.PrismaArrayExpression
import org.intellij.prisma.lang.psi.PrismaElementTypes.COMMA
import org.intellij.prisma.lang.psi.PrismaElementTypes.DATASOURCE_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.EQ
import org.intellij.prisma.lang.psi.PrismaElementTypes.GENERATOR_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.IDENTIFIER
import org.intellij.prisma.lang.psi.PrismaElementTypes.LBRACKET
import org.intellij.prisma.lang.psi.PrismaElementTypes.NAMED_ARGUMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.RBRACKET
import org.intellij.prisma.lang.psi.PrismaElementTypes.STRING_LITERAL
import org.intellij.prisma.lang.psi.PrismaElementTypes.VALUE_ARGUMENT
import org.intellij.prisma.lang.psi.PrismaExpression
import org.intellij.prisma.lang.psi.PrismaFile
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.types.isListType
import org.intellij.prisma.lang.types.isNamedType


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
    result: CompletionResultSet,
  ) {
    val file = parameters.originalFile as? PrismaFile ?: return
    val datasourceTypes = file.metadata.datasourceTypes
    val parent =
      parameters.position.parentOfTypes(PrismaArgument::class, PrismaMemberDeclaration::class) ?: return
    val schema = PrismaSchemaProvider
      .getEvaluatedSchema(PrismaSchemaEvaluationContext.forElement(parameters.originalPosition ?: parameters.position))
    val schemaElement = schema.match(parent) ?: return

    val isInString = parameters.position.elementType == STRING_LITERAL
    val listExpression = findListExpression(parameters)
    val isListExpected = isListType(schemaElement.type)
    if (isListExpected && listExpression == null) {
      return
    }

    val usedValues = mutableSetOf<String>()
    listExpression?.expressionList?.mapNotNullTo(usedValues) { PrismaSchemaPath.getSchemaLabel(it) }

    val queryPosition = parameters.originalPosition ?: parameters.position
    schema.substituteRefs(schemaElement.asSafely<PrismaSchemaVariantsCapability>()?.variants ?: emptyList())
      .asSequence()
      .filter { it.label !in usedValues }
      .filterNot { isInString && it is PrismaSchemaDeclaration && !isNamedType(it.type, PrimitiveTypes.STRING) }
      .filter { it.isAvailableForDatasources(datasourceTypes) }
      .filter { it.isAcceptedByPattern(queryPosition, context) }
      .mapNotNull {
        val defaultLabel = it.label ?: return@mapNotNull null
        val contextLabel = computeContextAwareLabel(it, parameters) ?: return@mapNotNull null
        createLookupElement(contextLabel, it, PrismaSchemaFakeElement.createForCompletion(parameters, it))
          .withPresentableText(defaultLabel)
          .withLookupString(defaultLabel)
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

  private fun computeContextAwareLabel(schemaElement: PrismaSchemaElement, parameters: CompletionParameters): String? {
    val wrapInQuotes = schemaElement.type == PrimitiveTypes.STRING && parameters.position.elementType != STRING_LITERAL
    return if (wrapInQuotes) schemaElement.label?.let { StringUtil.wrapWithDoubleQuote(it) } else schemaElement.label
  }
}
