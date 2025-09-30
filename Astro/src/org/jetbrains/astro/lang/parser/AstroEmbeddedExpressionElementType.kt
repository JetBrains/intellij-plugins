// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.*
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LazyParseableElement
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.psi.tree.ILightLazyParseableElementType
import com.intellij.util.CharTable
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroRawTextLexer.AstroEmbeddedExpressionBraceFixingLexer

class AstroEmbeddedExpressionElementType : JSElementType<JSEmbeddedContent>("ASTRO_EMBEDDED_EXPRESSION"),
                                           EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase, ILightLazyParseableElementType,
                                           JSEmbeddedBlockElementType, TemplateDataElementType.TemplateAwareElementType {

  override fun construct(node: ASTNode): PsiElement =
    object: JSEmbeddedContentImpl(node) {
      override fun getLanguage(): Language = AstroLanguage.INSTANCE
      override fun toString(): String = "JSEmbeddedContent:ASTRO_EMBEDDED_EXPRESSION"
    }

  override fun parse(text: CharSequence, table: CharTable): ASTNode = createTreeElement(text)

  override fun createTreeElement(text: CharSequence): TreeElement = LazyParseableElement(this, text)

  override fun getLanguage(): Language = AstroLanguage.INSTANCE

  override fun parseContents(chameleon: ASTNode): ASTNode? {
    val psi = chameleon.getPsi()
    val project = psi.getProject()

    val builder = PsiBuilderFactory.getInstance().createBuilder(
      project, chameleon, AstroEmbeddedExpressionBraceFixingLexer(JSFlexAdapter(DialectOptionHolder.TS)),
      AstroLanguage.INSTANCE, chameleon.chars)
    doParseTS(builder)
    return builder.getTreeBuilt().getFirstChildNode()
  }


  override fun parseContents(chameleon: LighterLazyParseableNode): FlyweightCapableTreeStructure<LighterASTNode?> {
    val file: PsiFile = checkNotNull(chameleon.getContainingFile()) { "Let's add LighterLazyParseableNode#getProject() method" }
    val project = file.getProject()
    val builder = PsiBuilderFactory.getInstance().createBuilder(
      project, chameleon, AstroEmbeddedExpressionBraceFixingLexer(JSFlexAdapter(DialectOptionHolder.TS)),
      AstroLanguage.INSTANCE, chameleon.getText())
    doParseTS(builder)
    return builder.getLightTree()
  }

  private fun doParseTS(builder: PsiBuilder) {
    val parser = AstroParsing(builder)
    val rootMarker = builder.mark()
    parser.parseEmbeddedExpression()
    rootMarker.done(this)
  }
}