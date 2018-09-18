// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Angular2EmbeddedContentTokenType extends IElementType
  implements EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase, ILightLazyParseableElementType {

  protected Angular2EmbeddedContentTokenType(@NotNull String debugName, @Nullable Language language) {
    super(debugName, language);
  }

  protected Angular2EmbeddedContentTokenType(@NotNull String debugName, @Nullable Language language, boolean register) {
    super(debugName, language, register);
  }

  @NotNull
  @Override
  public final ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    return new LazyParseableElement(this, text);
  }

  @Override
  public final ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getTreeBuilt().getFirstChildNode();
  }

  @Override
  public final FlyweightCapableTreeStructure<LighterASTNode> parseContents(LighterLazyParseableNode chameleon) {
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

    final CharSequence chars = chameleon instanceof ASTNode
                               ? ((ASTNode)chameleon).getChars()
                               : ((LighterLazyParseableNode)chameleon).getText();

    final Lexer lexer = createLexer();

    final PsiBuilder builder =
      chameleon instanceof ASTNode
      ? PsiBuilderFactory.getInstance().createBuilder(
        project, (ASTNode)chameleon, lexer, getLanguage(), chars)
      : PsiBuilderFactory.getInstance().createBuilder(
        project, (LighterLazyParseableNode)chameleon, lexer, getLanguage(), chars);

    parse(builder);
    return builder;
  }

  @NotNull
  protected abstract Lexer createLexer();

  protected abstract void parse(@NotNull PsiBuilder builder);
}
