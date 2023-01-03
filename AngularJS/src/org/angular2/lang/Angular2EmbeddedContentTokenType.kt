// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LazyParseableElement
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementTypeBase
import com.intellij.util.CharTable
import org.jetbrains.annotations.NonNls

abstract class Angular2EmbeddedContentTokenType : IElementType, EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase {
  protected constructor(debugName: @NonNls String, language: Language?) : super(debugName, language)
  protected constructor(debugName: @NonNls String, language: Language?, register: Boolean) : super(debugName, language, register)

  override fun parse(text: CharSequence, table: CharTable): ASTNode {
    return LazyParseableElement(this, text)
  }

  override fun parseContents(chameleon: ASTNode): ASTNode {
    val builder = doParseContents(chameleon)
    return builder.treeBuilt.firstChildNode
  }

  protected fun doParseContents(chameleon: ASTNode): PsiBuilder {
    val project: Project
    val psi = chameleon.psi
    project = psi.project
    val chars = chameleon.chars
    val lexer = createLexer()
    val builder = PsiBuilderFactory.getInstance().createBuilder(
      project, chameleon, lexer, language, chars)
    parse(builder)
    return builder
  }

  protected abstract fun createLexer(): Lexer
  protected abstract fun parse(builder: PsiBuilder)
}