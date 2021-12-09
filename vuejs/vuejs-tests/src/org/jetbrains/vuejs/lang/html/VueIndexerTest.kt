// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import org.jetbrains.vuejs.lang.html.highlighting.VueHighlightingLexer
import org.jetbrains.vuejs.lang.html.index.VueFilterLexer

class VueIndexerTest : VueHighlightingLexerTest() {
  override fun createLexer(): Lexer {
    return VueFilterLexer(OccurrenceConsumer(null, false),
                          VueHighlightingLexer(JSLanguageLevel.ES6, null, interpolationConfig))
  }
}
