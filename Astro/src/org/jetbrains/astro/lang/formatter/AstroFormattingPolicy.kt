// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.lang.javascript.formatter.blocks.JSBlock
import com.intellij.lang.javascript.formatter.blocks.alignment.ASTNodeBasedAlignmentFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.InjectedLanguageBlockBuilder
import com.intellij.psi.formatter.xml.HtmlPolicy

class AstroFormattingPolicy(settings: CodeStyleSettings, documentModel: FormattingDocumentModel) :
  HtmlPolicy(settings, documentModel) {

  private val jsFormattingModelBuilder = object : JavascriptFormattingModelBuilder() {
    override fun createBlockFactory(settings: CodeStyleSettings, dialect: Language, mode: FormattingMode): JSBlockContext {
      return AstroJSBlockContext(settings, dialect, null, mode)
    }
  }

  fun buildInjectedEmbeddedExpressionBlock(result: MutableList<Block>,
                                           child: ASTNode,
                                           wrap: Wrap?,
                                           alignment: Alignment?,
                                           indent: Indent?,
                                           injectedBlockBuilder: InjectedLanguageBlockBuilder) {
    assert(child.elementType === JSStubElementTypes.EMBEDDED_EXPRESSION)

    val childPsi: PsiElement = child.psi
    val childLanguage = childPsi.language

    val childModel = jsFormattingModelBuilder.createModel(FormattingContext.create(childPsi, injectedBlockBuilder.settings))
    val original = childModel.rootBlock

    if (original.isLeaf && !child.text.trim { it <= ' ' }.isEmpty() || !original.subBlocks.isEmpty()) {
      result.add(injectedBlockBuilder.createInjectedBlock(child, original, indent, 0, null, childLanguage))
    }
  }

  private inner class AstroJSBlockContext(topSettings: CodeStyleSettings,
                                          dialect: Language,
                                          explicitSettings: JSCodeStyleSettings?,
                                          formattingMode: FormattingMode)
    : JSBlockContext(topSettings, dialect, explicitSettings, formattingMode) {
    override fun createBlock(child: ASTNode,
                             wrap: Wrap?,
                             childAlignment: Alignment?,
                             childIndent: Indent?,
                             alignmentFactory: ASTNodeBasedAlignmentFactory?,
                             parentBlock: JSBlock?): Block {
      if (child.elementType === JSElementTypes.JSX_XML_LITERAL_EXPRESSION) {
        return AstroTagBlock(child, wrap, childAlignment, this@AstroFormattingPolicy, childIndent, false)
      }
      return super.createBlock(child, wrap, childAlignment, childIndent, alignmentFactory, parentBlock)
    }
  }


}