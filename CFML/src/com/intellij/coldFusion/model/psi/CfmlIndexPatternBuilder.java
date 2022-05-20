// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

/*
  User: Nadya.Zabrodina
 */

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;


/**
 */
public class CfmlIndexPatternBuilder implements IndexPatternBuilder {
  @Override
  public Lexer getIndexingLexer(@NotNull final PsiFile file) {
    if (file instanceof CfmlFile) {
      Project project = file.getProject();
      LayeredLexer cfmlLayeredLexer = new LayeredLexer(new CfmlLexer(true, project));
      cfmlLayeredLexer.registerLayer(new HtmlHighlightingLexer(), CfmlElementTypes.TEMPLATE_TEXT);

      return cfmlLayeredLexer;
    }
    return null;
  }

  private static final TokenSet tsCOMMENTS = TokenSet.create(CfmlTokenTypes.COMMENT, CfscriptTokenTypes.COMMENT);

  @Override
  public TokenSet getCommentTokenSet(@NotNull final PsiFile file) {
    if (file instanceof CfmlFile) {
      return tsCOMMENTS;
    }
    return null;
  }

  @Override
  public int getCommentStartDelta(final IElementType tokenType) {
    return 0;
  }

  @Override
  public int getCommentEndDelta(final IElementType tokenType) {
    return 0;
  }
}