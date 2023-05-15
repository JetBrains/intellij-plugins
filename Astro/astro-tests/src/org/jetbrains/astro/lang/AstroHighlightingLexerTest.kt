package org.jetbrains.astro.lang

import com.intellij.lexer.Lexer
import org.jetbrains.astro.lang.highlighting.AstroHighlightingLexer

class AstroHighlightingLexerTest : AstroLexerTest() {
  override fun createLexer(): Lexer = AstroHighlightingLexer(null)
  override fun getDirPath() = "lang/highlighting"

}