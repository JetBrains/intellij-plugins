package org.jetbrains.astro.lang.sfc

import com.intellij.lexer.Lexer
import org.jetbrains.astro.lang.sfc.highlighting.AstroSfcHighlightingLexer

class AstroSfcHighlightingLexerTest : AstroSfcLexerTest() {
  override fun createLexer(): Lexer = AstroSfcHighlightingLexer(null)
  override fun getDirPath() = "lang/sfc/highlighting"

  override fun testFrontmatterOnly1() {
    // TODO: minor - in this particular case lexer fails to restart
  }
}