package org.intellij.prisma.ide.schema

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType.ERROR_ELEMENT
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.util.*
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.presentation.PrismaPsiRenderer
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.PrismaElementTypes.*

sealed class PrismaSchemaPath(
  val label: String,
  val element: PsiElement,
) {
  companion object {
    fun forElement(element: PsiElement?): PrismaSchemaPath? {
      if (element == null || element is PrismaSchemaFakeElement) {
        return null
      }

      return CachedValuesManager.getCachedValue(element) {
        CachedValueProvider.Result.create(buildPath(element), element)
      }
    }

    private fun buildPath(element: PsiElement): PrismaSchemaPath? {
      val pathElement = adjustPathElement(element) ?: return null
      return when (pathElement) {
        is PrismaArgument -> createParameterPath(pathElement)
        is PrismaLiteralExpression -> createVariantPath(pathElement)
        else -> createDeclarationPath(pathElement)
      }
    }

    private fun createParameterPath(element: PrismaArgument): PrismaSchemaParameterPath? {
      var parentElement = element.parentOfType<PrismaArgumentsOwner>() ?: return null
      var isFieldExpression = false
      if (parentElement is PrismaFunctionCall && parentElement.parent is PrismaArrayExpression) {
        parentElement = parentElement.parentOfType() ?: return null
        isFieldExpression = true
      }
      val parentPath = forElement(parentElement) as? PrismaSchemaDeclarationPath ?: return null
      return when (element) {
        is PrismaNamedArgument -> element.referenceName?.let {
          PrismaSchemaParameterPath(it, element, parentPath)
        }

        is PrismaValueArgument -> if (element.isDefault && !isFieldExpression) {
          PrismaSchemaDefaultParameterPath(element, parentPath)
        }
        else {
          null
        }

        else -> null
      }
    }

    private fun createVariantPath(element: PrismaLiteralExpression): PrismaSchemaVariantPath? {
      val parent =
        element.parentOfTypes(PrismaArgument::class, PrismaMemberDeclaration::class) ?: return null
      val parentPath = forElement(parent) ?: return null
      val label = getSchemaLabel(element) ?: return null
      return PrismaSchemaVariantPath(label, element, parentPath)
    }

    private fun createDeclarationPath(element: PsiElement): PrismaSchemaDeclarationPath? {
      val kind = getSchemaKind(element) ?: return null
      val label = getSchemaLabel(element) ?: return null
      return PrismaSchemaDeclarationPath(label, element, kind)
    }

    fun adjustPathElement(element: PsiElement): PsiElement? {
      return when (element.elementType) {
        IDENTIFIER -> findIdentifierParent(element)

        AT, ATAT, UNSUPPORTED -> element.parent

        in PRISMA_LITERALS -> element.parent

        WHITE_SPACE, ERROR_ELEMENT ->
          PsiTreeUtil.skipParentsOfType(element, PsiWhiteSpace::class.java, PsiErrorElement::class.java)

        else -> element
      }
    }

    private fun findIdentifierParent(element: PsiElement?): PsiElement? {
      val parent =
        PsiTreeUtil.skipParentsOfType(element, PsiWhiteSpace::class.java, PsiErrorElement::class.java)

      if (parent is PrismaPathExpression) {
        val pathParent = parent.findTopmostPathParent()
        if (pathParent != null) {
          return pathParent
        }
      }

      return parent
    }

    private fun getSchemaKind(element: PsiElement): PrismaSchemaKind? {
      return when (element.elementType) {
        in PRISMA_KEYWORDS -> PrismaSchemaKind.KEYWORD

        UNSUPPORTED_TYPE -> PrismaSchemaKind.PRIMITIVE_TYPE

        TYPE_REFERENCE ->
          if (PrismaConstants.PrimitiveTypes.ALL.contains((element as? PrismaTypeReference)?.referenceName)) {
            PrismaSchemaKind.PRIMITIVE_TYPE
          }
          else {
            null
          }

        KEY_VALUE -> {
          val memberDeclaration = element as PrismaMemberDeclaration
          val declaration = memberDeclaration.containingDeclaration
          when (declaration?.elementType) {
            DATASOURCE_DECLARATION -> PrismaSchemaKind.DATASOURCE_FIELD
            GENERATOR_DECLARATION -> PrismaSchemaKind.GENERATOR_FIELD
            else -> null
          }
        }

        BLOCK_ATTRIBUTE -> PrismaSchemaKind.BLOCK_ATTRIBUTE
        FIELD_ATTRIBUTE -> PrismaSchemaKind.FIELD_ATTRIBUTE

        FUNCTION_CALL -> PrismaSchemaKind.FUNCTION

        else -> null
      }
    }

    fun getSchemaLabel(element: PsiElement): String? {
      val psiRenderer = PrismaPsiRenderer()
      return when {
        element.isKeyword -> element.text
        element is PrismaTypeReference -> psiRenderer.build(element)
        element is PrismaBlockAttribute -> "@@${psiRenderer.build(element.pathExpression)}"
        element is PrismaFieldAttribute -> "@${psiRenderer.build(element.pathExpression)}"
        element is PrismaUnsupportedType -> PrismaConstants.PrimitiveTypes.UNSUPPORTED
        element is PrismaKeyValue -> psiRenderer.build(element.identifier)
        element is PrismaReferenceElement -> element.referenceName
        element is PrismaFunctionCall -> psiRenderer.build(element.pathExpression)
        element is PrismaLiteralExpression -> element.value?.toString()
        element is PsiNamedElement -> element.name
        else -> null
      }
    }
  }
}

class PrismaSchemaDeclarationPath(
  label: String,
  element: PsiElement,
  val kind: PrismaSchemaKind,
) : PrismaSchemaPath(label, element)

open class PrismaSchemaParameterPath(
  label: String,
  element: PsiElement,
  val parent: PrismaSchemaDeclarationPath,
) : PrismaSchemaPath(label, element)

class PrismaSchemaDefaultParameterPath(
  element: PsiElement,
  parent: PrismaSchemaDeclarationPath,
) : PrismaSchemaParameterPath("#DEFAULT", element, parent)

class PrismaSchemaVariantPath(
  label: String,
  element: PsiElement,
  val parent: PrismaSchemaPath,
) : PrismaSchemaPath(label, element)
