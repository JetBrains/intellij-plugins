package com.intellij.dts.pp.highlighting

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

abstract class PpHighlightAnnotator : Annotator, DumbAware {
  protected abstract val tokenTypes: PpTokenTypes

  protected abstract val inactiveAttribute: TextAttributesKey

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element.elementType == tokenTypes.inactive) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(inactiveAttribute).create()
    }
  }
}