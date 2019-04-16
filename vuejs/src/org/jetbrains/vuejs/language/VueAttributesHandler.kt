// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueAttributesHandler : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer?) {
    val handled = lexer as VueHandledLexer
    if (!handled.inTagState()) {
      val text = lexer.tokenText
      handled.setSeenVueAttribute(text.startsWith(":") || text.startsWith("@") || text.startsWith("v-"))
    }
  }
}
