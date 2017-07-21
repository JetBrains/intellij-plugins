package org.jetbrains.vuejs.language

import com.intellij.lang.Language
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IFileElementType

object VueElementTypes {
  val FILE: IFileElementType = JSFileElementType.create(VueJSLanguage.INSTANCE)
  val EMBEDDED_JS = object : JSEmbeddedContentElementType(VueJSLanguage.INSTANCE, "VueJS") {
    override fun createStripperLexer(baseLanguage: Language): Lexer? {
      return null
    }
  }
}