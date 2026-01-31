// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.EmbeddedLazyParseableElementType;
import com.intellij.embedding.EmbeddingUtil;
import com.intellij.embedding.IndentEatingLexer;
import com.intellij.embedding.MasqueradingLexer;
import com.intellij.embedding.MasqueradingPsiBuilderAdapter;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.CssLazyStylesheet;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.plugins.jade.js.JSInJadeEmbeddedContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JadeEmbeddedTokenTypesWrapper extends EmbeddedLazyParseableElementType {
  private final IElementType myDelegate;

  public JadeEmbeddedTokenTypesWrapper(@NotNull IElementType delegate) {
    super("JADE_EMBEDDED_WRAPPER: " + delegate.toString(), delegate.getLanguage());
    myDelegate = delegate;
  }

  public IElementType getDelegate() {
    return myDelegate;
  }

  @Override
  public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
    final Lexer baseLexer;
    if (myDelegate instanceof EmbeddedLazyParseableElementType) {
      baseLexer = ((EmbeddedLazyParseableElementType)myDelegate).createLexer(chameleon, project);
    }
    else {
      baseLexer = LanguageParserDefinitions.INSTANCE.forLanguage(myDelegate.getLanguage()).createLexer(project);
    }

    final int baseIndent = EmbeddingUtil.calcBaseIndent(chameleon);
    return new JadeSimpleInterpolationLexer(new IndentEatingLexer(baseLexer, baseIndent));
  }

  @Override
  public PsiBuilder getBuilder(ASTNode chameleon, Project project, ParserDefinition parserDefinition, Lexer lexer, CharSequence chars) {
    if (myDelegate instanceof EmbeddedLazyParseableElementType) {
      return ((EmbeddedLazyParseableElementType)myDelegate).getBuilder(chameleon, project, parserDefinition, lexer, chars);
    }

    return new MasqueradingPsiBuilderAdapter(project, parserDefinition, ((MasqueradingLexer)lexer), chameleon, chars);
  }

  @Override
  public ASTNode parseAndGetTree(@NotNull PsiBuilder builder) {
    if (myDelegate instanceof EmbeddedLazyParseableElementType) {
      return ((EmbeddedLazyParseableElementType)myDelegate).parseAndGetTree(builder);
    }

    return super.parseAndGetTree(builder);
  }

  @Override
  public @Nullable ASTNode createNode(final CharSequence text) {
    if (myDelegate instanceof CssStylesheetLazyElementType) {
      return new CssLazyStylesheet(text, this);
    }

    return new LazyParseableElement(this, text) {

      @Override
      protected PsiElement createPsiNoLock() {
        // Since JS, coffee, etc. requires some special (JSExecScope) wrapper for the code to be held in, we need to
        // have this wrapper for embedded js to be JSExecScope
        if (JadeEmbeddedTokenTypesWrapper.this.myDelegate.getLanguage().isKindOf(JavascriptLanguage.INSTANCE)) {
          return new JSInJadeEmbeddedContentImpl(this);
        }

        final Language lang = getElementType().getLanguage();
        final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
        if (parserDefinition != null) {
          try {
            return parserDefinition.createElement(this);
          }
          catch (IncorrectOperationException ignored) {
          }
        }

        return new ASTWrapperPsiElement(this);
      }
    };
  }

}
