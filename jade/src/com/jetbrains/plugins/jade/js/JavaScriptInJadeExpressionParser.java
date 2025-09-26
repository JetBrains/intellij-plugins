// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptParserBundle;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;

class JavaScriptInJadeExpressionParser extends ES6ExpressionParser<JavaScriptInJadeParser> {

  protected JavaScriptInJadeExpressionParser(JavaScriptInJadeParser parser) {
    super(parser);
  }

  @Override
  public boolean parsePrimaryExpression() {
    final IElementType firstToken = builder.getTokenType();
    if (firstToken == JadeTokenTypes.INTERPOLATED_STRING_START) {
      parseInterpolatedString();
      return true;
    }
    if (firstToken == JSTokenTypes.IDENTIFIER && "attributes".equals(builder.getTokenText())) {
      return parser.getStatementParser().markVariable(false);
    }

    return super.parsePrimaryExpression();
  }

  private void parseInterpolatedString() {
    LOG.assertTrue(builder.getTokenType() == JadeTokenTypes.INTERPOLATED_STRING_START);

    PsiBuilder.Marker string = builder.mark();
    builder.advanceLexer();

    while (true) {
      IElementType tokenType = builder.getTokenType();

      if (tokenType == null) {
        break;
      }

      if (tokenType == JadeTokenTypes.INTERPOLATED_STRING_END) {
        builder.advanceLexer();
        break;
      }

      if (isTokenInterpolatedStringPart(tokenType)) {
        builder.advanceLexer();
        continue;
      }

      PsiBuilder.Marker expression = builder.mark();
      boolean parseSuccessful = !parseExpressionOptional();
      if (parseSuccessful) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"));
        builder.advanceLexer();
      }
      expression.done(JadeTokenTypes.INTERPOLATED_EXPRESSION);
    }

    string.done(JSElementTypes.PARENTHESIZED_EXPRESSION);
  }

  private static boolean isTokenInterpolatedStringPart(IElementType token) {
    return token == JadeTokenTypes.INTERPOLATED_STRING_START ||
           token == JadeTokenTypes.INTERPOLATED_STRING_END ||
           token == JadeTokenTypes.INTERPOLATED_STRING_PART;
  }
}
