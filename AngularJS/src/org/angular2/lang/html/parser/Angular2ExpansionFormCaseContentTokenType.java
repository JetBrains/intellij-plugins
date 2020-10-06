// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiElement;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.XmlASTWrapperPsiElement;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.jetbrains.annotations.NotNull;

final class Angular2ExpansionFormCaseContentTokenType extends HtmlCustomEmbeddedContentTokenType {

  public static final Angular2ExpansionFormCaseContentTokenType INSTANCE = new Angular2ExpansionFormCaseContentTokenType();

  private Angular2ExpansionFormCaseContentTokenType() {
    super("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage.INSTANCE);
  }

  @Override
  protected @NotNull Lexer createLexer() {
    return new Angular2HtmlLexer(true, null);
  }

  @Override
  protected void parse(@NotNull PsiBuilder builder) {
    new Angular2HtmlParsing(builder).parseExpansionFormContent();
  }

  @Override
  public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
    return new XmlASTWrapperPsiElement(node);
  }
}