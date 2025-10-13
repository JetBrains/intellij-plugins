// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.psi.impl.source.tree.LazyParseableElement
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.psi.tree.ILightLazyParseableElementType
import com.intellij.util.CharTable
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.annotations.NonNls

abstract class VueEmbeddedContentTokenType protected constructor(@NonNls debugName: String, language: Language?, register: Boolean)
  : IElementType(debugName, language, register), EmbeddingElementType, ICustomParsingType,
    ILazyParseableElementTypeBase, ILightLazyParseableElementType {

  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return LazyParseableElement(this, text)
  }

  override fun parseContents(chameleon: ASTNode): ASTNode {
    val builder = doParseContents(chameleon)
    return builder.treeBuilt.firstChildNode
  }

  override fun parseContents(chameleon: LighterLazyParseableNode): FlyweightCapableTreeStructure<LighterASTNode> {
    val file = chameleon.containingFile ?: error("Let's add LighterLazyParseableNode#getProject() method")
    val project = file.project
    val lexer = createLexer()
    val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chameleon.text)
    parse(builder)
    return builder.lightTree
  }

  private fun doParseContents(chameleon: ASTNode): PsiBuilder {
    val psi = chameleon.psi
    val project = psi.project

    val chars = chameleon.chars

    val lexer = createLexer()

    val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, language, chars)

    parse(builder)
    return builder
  }

  protected abstract fun createLexer(): Lexer

  protected abstract fun parse(builder: PsiBuilder)
}
