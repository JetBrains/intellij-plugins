// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.highlighting

import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.angular2.lang.expr.lexer.Angular2TokenTypes

class Angular2SyntaxHighlighter : TypeScriptHighlighter(Angular2Language.optionHolder) {

  override fun getHighlightingLexer(): Lexer =
    Angular2Lexer(Angular2Lexer.RegularBinding)

  override fun getKeywords(): TokenSet =
    Angular2TokenTypes.KEYWORDS

  override fun getTokenHighlights(tokenType: IElementType): Array<out TextAttributesKey> =
    angularKeys[tokenType]?.let { SyntaxHighlighterBase.pack(it) }
    ?: super.getTokenHighlights(tokenType)
}

private val angularKeys: Map<IElementType, TextAttributesKey> = mapOf(
  Angular2TokenTypes.BLOCK_PARAMETER_NAME to TypeScriptHighlighter.TS_KEYWORD,
  Angular2TokenTypes.BLOCK_PARAMETER_PREFIX to TypeScriptHighlighter.TS_KEYWORD,
)
