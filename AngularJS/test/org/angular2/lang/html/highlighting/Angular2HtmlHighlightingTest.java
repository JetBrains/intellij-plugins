// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.lexer.Lexer;
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest;

public class Angular2HtmlHighlightingTest extends Angular2HtmlLexerTest {

  @Override
  protected Lexer createLexer() {
    return new Angular2HtmlHighlightingLexer(true, null, null);
  }

  public void testVarWith$() {
    doTest("{{ publicUsedField }}\n" +
           "<div [id]=\"privateUsedField\"></div>\n" +
           "{{ publicUsedConstructorField }}\n" +
           "<div [id]=\"privateUsedConstructorField\"></div>\n" +
           "<div (click)=\"publicUsedMethod()\"></div>\n" +
           "<div (click)=\"privateUsedMethod()\"></div>\n" +
           "\n" +
           "<div *ngIf=\"myObservable$ | async\"></div>\n" +
           "\n" +
           "{{ testFn() }}");
  }

  public void testNestedInterpolations() {
    doTest("<div *ngFor='let card of cards'>\n" +
           "  {{card.name}} - {{card.damage}}\n" +
           "  <button (click)='damage(card)'> Damage </button>\n" +
           "</div>");
  }
}
