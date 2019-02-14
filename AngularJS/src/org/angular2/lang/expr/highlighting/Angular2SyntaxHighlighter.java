// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.highlighting;

import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.lexer.Angular2Lexer;
import org.angular2.lang.expr.lexer.Angular2TokenTypes;
import org.jetbrains.annotations.NotNull;

public class Angular2SyntaxHighlighter extends JSHighlighter {

  public Angular2SyntaxHighlighter() {
    super(Angular2Language.INSTANCE.getOptionHolder(), false);
  }

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new Angular2Lexer();
  }

  @NotNull
  @Override
  public TokenSet getKeywords() {
    return Angular2TokenTypes.KEYWORDS;
  }
}