package org.intellij.prisma.ide.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import org.intellij.prisma.ide.completion.schema.*
import org.intellij.prisma.lang.psi.PrismaElementTypes

class PrismaCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, PrismaKeywordProvider)
    extend(CompletionType.BASIC, PrismaPrimitiveTypeProvider)
    extend(CompletionType.BASIC, PrismaDatasourceFieldsProvider)
    extend(CompletionType.BASIC, PrismaGeneratorFieldsProvider)
    extend(CompletionType.BASIC, PrismaBlockAttributesProvider)
    extend(CompletionType.BASIC, PrismaFieldAttributesProvider)
    extend(CompletionType.BASIC, PrismaParametersProvider)
    extend(CompletionType.BASIC, PrismaValuesProvider)
    extend(CompletionType.BASIC, PrismaOperatorClassProvider)
    extend(CompletionType.BASIC, PrismaNativeTypeProvider)
  }

  private fun extend(type: CompletionType, provider: PrismaCompletionProvider) {
    extend(type, provider.pattern, provider)
  }

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    super.fillCompletionVariants(parameters, decorateResultSet(parameters, result))
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    val element = context.file.findElementAt(context.startOffset) ?: return
    if (element.elementType == PrismaElementTypes.IDENTIFIER) {
      context.dummyIdentifier = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
    }
  }

  private fun decorateResultSet(
    parameters: CompletionParameters,
    result: CompletionResultSet
  ): CompletionResultSet {
    val prefix = computePrefix(parameters, result)
    return result.withPrefixMatcher(result.prefixMatcher.cloneWithPrefix(prefix))
  }

  private fun computePrefix(
    parameters: CompletionParameters,
    result: CompletionResultSet
  ): String {
    val prefix = result.prefixMatcher.prefix
    val prev = parameters.position.prevLeaf()
    val attrPrefix = when (prev?.elementType) {
      PrismaElementTypes.AT -> "@"
      PrismaElementTypes.ATAT -> "@@"
      else -> ""
    }
    return attrPrefix + prefix
  }
}