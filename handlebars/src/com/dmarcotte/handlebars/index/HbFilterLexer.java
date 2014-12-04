package com.dmarcotte.handlebars.index;

import com.dmarcotte.handlebars.parsing.HbRawLexer;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;

public class HbFilterLexer extends BaseFilterLexer {
  public HbFilterLexer(OccurrenceConsumer table) {
    super(new HbRawLexer(), table);
  }

  public void advance() {
    final IElementType tokenType = myDelegate.getTokenType();

    if (tokenType == HbTokenTypes.COMMENT_CONTENT) {
      scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false);
      advanceTodoItemCountsInToken();
    }

    myDelegate.advance();
  }
}

