package org.intellij.prisma.ide.highlighting

import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.intellij.prisma.lang.psi.PrismaBlockAttribute
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaElementTypes.AT
import org.intellij.prisma.lang.psi.PrismaElementTypes.ATAT
import org.intellij.prisma.lang.psi.PrismaElementTypes.IDENTIFIER
import org.intellij.prisma.lang.psi.PrismaEnumValueDeclaration
import org.intellij.prisma.lang.psi.PrismaFieldAttribute
import org.intellij.prisma.lang.psi.PrismaFieldDeclaration
import org.intellij.prisma.lang.psi.PrismaFunctionCall
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaNamedArgument
import org.intellij.prisma.lang.psi.PrismaPathExpression
import org.intellij.prisma.lang.psi.PrismaTypeReference
import org.intellij.prisma.lang.psi.findTopmostPathParent
import org.intellij.prisma.lang.psi.isFieldExpression

class PrismaHighlightingAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element.elementType) {
      IDENTIFIER -> highlightIdentifier(holder, element)
      AT, ATAT -> newAnnotation(holder, element, PrismaColors.ATTRIBUTE)
    }
  }

  private fun highlightIdentifier(holder: AnnotationHolder, element: PsiElement) {
    val parent = element.parent ?: return

    when (parent) {
      is PrismaDeclaration -> newAnnotation(holder, element, PrismaColors.TYPE_NAME)
      is PrismaFieldDeclaration -> newAnnotation(holder, element, PrismaColors.FIELD_NAME)
      is PrismaTypeReference -> newAnnotation(holder, element, PrismaColors.TYPE_REFERENCE)
      is PrismaNamedArgument -> newAnnotation(holder, element, PrismaColors.PARAMETER)
      is PrismaKeyValue -> newAnnotation(holder, element, PrismaColors.FIELD_NAME)
      is PrismaEnumValueDeclaration -> newAnnotation(holder, element, PrismaColors.FIELD_NAME)
      is PrismaPathExpression -> highlightPathExpression(parent, holder, element)
    }
  }

  private fun highlightPathExpression(
    expr: PrismaPathExpression,
    holder: AnnotationHolder,
    element: PsiElement
  ) {
    when (val topmostParent = expr.findTopmostPathParent()) {
      is PrismaFunctionCall -> if (isFieldExpression(topmostParent)) {
        newAnnotation(holder, element, PrismaColors.FIELD_REFERENCE)
      }
      else {
        newAnnotation(holder, element, PrismaColors.FUNCTION)
      }

      is PrismaBlockAttribute, is PrismaFieldAttribute ->
        newAnnotation(holder, element, PrismaColors.ATTRIBUTE)

      else -> newAnnotation(holder, element, PrismaColors.FIELD_REFERENCE)
    }
  }

  private fun newAnnotation(
    holder: AnnotationHolder, element: PsiElement, textAttributesKey: TextAttributesKey
  ) {
    newAnnotationBuilder(holder, textAttributesKey.externalName)
      .textAttributes(textAttributesKey)
      .range(element)
      .create()
  }

  private fun newAnnotationBuilder(holder: AnnotationHolder, @InspectionMessage tag: String) =
    if (ApplicationManager.getApplication().isUnitTestMode)
      holder.newAnnotation(HighlightSeverity.INFORMATION, tag)
    else
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
}