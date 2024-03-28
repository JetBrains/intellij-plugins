package com.intellij.dts.highlighting

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import org.jetbrains.annotations.PropertyKey

class DtsSyntaxAnnotator : Annotator {
  private fun AnnotationHolder.newAnnotation(bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): AnnotationBuilder {
    return newAnnotation(HighlightSeverity.ERROR, DtsBundle.message(bundleKey))
  }

  private fun annotateChar(element: PsiElement, holder: AnnotationHolder) {
    if (!element.text.endsWith('\'')) {
      holder.newAnnotation("syntax.unterminated_char")
        .range(TextRange.from(element.endOffset, 0))
        .create()
    }
    else if (element.text.trim('\'').isEmpty()) {
      holder.newAnnotation("syntax.empty_char")
        .range(element)
        .create()
    }
  }

  private fun annotateString(element: PsiElement, holder: AnnotationHolder) {
    if (!element.text.endsWith('"')) {
      holder.newAnnotation("syntax.unterminated_string")
        .range(TextRange.from(element.endOffset, 0))
        .afterEndOfLine()
        .create()
    }
  }

  private fun annotatePHandle(element: PsiElement, holder: AnnotationHolder) {
    val whitespace = DtsUtil.children(element, unfiltered = true).filter { child -> child.elementType == TokenType.WHITE_SPACE }

    for (child in whitespace) {
      holder.newAnnotation("syntax.invalid_whitespace")
        .range(child)
        .create()
    }
  }

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element.elementType) {
      DtsTypes.CHAR_LITERAL -> annotateChar(element, holder)
      DtsTypes.STRING_LITERAL, DtsTypes.INCLUDE_PATH -> annotateString(element, holder)
      DtsTypes.P_HANDLE -> annotatePHandle(element, holder)
    }
  }
}
