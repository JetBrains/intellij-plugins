package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import org.jetbrains.vuejs.VueLanguage

class VueEmbeddedCssProvider : EmbeddedCssProvider() {
  override fun enableEmbeddedCssFor(language: Language): Boolean = language is VueLanguage
}