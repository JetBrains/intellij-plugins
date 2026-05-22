// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.html.embedding.HtmlEmbedmentInfo
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType

class VueScriptEmbedmentInfo(private val elementType: IElementType) : HtmlEmbedmentInfo {
  override fun getElementType(): IElementType = elementType
  override fun createHighlightingLexer(): Lexer = error("VueEmbeddedContentSupport did something unexpected")
}