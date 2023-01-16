// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.typescript

import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase

class AstroFrontmatterHighlightingLexer : MergingLexerAdapterBase(
  JSHighlighter(JavaScriptSupportLoader.TYPESCRIPT.optionHolder).highlightingLexer) {
  override fun getMergeFunction(): MergeFunction {
    return MergeFunction { type, _ ->
      AstroFrontmatterHighlighterToken[type]
    }
  }
}