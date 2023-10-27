// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.formatter

import com.intellij.formatting.*
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.lang.javascript.formatter.blocks.CompositeJSBlock
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.formatter.WrappingUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings

class Angular2FormattingModelBuilder : JavascriptFormattingModelBuilder() {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val element = formattingContext.psiElement
    val settings = formattingContext.codeStyleSettings
    val dialect = JSLanguageUtil.getLanguageDialect(element)
    val alignment = element.node.getUserData(BLOCK_ALIGNMENT)
    val jsBlockContext: JSBlockContext = createBlockFactory(settings, dialect, formattingContext.formattingMode)
    var rootBlock: Block
    if (element.parent is XmlTag
        || element.parent is XmlDocument) {
      // interpolations
      val wrapType = WrappingUtil.getWrapType(settings.getCustomSettings(Angular2HtmlCodeStyleSettings::class.java).INTERPOLATION_WRAP)
      rootBlock = jsBlockContext.createBlock(element.node, Wrap.createWrap(wrapType, true),
                                             alignment, Indent.getNormalIndent(), null, null)
      // Wrap with a composite block to add indentation
      rootBlock = CompositeJSBlock(listOf(rootBlock), { _, _ -> null }, null,
                                   JSFileElementType.getByLanguage(dialect), jsBlockContext)
    }
    else {
      rootBlock = jsBlockContext.createBlock(element.node, null, alignment, null, null, null)
    }
    return createJSFormattingModel(element.containingFile, settings, rootBlock)
  }
}