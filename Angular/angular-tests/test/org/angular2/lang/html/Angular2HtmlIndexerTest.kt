// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import org.angular2.Angular2TestUtil
import org.angular2.lang.html.index.Angular2HtmlFilterLexer
import org.angular2.lang.html.lexer.Angular2HtmlLexer

open class Angular2HtmlIndexerTest : Angular2HtmlHighlightingLexerTest() {
  override fun createLexer(): Lexer {
    return Angular2HtmlFilterLexer(OccurrenceConsumer(null, false),
                                   Angular2HtmlLexer(true, templateSyntax, null))
  }

  override fun getDirPath(): String {
    return Angular2TestUtil.getLexerTestDirPath() + "html/index"
  }
}
