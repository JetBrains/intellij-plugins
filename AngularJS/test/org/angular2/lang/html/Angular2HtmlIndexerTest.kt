// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer
import org.angular2.lang.html.index.Angular2HtmlFilterLexer
import org.angularjs.AngularTestUtil

class Angular2HtmlIndexerTest : Angular2HtmlHighlightingLexerTest() {
  override fun createLexer(): Lexer {
    return Angular2HtmlFilterLexer(OccurrenceConsumer(null, false),
                                   Angular2HtmlHighlightingLexer(true, null,
                                                                 FileTypeRegistry.getInstance().findFileTypeByName("CSS")))
  }

  override fun getDirPath(): String {
    return AngularTestUtil.getLexerTestDirPath() + "html/index"
  }
}
