// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.index

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.todo.TodoIndexEntry
import com.intellij.psi.impl.cache.impl.todo.VersionedTodoIndexer
import com.intellij.util.indexing.FileContent
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueTodoIndexer : VersionedTodoIndexer() {
  override fun map(inputData: FileContent): Map<TodoIndexEntry, Int> {
    return BaseFilterLexerUtil.scanContent(inputData, true) { consumer ->
      VueFilterLexer(consumer, SyntaxHighlighterFactory.getSyntaxHighlighter(
        VueLanguage.INSTANCE, inputData.project, inputData.file).highlightingLexer)
    }.todoMap
  }

  override fun getVersion(): Int {
    return 1
  }
}
