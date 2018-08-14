// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlPairedBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] PAIRS = new BracePair[]{
    new BracePair(CfscriptTokenTypes.L_BRACKET, CfscriptTokenTypes.R_BRACKET, false),
    new BracePair(CfscriptTokenTypes.L_SQUAREBRACKET, CfscriptTokenTypes.R_SQUAREBRACKET, false),
    new BracePair(CfscriptTokenTypes.L_CURLYBRACKET, CfscriptTokenTypes.R_CURLYBRACKET, true),
    new BracePair(CfscriptTokenTypes.OPENSHARP, CfscriptTokenTypes.CLOSESHARP, true)
  };

  @Override
  @NotNull
  public BracePair[] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType tokenType) {
    return lbraceType != CfscriptTokenTypes.L_CURLYBRACKET;
  }

  @Override
  public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
    PsiElement element = file.findElementAt(openingBraceOffset);
    if (element == null || element instanceof PsiFile) return openingBraceOffset;
    PsiElement parent = element.getParent();
    return parent.getTextRange().getStartOffset();
  }
}
