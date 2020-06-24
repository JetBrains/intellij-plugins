// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.embedding.EmbeddingElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementTypeBase;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Angular2EmbeddedContentTokenType extends IElementType
  implements EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase {

  protected Angular2EmbeddedContentTokenType(@NotNull @NonNls String debugName, @Nullable Language language) {
    super(debugName, language);
  }

  protected Angular2EmbeddedContentTokenType(@NotNull @NonNls String debugName, @Nullable Language language, boolean register) {
    super(debugName, language, register);
  }

  @Override
  public final @NotNull ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    return new LazyParseableElement(this, text);
  }

  @Override
  public final ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getTreeBuilt().getFirstChildNode();
  }

  protected PsiBuilder doParseContents(@NotNull ASTNode chameleon) {
    Project project;
    PsiElement psi = chameleon.getPsi();
    project = psi.getProject();

    final CharSequence chars = chameleon.getChars();

    final Lexer lexer = createLexer();

    final PsiBuilder builder =
      PsiBuilderFactory.getInstance().createBuilder(
        project, chameleon, lexer, getLanguage(), chars);

    parse(builder);
    return builder;
  }

  protected abstract @NotNull Lexer createLexer();

  protected abstract void parse(@NotNull PsiBuilder builder);
}
