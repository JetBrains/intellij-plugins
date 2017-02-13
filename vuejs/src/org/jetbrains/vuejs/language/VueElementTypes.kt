package org.jetbrains.vuejs.language

import com.intellij.lang.Language
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lexer.Lexer

object VueElementTypes {
  val EMBEDDED_JS = object : JSEmbeddedContentElementType(VueJSLanguage.INSTANCE, "VueJS") {
    override fun createStripperLexer(baseLanguage: Language): Lexer? {
      return null
    }
  }
}