// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.html.Angular2HtmlFileElementType;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorElementType;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlParserDefinition extends HTMLParserDefinition {

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new Angular2HtmlLexer( true, null);
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new Angular2HtmlParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return Angular2HtmlFileElementType.INSTANCE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new HtmlFileImpl(viewProvider, Angular2HtmlFileElementType.INSTANCE);
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    if (node.getElementType() instanceof Angular2HtmlNgContentSelectorElementType) {
      return ((Angular2HtmlNgContentSelectorElementType)node.getElementType()).createPsi(node);
    }
    return super.createElement(node);
  }
}
