// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.index

import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil
import com.intellij.psi.impl.cache.impl.todo.TodoIndexEntry
import com.intellij.psi.impl.cache.impl.todo.VersionedTodoIndexer
import com.intellij.util.indexing.FileContent
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.highlighting.VueSyntaxHighlighterFactory

class VueTodoIndexer : VersionedTodoIndexer() {
  override fun map(inputData: FileContent): Map<TodoIndexEntry, Int> {
    return BaseFilterLexerUtil.calcTodoEntries(inputData) { consumer ->
      // We need to choose lexer lang mode, since we don't have access to the indices here.
      // Let's use TypeScript lang, because it's a superset over JavaScript and should
      // work well at most times in both JS and TS cases.
      VueFilterLexer(consumer, VueSyntaxHighlighterFactory.getSyntaxHighlighter(
        inputData.project, inputData.file, LangMode.HAS_TS).highlightingLexer)
    }
  }

  override fun getVersion(): Int {
    return 1
  }
}
