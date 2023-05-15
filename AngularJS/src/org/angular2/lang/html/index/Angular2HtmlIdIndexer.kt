// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.index

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.id.IdIndexEntry
import com.intellij.psi.impl.cache.impl.id.LexingIdIndexer
import com.intellij.util.indexing.FileContent
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2HtmlIdIndexer : LexingIdIndexer {
  override fun map(inputData: FileContent): Map<IdIndexEntry, Int> {
    return BaseFilterLexerUtil.calcIdEntries(inputData) { consumer ->
      Angular2HtmlFilterLexer(
        consumer,
        SyntaxHighlighterFactory.getSyntaxHighlighter(
          Angular2HtmlLanguage.INSTANCE, inputData.project, inputData.file).highlightingLexer
      )
    }
  }

  override fun getVersion(): Int {
    return 3
  }
}