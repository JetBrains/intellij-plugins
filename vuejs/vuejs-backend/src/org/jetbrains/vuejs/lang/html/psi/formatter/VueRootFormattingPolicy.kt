// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.FormattingDocumentModel
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType

class VueRootFormattingPolicy(settings: CodeStyleSettings, documentModel: FormattingDocumentModel) :
  HtmlPolicy(settings, documentModel) {

  private val myVueCodeStyleSettings: VueCodeStyleSettings = settings.getCustomSettings(VueCodeStyleSettings::class.java)

  val uniformIndents: Boolean get() = myVueCodeStyleSettings.UNIFORM_INDENT

  val htmlPolicy: HtmlPolicy get() = HtmlPolicy(myRootSettings, documentModel)

  override fun indentChildrenOf(parentTag: XmlTag?): Boolean {
    if (parentTag == null) {
      return true
    }
    val firstChild = findFirstNonEmptyChild(parentTag) ?: return false
    if (firstChild.node.elementType !== XmlTokenType.XML_START_TAG_START) {
      return false
    }
    return checkName(parentTag, myVueCodeStyleSettings.INDENT_CHILDREN_OF_TOP_LEVEL)
  }
}