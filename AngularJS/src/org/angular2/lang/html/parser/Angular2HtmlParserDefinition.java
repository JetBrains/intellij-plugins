// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.Angular2EmbeddedContentTokenType;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.XmlASTWrapperPsiElement;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlParserDefinition extends HTMLParserDefinition {

  static IFileElementType HTML_FILE = new IStubFileElementType(Angular2HtmlLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new Angular2HtmlLexer(true, null);
  }

  @NotNull
  @Override
  public PsiParser createParser(Project project) {
    return new Angular2HtmlParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return HTML_FILE;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new HtmlFileImpl(viewProvider, HTML_FILE);
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    if (node.getElementType() instanceof Angular2EmbeddedContentTokenType) {
      return new XmlASTWrapperPsiElement(node);
    }
    return super.createElement(node);
  }
}
