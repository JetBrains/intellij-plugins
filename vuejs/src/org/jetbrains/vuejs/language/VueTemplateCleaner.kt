// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer

class VueTemplateCleaner : BaseHtmlLexer.TokenHandler {
  override fun handleElement(lexer: Lexer?) {
    (lexer as VueHandledLexer).setSeenTemplate(false)
  }
}
