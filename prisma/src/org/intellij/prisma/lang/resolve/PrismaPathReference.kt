package org.intellij.prisma.lang.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.intellij.prisma.ide.schema.PrismaSchemaParameter
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.presentation.PrismaPsiRenderer
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.types.PrismaResolvableType
import org.intellij.prisma.lang.types.unwrapType

class PrismaPathReference(
  element: PsiElement,
  range: TextRange,
  soft: Boolean = false,
) : PrismaReference(element, range, soft) {

  override fun processCandidates(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement,
  ) {
    val context = findContext(element) ?: return
    val schemaElement = PrismaSchemaProvider.getSchema().match(context) ?: return
    if (schemaElement is PrismaSchemaParameter) {
      when (schemaElement.label) {
        PrismaConstants.ParameterNames.FIELDS -> {
          resolveField(processor, state, element)
        }

        PrismaConstants.ParameterNames.REFERENCES -> {
          processor.filter = { it is PrismaFieldDeclaration }
          resolveTypeField(processor, state, element)
        }

        PrismaConstants.ParameterNames.EXPRESSION -> {
          processor.filter = { it is PrismaEnumValueDeclaration }
          resolveTypeField(processor, state, element)
        }
      }
    }
  }

  private fun findContext(element: PsiElement): PsiElement? {
    val parent = PsiTreeUtil.skipParentsOfType(element, PrismaPathExpression::class.java)
    if (parent is PrismaArgument) {
      return parent
    }
    if (parent is PrismaArrayExpression || parent is PrismaFunctionCall && parent.parent is PrismaArrayExpression) {
      return parent.parentOfType<PrismaArgument>()
    }
    return null
  }

  private fun resolveTypeField(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement
  ) {
    val typeOwner = element.parentOfType<PrismaTypeOwner>() ?: return
    val type = typeOwner.type.unwrapType()
    if (type is PrismaResolvableType) {
      type.resolveDeclaration()?.processDeclarations(processor, state, null, element)
    }
  }

  private fun resolveField(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement
  ) {
    if (element is PrismaQualifiedReferenceElement && element.qualifier != null) {
      resolveQualifiedField(processor, state, element)
    }
    else {
      resolveLocalField(processor, state, element)
    }
  }

  private fun resolveLocalField(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement,
  ) {
    element.parentOfType<PrismaDeclaration>()?.processDeclarations(processor, state, null, element)
  }

  private fun resolveQualifiedField(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement,
  ) {
    val qualifier = (element as? PrismaQualifiedReferenceElement)?.qualifier as? PrismaReferenceElement ?: return
    val memberDeclaration = qualifier.resolve() as? PrismaMemberDeclaration ?: return
    if (memberDeclaration !is PrismaTypeOwner) {
      return
    }
    val type = memberDeclaration.type.unwrapType() as? PrismaResolvableType ?: return
    // only composite types
    val typeDeclaration = type.resolveDeclaration() as? PrismaTypeDeclaration ?: return
    typeDeclaration.processDeclarations(processor, state, null, element)
  }

  override fun collectIgnoredNames(): Set<String> {
    val arrayExpression = element.parentOfType<PrismaArrayExpression>() ?: return emptySet()
    val psiRenderer = PrismaPsiRenderer()
    return arrayExpression.expressionList.mapNotNullTo(mutableSetOf()) {
      var expr = it
      if (expr is PrismaFunctionCall) {
        expr = expr.pathExpression
      }
      if (expr is PrismaPathExpression) {
        return@mapNotNullTo psiRenderer.build(expr.leftmostQualifier)
      }

      null
    }
  }

  companion object {
    fun create(element: PrismaReferenceElement): PrismaPathReference? {
      val identifier = element.referenceNameElement ?: return null
      val range = TextRange.from(identifier.startOffsetInParent, identifier.textLength)
      return PrismaPathReference(element, range)
    }
  }
}