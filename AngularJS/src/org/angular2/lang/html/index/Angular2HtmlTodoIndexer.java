// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.index;

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.psi.impl.cache.impl.BaseFilterLexerUtil;
import com.intellij.psi.impl.cache.impl.todo.TodoIndexEntry;
import com.intellij.psi.impl.cache.impl.todo.VersionedTodoIndexer;
import com.intellij.util.indexing.FileContent;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Angular2HtmlTodoIndexer extends VersionedTodoIndexer {
  @Override
  public @NotNull Map<TodoIndexEntry, Integer> map(@NotNull FileContent inputData) {
    return BaseFilterLexerUtil.scanContent(inputData, consumer ->
      new Angular2HtmlFilterLexer(consumer, SyntaxHighlighterFactory.getSyntaxHighlighter(
        Angular2HtmlLanguage.INSTANCE, inputData.getProject(), inputData.getFile()).getHighlightingLexer())).todoMap;
  }

  @Override
  public int getVersion() {
    return 2;
  }
}
