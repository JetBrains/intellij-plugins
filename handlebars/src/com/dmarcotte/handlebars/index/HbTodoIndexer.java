package com.dmarcotte.handlebars.index;

import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.impl.cache.impl.id.LexerBasedIdIndexer;
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer;

public class HbTodoIndexer extends LexerBasedTodoIndexer {
  public Lexer createLexer(final OccurrenceConsumer consumer) {
    return createIndexingLexer(consumer);
  }

  public static Lexer createIndexingLexer(OccurrenceConsumer consumer) {
    return new HbFilterLexer(consumer);
  }
}
