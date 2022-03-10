// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.index

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.id.IdIndexEntry
import com.intellij.psi.impl.cache.impl.id.LexingIdIndexer
import com.intellij.util.indexing.FileContent
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueIdIndexer : LexingIdIndexer {
  override fun map(inputData: FileContent): Map<IdIndexEntry, Int> {
    return BaseFilterLexerUtil.calcIdEntries(inputData) { consumer ->
      VueFilterLexer(consumer, SyntaxHighlighterFactory.getSyntaxHighlighter(
        VueLanguage.INSTANCE, inputData.project, inputData.file).highlightingLexer)
    }
  }

  override fun getVersion(): Int {
    return 1
  }
}
