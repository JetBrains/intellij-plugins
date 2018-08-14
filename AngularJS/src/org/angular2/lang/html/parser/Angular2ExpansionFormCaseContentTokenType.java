// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.CharTable;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.jetbrains.annotations.NotNull;

class Angular2ExpansionFormCaseContentTokenType extends IElementType implements ICustomParsingType {

  public static final Angular2ExpansionFormCaseContentTokenType INSTANCE = new Angular2ExpansionFormCaseContentTokenType();

  private Angular2ExpansionFormCaseContentTokenType() {
    super("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    ParserDefinition parserDef = LanguageParserDefinitions.INSTANCE.forLanguage(Angular2HtmlLanguage.INSTANCE);
    Lexer lexer = new Angular2HtmlLexer(true, null);
    PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(parserDef, lexer, text);

    Angular2HtmlParsing htmlParsing = new Angular2HtmlParsing(builder);
    htmlParsing.parseExpansionFormContent();
    return builder.getTreeBuilt();
  }
}