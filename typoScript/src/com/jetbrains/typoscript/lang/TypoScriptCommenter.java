/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.typoscript.lang;

import com.intellij.codeInsight.generation.CommenterDataHolder;
import com.intellij.codeInsight.generation.SelfManagingCommenter;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import com.jetbrains.typoscript.lang.psi.TypoScriptCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TypoScriptCommenter implements Commenter, SelfManagingCommenter<TypoScriptCommenter.MyCommenterData> {

  public static final String LINE_COMMENT_PREFIX = "#";
  public static final String BLOCK_COMMENT_PREFIX = "/*";
  public static final String BLOCK_COMMENT_SUFFIX = "*/";

  @Override
  public String getLineCommentPrefix() {
    return LINE_COMMENT_PREFIX;
  }

  @Override
  public String getBlockCommentPrefix() {
    return BLOCK_COMMENT_PREFIX;
  }

  @Override
  public String getBlockCommentSuffix() {
    return BLOCK_COMMENT_SUFFIX;
  }

  @Override
  public String getCommentedBlockCommentPrefix() {
    return BLOCK_COMMENT_PREFIX;
  }

  @Override
  public String getCommentedBlockCommentSuffix() {
    return BLOCK_COMMENT_SUFFIX;
  }

  @Override
  public MyCommenterData createLineCommentingState(int startLine,
                                                   int endLine,
                                                   @NotNull Document document,
                                                   @NotNull PsiFile file) {
    return MyCommenterData.INSTANCE;
  }

  @Override
  public MyCommenterData createBlockCommentingState(int selectionStart,
                                                    int selectionEnd,
                                                    @NotNull Document document,
                                                    @NotNull PsiFile file) {
    PsiElement beginElement = getPsiElementAtOffset(selectionStart, file);
    TypoScriptCompositeElement beginTypoElement = PsiTreeUtil.getParentOfType(beginElement, TypoScriptCompositeElement.class);
    boolean needNewLineBefore = beginTypoElement != null && beginTypoElement.getTextOffset() != selectionStart;

    PsiElement endElement = getPsiElementAtOffset(selectionEnd, file);
    TypoScriptCompositeElement endTypoElement = PsiTreeUtil.getParentOfType(endElement, TypoScriptCompositeElement.class);
    boolean needNewLineAfter = endTypoElement != null && endTypoElement.getTextOffset() + endTypoElement.getTextLength() != selectionEnd;

    return new MyCommenterData(needNewLineBefore, needNewLineAfter);
  }

  @Nullable
  private static PsiElement getPsiElementAtOffset(int offset, PsiFile file) {
    final FileViewProvider fileViewProvider = file.getViewProvider();
    file = fileViewProvider.getPsi(fileViewProvider.getBaseLanguage());
    final Language typoScriptLanguage = file.getLanguage();

    return fileViewProvider.findElementAt(offset, typoScriptLanguage);
  }


  @Override
  public void commentLine(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    document.insertString(offset, LINE_COMMENT_PREFIX);
  }

  @Override
  public void uncommentLine(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    document.deleteString(offset, offset + LINE_COMMENT_PREFIX.length());
  }

  @Override
  public boolean isLineCommented(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    return CharArrayUtil.regionMatches(document.getCharsSequence(), offset, LINE_COMMENT_PREFIX);
  }

  @Override
  public String getCommentPrefix(int line, @NotNull Document document, @NotNull MyCommenterData data) {
    return LINE_COMMENT_PREFIX;
  }

  @Override
  public TextRange getBlockCommentRange(int selectionStart,
                                        int selectionEnd,
                                        @NotNull Document document,
                                        @NotNull MyCommenterData data) {
    String commentSuffix = data.getBlockCommentSuffix();
    String commentPrefix = data.getBlockCommentPrefix();

    selectionStart = CharArrayUtil.shiftForward(document.getCharsSequence(), selectionStart, " \t\n");
    selectionEnd = CharArrayUtil.shiftBackward(document.getCharsSequence(), selectionEnd - 1, " \t\n") + 1;

    if (selectionEnd < selectionStart) {
      selectionEnd = selectionStart;
    }

    if (CharArrayUtil.regionMatches(document.getCharsSequence(), selectionEnd - commentSuffix.length(), commentSuffix) &&
        CharArrayUtil.regionMatches(document.getCharsSequence(), selectionStart, commentPrefix)) {
      return new TextRange(selectionStart, selectionEnd);
    }
    return null;
  }

  @Override
  public String getBlockCommentPrefix(int selectionStart, @NotNull Document document, @NotNull MyCommenterData data) {
    return data.getBlockCommentPrefix();
  }

  @Override
  public String getBlockCommentSuffix(int selectionEnd, @NotNull Document document, @NotNull MyCommenterData data) {
    return data.getBlockCommentSuffix();
  }

  @Override
  public void uncommentBlockComment(int startOffset, int endOffset, Document document, MyCommenterData data) {
    String commentSuffix = data.getBlockCommentSuffix();
    String commentPrefix = data.getBlockCommentPrefix();

    int startBlockLine = document.getLineNumber(startOffset);
    int endBlockLine = document.getLineNumber(endOffset);

    if (document.getCharsSequence().subSequence(document.getLineStartOffset(startBlockLine),
                                                document.getLineEndOffset(startBlockLine)).toString()
      .matches("\\s*\\Q" + commentPrefix + "\\E\\s*")) {
      if (document.getCharsSequence().subSequence(document.getLineStartOffset(endBlockLine),
                                                  document.getLineEndOffset(endBlockLine)).toString()
        .matches("\\s*\\Q" + commentSuffix + "\\E\\s*")) {
        document.deleteString(document.getLineStartOffset(endBlockLine), document.getLineEndOffset(endBlockLine) + 1);
        document.deleteString(document.getLineStartOffset(startBlockLine), document.getLineEndOffset(startBlockLine) + 1);
        return;
      }
    }

    document.deleteString(endOffset - commentSuffix.length(), endOffset);
    document.deleteString(startOffset, startOffset + commentPrefix.length());
  }

  @NotNull
  @Override
  public TextRange insertBlockComment(int startOffset, int endOffset, Document document, MyCommenterData data) {
    String prefix = data.getBlockCommentPrefix();
    document.insertString(startOffset, prefix);
    String suffix = data.getBlockCommentSuffix();
    document.insertString(endOffset + prefix.length(), suffix);
    return new TextRange(startOffset, startOffset + prefix.length() + suffix.length());
  }

  static class MyCommenterData extends CommenterDataHolder {
    private static final MyCommenterData INSTANCE = new MyCommenterData(false, false);

    final boolean myNeedNewLineBefore;
    final boolean myNeedNewLineAfter;


    MyCommenterData(boolean before, boolean after) {
      myNeedNewLineBefore = before;
      myNeedNewLineAfter = after;
    }

    @NotNull
    public String getBlockCommentPrefix() {
      if (myNeedNewLineBefore) {
        return "\n" + BLOCK_COMMENT_PREFIX;
      }
      else {
        return BLOCK_COMMENT_PREFIX;
      }
    }

    @NotNull
    public String getBlockCommentSuffix() {
      if (myNeedNewLineAfter) {
        return BLOCK_COMMENT_SUFFIX + "\n";
      }
      else {
        return BLOCK_COMMENT_SUFFIX;
      }
    }
  }
}
