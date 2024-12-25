// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.BracePair;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageBraceMatching;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeExtensionPoint;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlBraceMatcher implements BraceMatcher {
  private static final BracePair[] PAIRS = new BracePair[]{
    // new BracePair(CfmlTokenTypes.OPENER, CfmlTokenTypes.CLOSER, true),
    new BracePair(CfscriptTokenTypes.L_BRACKET, CfscriptTokenTypes.R_BRACKET, false),
    new BracePair(CfscriptTokenTypes.L_SQUAREBRACKET, CfscriptTokenTypes.R_SQUAREBRACKET, false),
    new BracePair(CfscriptTokenTypes.L_CURLYBRACKET, CfscriptTokenTypes.R_CURLYBRACKET, false),
    new BracePair(CfscriptTokenTypes.OPENSHARP, CfscriptTokenTypes.CLOSESHARP, true),/*
            new BracePair(CfscriptTokenTypes.DOUBLE_QUOTE, CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER, false),
            new BracePair(CfscriptTokenTypes.SINGLE_QUOTE, CfscriptTokenTypes.SINGLE_QUOTE_CLOSER, false),*/
    new BracePair(CfmlTokenTypes.START_EXPRESSION, CfmlTokenTypes.END_EXPRESSION, true)/*,
            new BracePair(CfmlTokenTypes.DOUBLE_QUOTE, CfmlTokenTypes.DOUBLE_QUOTE_CLOSER, false),
            new BracePair(CfmlTokenTypes.SINGLE_QUOTE, CfmlTokenTypes.SINGLE_QUOTE_CLOSER, false)*/
  };

  @Override
  public int getBraceTokenGroupId(@NotNull IElementType tokenType) {
    final Language l = tokenType.getLanguage();
    return l.hashCode();
        /*
        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(l);
        if (matcher != null) {
          BracePair[] pairs = matcher.getPairs();
          for (BracePair pair : pairs) {
            if (pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType ) {
              return l.hashCode();
            }
          }
        }
        FileType tokenFileType = tokenType.getLanguage().getAssociatedFileType();
        if (tokenFileType != CfmlFileType.INSTANCE) {
            for(FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
                if (tokenFileType.getName().equals(ext.filetype)) {
                    return ext.getInstance().getBraceTokenGroupId(tokenType);
                }
            }
        }
        return l.hashCode();
        */
  }

  @Override
  public boolean isLBraceToken(@NotNull HighlighterIterator iterator, @NotNull CharSequence fileText, @NotNull FileType fileType) {
    final IElementType tokenType = iterator.getTokenType();
    PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
    if (matcher != null) {
      BracePair[] pairs = matcher.getPairs();
      for (BracePair pair : pairs) {
        if (pair.getLeftBraceType() == tokenType) return true;
      }
    }

    if (!tokenType.getLanguage().equals(CfmlLanguage.INSTANCE)) {
      FileType tokenFileType = iterator.getTokenType().getLanguage().getAssociatedFileType();
      if (tokenFileType != null && tokenFileType != CfmlFileType.INSTANCE) {
        for (FileTypeExtensionPoint<BraceMatcher> ext : BraceMatcher.EP_NAME.getExtensionList()) {
          if (ext.filetype != null && ext.filetype.equals(tokenFileType.getName())) {
            return ext.getInstance().isLBraceToken(iterator, fileText,
                                                   tokenFileType instanceof XmlFileType ? HtmlFileType.INSTANCE : tokenFileType);
          }
        }
      }
    }

    for (BracePair pair : PAIRS) {
      if (pair.getLeftBraceType() == tokenType) {
        return true;
      }
    }
    return tokenType.equals(CfmlTokenTypes.OPENER) &&
           (!CfmlUtil.isEndTagRequired(getTagName(fileText, iterator), null) || findEndTag(fileText, iterator));
  }

  @Override
  public boolean isRBraceToken(@NotNull HighlighterIterator iterator, @NotNull CharSequence fileText, @NotNull FileType fileType) {
    final IElementType tokenType = iterator.getTokenType();

    PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
    if (matcher != null) {
      BracePair[] pairs = matcher.getPairs();
      for (BracePair pair : pairs) {
        if (pair.getRightBraceType() == tokenType) return true;
      }
    }

    if (!tokenType.getLanguage().equals(CfmlLanguage.INSTANCE)) {
      FileType tokenFileType = iterator.getTokenType().getLanguage().getAssociatedFileType();
      if (tokenFileType != null && tokenFileType != CfmlFileType.INSTANCE) {
        for (FileTypeExtensionPoint<BraceMatcher> ext : BraceMatcher.EP_NAME.getExtensionList()) {
          if (ext.filetype != null && ext.filetype.equals(tokenFileType.getName())) {
            return ext.getInstance().isRBraceToken(iterator, fileText,
                                                   tokenFileType instanceof XmlFileType ? HtmlFileType.INSTANCE : tokenFileType);
          }
        }
      }
    }

    for (BracePair pair : PAIRS) {
      if (pair.getRightBraceType() == tokenType) return true;
    }
    return ((tokenType.equals(CfmlTokenTypes.CLOSER)) && findBeginTag(fileText, iterator)) ||
           (tokenType.equals(CfmlTokenTypes.R_ANGLEBRACKET) &&
            !CfmlUtil.isEndTagRequired(getTagName(fileText, iterator), null) &&
            !findEndTag(fileText, iterator));
  }

  @Override
  public boolean isPairBraces(@NotNull IElementType tokenType1, @NotNull IElementType tokenType2) {
    PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType1.getLanguage());
    if (matcher != null) {
      BracePair[] pairs = matcher.getPairs();
      for (BracePair pair : pairs) {
        if (pair.getLeftBraceType() == tokenType1) return pair.getRightBraceType() == tokenType2;
        if (pair.getRightBraceType() == tokenType1) return pair.getLeftBraceType() == tokenType2;
      }
    }

    FileType tokenFileType1 = tokenType1.getLanguage().getAssociatedFileType();
    FileType tokenFileType2 = tokenType2.getLanguage().getAssociatedFileType();
    if (tokenFileType2 != tokenFileType1) {
      return false;
    }
    if (tokenFileType1 != CfmlFileType.INSTANCE && tokenFileType1 != null) {
      for (FileTypeExtensionPoint<BraceMatcher> ext : BraceMatcher.EP_NAME.getExtensionList()) {
        if (ext.filetype.equals(tokenFileType1.getName())) {
          return ext.getInstance().isPairBraces(tokenType1, tokenType2);
        }
      }
    }

    for (BracePair pair : PAIRS) {
      if (pair.getLeftBraceType() == tokenType1) return pair.getRightBraceType() == tokenType2;
      if (pair.getRightBraceType() == tokenType1) return pair.getLeftBraceType() == tokenType2;
    }
    return (tokenType1.equals(CfmlTokenTypes.OPENER) && tokenType2.equals(CfmlTokenTypes.CLOSER)) ||
           (tokenType1.equals(CfmlTokenTypes.CLOSER) && tokenType2.equals(CfmlTokenTypes.OPENER)) ||
           (tokenType1.equals(CfmlTokenTypes.R_ANGLEBRACKET) && tokenType2.equals(CfmlTokenTypes.OPENER)) ||
           (tokenType1.equals(CfmlTokenTypes.OPENER) && tokenType2.equals(CfmlTokenTypes.R_ANGLEBRACKET));
  }

  @Override
  public boolean isStructuralBrace(@NotNull HighlighterIterator iterator, @NotNull CharSequence text, @NotNull FileType fileType) {
    IElementType type = iterator.getTokenType();
    if (type == CfscriptTokenTypes.L_BRACKET || type == CfscriptTokenTypes.R_BRACKET) {
      return false;
    }
    return true;
        /*
        for(FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
            if (StdFileTypes.HTML.getName().equals(ext.filetype)) {
                return ext.getInstance().isStructuralBrace(iterator, text, StdFileTypes.HTML);
            }
        }
        IElementType tokenType = iterator.getTokenType();

        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
        if (matcher != null) {
          BracePair[] pairs = matcher.getPairs();
          for (BracePair pair : pairs) {
            if ((pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType) &&
                pair.isStructural()) return true;
          }
        }
        for (BracePair pair : PAIRS) {
            if ((pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType) &&
                    pair.isStructural()) return true;
        }
        return tokenType.equals(CfmlTokenTypes.OPENER) || tokenType.equals(CfmlTokenTypes.CLOSER);
        */
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(final @NotNull IElementType lbraceType, final @Nullable IElementType contextType) {
    return true;
  }

  private static boolean findBeginTag(CharSequence fileText, HighlighterIterator iterator) {
    IElementType tokenType;
    final String name = getTagName(fileText, iterator);
    if (name == null) {
      return false;
    }
    int balance = 0;
    int count = 0;
    while (balance < 1) {
      iterator.retreat();
      count++;
      if (iterator.atEnd()) break;
      tokenType = iterator.getTokenType();
      String currentTagName = getTagName(fileText, iterator);
      if (tokenType == CfmlTokenTypes.CLOSER && name.equals(currentTagName)) {
        balance--;
      }
      else if (tokenType == CfmlTokenTypes.OPENER &&
               name.equals(currentTagName)) {
        balance++;
      }
    }
    while (count-- > 0) iterator.advance();
    return balance == 1;
  }

  private static boolean findEndTag(CharSequence fileText, HighlighterIterator iterator) {
    IElementType tokenType;
    final String name = getTagName(fileText, iterator);
    if (name == null) {
      return false;
    }
    int balance = 0;
    int count = 0;
    while (balance > -1 && !iterator.atEnd()) {
      iterator.advance();
      count++;
      if (iterator.atEnd()) break;
      tokenType = iterator.getTokenType();
      String currrentTagName = getTagName(fileText, iterator);
      if (tokenType == CfmlTokenTypes.OPENER &&
          name.equals(currrentTagName)) {
        balance++;
      }
      else if (tokenType == CfmlTokenTypes.CLOSER && name.equals(currrentTagName)) {
        balance--;
      }
    }
    while (count-- > 0) iterator.retreat();
    return balance == -1;
  }

  public static @Nullable String getTagName(CharSequence fileText, HighlighterIterator iterator) {
    final IElementType tokenType = iterator.getTokenType();
    String name = null;
    if (tokenType == CfmlTokenTypes.CLOSER || tokenType == CfmlTokenTypes.R_ANGLEBRACKET) {
      iterator.retreat();
      IElementType tokenType1 = (!iterator.atEnd() ? iterator.getTokenType() : null);

      if (tokenType1 == CfmlTokenTypes.CF_TAG_NAME) {
        name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
      }
      else {
        int counter = 0;
        while (!iterator.atEnd()) {
          if (iterator.getTokenType() == CfmlTokenTypes.CF_TAG_NAME) {
            name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
            break;
          }
          if (iterator.getTokenType() == CfmlTokenTypes.CLOSER ||
              iterator.getTokenType() == CfmlTokenTypes.R_ANGLEBRACKET) {
            break;
          }
          iterator.retreat();
          counter++;
        }
        while (counter-- > 0) iterator.advance();
      }
      iterator.advance();
    }
    else if (tokenType == CfmlTokenTypes.OPENER) {
      iterator.advance();
      IElementType tokenType1 = (!iterator.atEnd() ? iterator.getTokenType() : null);

      if (tokenType1 == CfmlTokenTypes.CF_TAG_NAME) {
        name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
      }
      iterator.retreat();
    }
    return name == null ? name : StringUtil.toLowerCase(name);
  }

  @Override
  public IElementType getOppositeBraceTokenType(final @NotNull IElementType type) {
    for (BracePair pair : PAIRS) {
      if (pair.getLeftBraceType() == type) return pair.getRightBraceType();
      if (pair.getRightBraceType() == type) return pair.getLeftBraceType();
    }
    if (type == CfmlTokenTypes.OPENER) return CfmlTokenTypes.R_ANGLEBRACKET;
    if (type == CfmlTokenTypes.CLOSER) return CfmlTokenTypes.OPENER;
    if (type == CfmlTokenTypes.R_ANGLEBRACKET) return CfmlTokenTypes.OPENER;
    return null;
  }

  @Override
  public int getCodeConstructStart(final @NotNull PsiFile file, int openingBraceOffset) {
    PsiElement element = file.findElementAt(openingBraceOffset);
    if (element == null || element instanceof PsiFile) return openingBraceOffset;
    PsiElement parent = element.getParent();
    return parent.getTextRange().getStartOffset();
  }
}
