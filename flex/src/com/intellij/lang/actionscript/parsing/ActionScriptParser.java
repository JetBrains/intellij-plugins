// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptParser extends JavaScriptParser<ExpressionParser, ActionScriptStatementParser, ActionScriptFunctionParser, JSPsiTypeParser> {
  public ActionScriptParser(PsiBuilder builder) {
    super(JavaScriptSupportLoader.ECMA_SCRIPT_L4, builder);
    myStatementParser = new ActionScriptStatementParser(this);
    myFunctionParser = new ActionScriptFunctionParser(this);
    myExpressionParser = new ActionScriptExpressionParser(this);
  }

  @Override
  public boolean isIdentifierToken(IElementType tokenType) {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(tokenType);
  }

  @Override
  public void buildTokenElement(@NotNull IElementType type) {
    final PsiBuilder.Marker marker = builder.mark();
    builder.advanceLexer();
    if (builder.getTokenType() == JSTokenTypes.GENERIC_SIGNATURE_START) {
      myTypeParser.parseECMA4GenericSignature();
    }
    marker.done(type);
  }

  @Override
  protected void doParseJS() {
    while (!builder.eof()) {
      if (builder.getTokenType() == JSTokenTypes.AT) {
        builder.advanceLexer();
        getStatementParser().parseAttributeBody();
      }
      else {
        getStatementParser().parseStatement();
      }
    }
  }
}
