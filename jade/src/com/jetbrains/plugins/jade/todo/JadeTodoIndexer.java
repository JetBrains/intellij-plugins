package com.jetbrains.plugins.jade.todo;

import com.intellij.application.options.CodeStyle;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.lexer.JadeLexer;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

public final class JadeTodoIndexer extends LexerBasedTodoIndexer {

  @Override
  public @NotNull Lexer createLexer(@NotNull OccurrenceConsumer consumer) {
    Lexer delegate = new JadeLexer(CodeStyle.getDefaultSettings());

    return new BaseFilterLexer(delegate, consumer) {
      @Override
      public void advance() {
        final IElementType tokenType = myDelegate.getTokenType();

        if (JadeTokenTypes.COMMENTS.contains(tokenType)) {
          scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false);
          advanceTodoItemCountsInToken();
        }

        myDelegate.advance();
      }
    };
  }
}
