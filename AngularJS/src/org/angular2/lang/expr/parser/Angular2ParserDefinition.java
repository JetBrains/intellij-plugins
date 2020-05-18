// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.lexer.Angular2Lexer;
import org.angular2.lang.html.XmlASTWrapperPsiElement;
import org.angular2.lang.html.parser.Angular2HtmlVarAttrTokenType;
import org.jetbrains.annotations.NotNull;

public class Angular2ParserDefinition extends JavascriptParserDefinition {
  public static final IFileElementType FILE = JSFileElementType.create(Angular2Language.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new Angular2Lexer();
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new Angular2PsiParser();
  }

  @Override
  public @NotNull JavaScriptParser<?, ?, ?, ?> createJSParser(@NotNull PsiBuilder builder) {
    return new Angular2Parser(builder);
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    if (node.getElementType() == Angular2HtmlVarAttrTokenType.REFERENCE
        || node.getElementType() == Angular2HtmlVarAttrTokenType.LET) {
      return new XmlASTWrapperPsiElement(node);
    }
    return super.createElement(node);
  }
}
