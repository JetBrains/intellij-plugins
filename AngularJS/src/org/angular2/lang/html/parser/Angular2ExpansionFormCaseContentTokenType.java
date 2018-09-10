// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.embedding.EmbeddingElementType;
import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementTypeBase;
import com.intellij.psi.tree.ILightLazyParseableElementType;
import com.intellij.util.CharTable;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.jetbrains.annotations.NotNull;

class Angular2ExpansionFormCaseContentTokenType extends IElementType
  implements EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase, ILightLazyParseableElementType {

  public static final Angular2ExpansionFormCaseContentTokenType INSTANCE = new Angular2ExpansionFormCaseContentTokenType();

  private Angular2ExpansionFormCaseContentTokenType() {
    super("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    return new LazyParseableElement(this, text);
  }

  @Override
  public ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getTreeBuilt().getFirstChildNode();
  }

  @Override
  public FlyweightCapableTreeStructure<LighterASTNode> parseContents(LighterLazyParseableNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getLightTree();
  }

  protected PsiBuilder doParseContents(@NotNull Object chameleon) {
    assert chameleon instanceof ASTNode || chameleon instanceof LighterLazyParseableNode : chameleon.getClass();

    Project project;
    if (chameleon instanceof ASTNode) {
      PsiElement psi = ((ASTNode)chameleon).getPsi();
      project = psi.getProject();
    }
    else {
      PsiFile file = ((LighterLazyParseableNode)chameleon).getContainingFile();
      assert file != null : "Let's add LighterLazyParseableNode#getProject() method";
      project = file.getProject();
    }

    CharSequence chars = chameleon instanceof ASTNode
                         ? ((ASTNode)chameleon).getChars()
                         : ((LighterLazyParseableNode)chameleon).getText();

    Lexer lexer = new Angular2HtmlLexer(true, null);

    PsiBuilder builder =
      chameleon instanceof ASTNode
      ? PsiBuilderFactory.getInstance().createBuilder(project, (ASTNode)chameleon, lexer,
                                                      Angular2HtmlLanguage.INSTANCE, chars)
      : PsiBuilderFactory.getInstance().createBuilder(project, (LighterLazyParseableNode)chameleon,
                                                      lexer, Angular2HtmlLanguage.INSTANCE, chars);

    Angular2HtmlParsing htmlParsing = new Angular2HtmlParsing(builder);
    htmlParsing.parseExpansionFormContent();
    return builder;
  }
}