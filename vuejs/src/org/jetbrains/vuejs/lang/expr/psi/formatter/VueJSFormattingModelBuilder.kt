// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.formatter

import com.intellij.formatting.*
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.lang.javascript.formatter.blocks.CompositeJSBlock
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.WrappingUtil
import com.intellij.psi.xml.XmlText
import com.intellij.webcore.formatter.SpacingStrategy
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import org.jetbrains.vuejs.lang.html.psi.formatter.VueSyntheticBlock

class VueJSFormattingModelBuilder : JavascriptFormattingModelBuilder() {

  override fun createModel(element: PsiElement, settings: CodeStyleSettings, mode: FormattingMode): FormattingModel {
    val dialect = JSLanguageUtil.getLanguageDialect(element)
    val alignment = element.node.getUserData(BLOCK_ALIGNMENT)
    val jsBlockContext: JSBlockContext = createBlockFactory(settings, dialect, mode)
    var rootBlock: Block
    val host = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
    if (host is XmlText) {
      // interpolation injection
      val wrapType = WrappingUtil.getWrapType(settings.getCustomSettings(VueCodeStyleSettings::class.java).INTERPOLATION_WRAP)
      rootBlock = jsBlockContext.createBlock(element.node, Wrap.createWrap(wrapType, true),
                                             alignment, Indent.getNormalIndent(), null, null)
      // Wrap with a composite block to add indentation
      if (element is PsiFile) {
        rootBlock = CompositeJSBlock(listOf(rootBlock), SpacingStrategy { _, _ -> null }, null, element.node.elementType, jsBlockContext)
      }
    }
    else {
      rootBlock = jsBlockContext.createBlock(element.node, null, alignment, null, null, null)
    }
    return createJSFormattingModel(element.containingFile, settings, rootBlock)
  }
}