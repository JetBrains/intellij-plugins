// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor.braces;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class HbBraceMatcher implements BraceMatcher {

  private static final Set<IElementType> LEFT_BRACES = new HashSet<>();
  private static final Set<IElementType> RIGHT_BRACES = new HashSet<>();

  static {
    LEFT_BRACES.add(HbTokenTypes.OPEN);
    LEFT_BRACES.add(HbTokenTypes.OPEN_PARTIAL);
    LEFT_BRACES.add(HbTokenTypes.OPEN_UNESCAPED);
    LEFT_BRACES.add(HbTokenTypes.OPEN_BLOCK);
    LEFT_BRACES.add(HbTokenTypes.OPEN_INVERSE);

    RIGHT_BRACES.add(HbTokenTypes.CLOSE);
    RIGHT_BRACES.add(HbTokenTypes.CLOSE_UNESCAPED);
  }

  @Override
  public boolean isPairBraces(@NotNull IElementType tokenType1, @NotNull IElementType tokenType2) {
    return LEFT_BRACES.contains(tokenType1) && RIGHT_BRACES.contains(tokenType2)
           || RIGHT_BRACES.contains(tokenType1) && LEFT_BRACES.contains(tokenType2);
  }

  @Override
  public boolean isLBraceToken(@NotNull HighlighterIterator iterator, @NotNull CharSequence fileText, @NotNull FileType fileType) {
    return LEFT_BRACES.contains(iterator.getTokenType());
  }

  @Override
  public boolean isRBraceToken(@NotNull HighlighterIterator iterator, @NotNull CharSequence fileText, @NotNull FileType fileType) {
    if (!RIGHT_BRACES.contains(iterator.getTokenType())) {
      // definitely not a right brace
      return false;
    }

    boolean isRBraceToken = false;
    int iteratorRetreatCount = 0;
    while (true) {
      iterator.retreat();
      iteratorRetreatCount++;

      if (iterator.atEnd()) {
        break;
      }

      if (iterator.getTokenType() == HbTokenTypes.OPEN_BLOCK) {
        // the first open type token we encountered is a block opener,
        // so this is not a close brace (the paired close brace for these tokens
        // is at the end of the corresponding block close 'stache)
        break;
      }

      if (iterator.getTokenType() == HbTokenTypes.OPEN_INVERSE) {
        // this might be a simple inverse, so backtrack until we either see
        // and ID (which means we're in a situation like OPEN_BLOCK above)
        // or a CLOSE (which means we're a simple inverse, and this is the RBrace)
        while (iteratorRetreatCount-- > 0) {
          iterator.advance();
          if (iterator.getTokenType() == HbTokenTypes.ID) {
            break;
          }

          if (iterator.getTokenType() == HbTokenTypes.CLOSE) {
            isRBraceToken = true;
            break;
          }
        }
        break;
      }

      if (iterator.getTokenType() == HbTokenTypes.OPEN
          || iterator.getTokenType() == HbTokenTypes.OPEN_PARTIAL
          || iterator.getTokenType() == HbTokenTypes.OPEN_UNESCAPED
          || iterator.getTokenType() == HbTokenTypes.OPEN_ENDBLOCK) {
        // the first open token we encountered was a simple opener (i.e. didn't start a block)
        // or the close brace of a close block 'stache for some open block.  Definitely a right brace.
        isRBraceToken = true;
      }
    }

    // reset the given iterator before returning
    while (iteratorRetreatCount-- > 0) {
      iterator.advance();
    }

    return isRBraceToken;
  }

  @Override
  public int getBraceTokenGroupId(@NotNull IElementType tokenType) {
    return 1;
  }

  @Override
  public boolean isStructuralBrace(@NotNull HighlighterIterator iterator, @NotNull CharSequence text, @NotNull FileType fileType) {
    return false;
  }

  @Override
  public @Nullable IElementType getOppositeBraceTokenType(@NotNull IElementType type) {
    return null;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
