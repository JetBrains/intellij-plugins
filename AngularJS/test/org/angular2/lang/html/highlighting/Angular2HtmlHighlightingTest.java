// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.lexer.Lexer;
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest;

public class Angular2HtmlHighlightingTest extends Angular2HtmlLexerTest {

  @Override
  protected Lexer createLexer() {
    return new Angular2HtmlHighlightingLexer(true, null, null);
  }
}
