// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lexer.BaseHtmlLexer
import com.intellij.psi.tree.IElementType

interface VueLexerHandle {

  fun registerHandler(elementType: IElementType, value: BaseHtmlLexer.TokenHandler)

  var seenTag: Boolean
  var seenStyleType: Boolean
  var seenScriptType: Boolean
  var seenScript: Boolean
  var scriptType: String?

  val seenStyle: Boolean
  val styleType: String?
  val inTagState: Boolean
  val interpolationConfig: Pair<String, String>?

}
