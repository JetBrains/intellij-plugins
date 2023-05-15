// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.emmet.generators.XmlZenCodingGeneratorImpl
import com.intellij.lang.Language
import com.intellij.lang.xml.XMLLanguage
import org.jetbrains.astro.lang.AstroFileImpl

class AstroHtmlZenCodingGenerator : XmlZenCodingGeneratorImpl() {
  override fun isMyContext(callback: CustomTemplateCallback, wrapping: Boolean): Boolean {
    return callback.context.containingFile is AstroFileImpl
  }

  override fun isMyLanguage(language: Language): Boolean {
    return language.isKindOf(XMLLanguage.INSTANCE)
  }

  override fun isHtml(callback: CustomTemplateCallback): Boolean {
    return true
  }
}