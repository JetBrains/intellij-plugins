// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.index;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer;
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingTest;

public class Angular2HtmlIndexerTest extends Angular2HtmlHighlightingTest {

  @Override
  protected Lexer createLexer() {
    return new Angular2HtmlFilterLexer(new OccurrenceConsumer(null, false),
                                       new Angular2HtmlHighlightingLexer(true, null,
                                                                         FileTypeRegistry.getInstance().findFileTypeByName("CSS")));
  }
}
