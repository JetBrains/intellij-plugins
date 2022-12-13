// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.LangMode

class VueFile(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, VueFileElementType.INSTANCE) {
  override fun getStub(): VueFileStub? = super.getStub() as VueFileStub?

  val langMode: LangMode
    get() {
      val stub = stub
      if (stub != null) {
        return stub.langMode
      }
      return calcLangMode()
    }

  private fun calcLangMode(): LangMode {
    var result = LangMode.NO_TS
    for (element in document?.children ?: arrayOf()) {
      if (element is XmlTag && HtmlUtil.isScriptTag(element)) {
        val lang = element.getAttributeValue(LANG_ATTRIBUTE_NAME)?.trim()
        if (LangMode.knownAttrValues.contains(lang)) {
          result = LangMode.fromAttrValue(lang)
          break
        }
      }
    }
    return result
  }
}
