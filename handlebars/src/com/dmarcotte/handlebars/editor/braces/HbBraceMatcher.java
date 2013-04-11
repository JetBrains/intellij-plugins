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

public class HbBraceMatcher implements BraceMatcher {

  private static final Set<IElementType> LEFT_BRACES = new HashSet<IElementType>();
  private static final Set<IElementType> RIGHT_BRACES = new HashSet<IElementType>();

  static {
    LEFT_BRACES.add(HbTokenTypes.OPEN);
    LEFT_BRACES.add(HbTokenTypes.OPEN_PARTIAL);
    LEFT_BRACES.add(HbTokenTypes.OPEN_UNESCAPED);
    LEFT_BRACES.add(HbTokenTypes.OPEN_BLOCK);
    LEFT_BRACES.add(HbTokenTypes.OPEN_INVERSE);

    RIGHT_BRACES.add(HbTokenTypes.CLOSE);
  }

  @Override
  public boolean isPairBraces(IElementType tokenType1, IElementType tokenType2) {
    return LEFT_BRACES.contains(tokenType1) && RIGHT_BRACES.contains(tokenType2)
           || RIGHT_BRACES.contains(tokenType1) && LEFT_BRACES.contains(tokenType2);
  }

  @Override
  public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
    return LEFT_BRACES.contains(iterator.getTokenType());
  }

  @Override
  public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
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
  public int getBraceTokenGroupId(IElementType tokenType) {
    return 1;
  }

  @Override
  public boolean isStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType) {
    return false;
  }

  @Nullable
  @Override
  public IElementType getOppositeBraceTokenType(@NotNull IElementType type) {
    return null;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
