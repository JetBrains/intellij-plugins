// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.*;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSParser
  extends JavaScriptParser<AngularJSExpressionParser, StatementParser<?>, FunctionParser<?>, JSPsiTypeParser<?>> {

  public AngularJSParser(PsiBuilder builder) {
    super(JavascriptLanguage.INSTANCE, builder);
    myExpressionParser = new AngularJSExpressionParser(this);
    myStatementParser = new AngularJSStatementParser(this);
  }

  @Override
  public boolean isIdentifierName(IElementType firstToken) {
    return super.isIdentifierName(firstToken) || firstToken == AngularJSTokenTypes.THEN;
  }

  public void parseAngular(IElementType root) {
    final PsiBuilder.Marker rootMarker = builder.mark();
    while (!builder.eof()) {
      getStatementParser().parseStatement();
    }
    rootMarker.done(root);
  }
}
