package org.jetbrains.astro.lang

import com.intellij.lexer.Lexer
import org.jetbrains.astro.lang.lexer.AstroLexer

class AstroHighlightingLexerTest : AstroLexerTest() {
  override fun createLexer(): Lexer = AstroLexer(null, true, false)
  override fun getDirPath() = "lang/highlighting"

}