package com.intellij.dts.core.lang.parser

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.dts.core.lang.DtsLanguage
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiErrorElement

class DtsHighlightingErrorFilter : HighlightErrorFilter() {
  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    return if (element.language == DtsLanguage) {
      Registry.`is`("dts.parser_errors")
    }
    else {
      true
    }
  }
}