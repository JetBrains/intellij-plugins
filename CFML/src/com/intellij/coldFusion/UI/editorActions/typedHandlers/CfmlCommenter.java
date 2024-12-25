// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.generation.CommenterDataHolder;
import com.intellij.codeInsight.generation.SelfManagingCommenter;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.impl.CfmlTagScriptImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
// TODO: uncomment html style comments
public final class CfmlCommenter implements Commenter, SelfManagingCommenter<CfmlCommenter.MyCommenterData> {
  private static final String CF_SCRIPT_BLOCK_COMMENT_PREFIX = "/*";
  private static final String CF_SCRIPT_BLOCK_COMMENT_SUFFIX = "*/";
  private static final String CF_SCRIPT_LINE_COMMENT_PREFIX = "//";
  private static final String CFML_COMMENT_PREFIX = "<!---";
  private static final String CFML_COMMENT_SUFFIX = "--->";

  @Override
  public String getLineCommentPrefix() {
    return null;
  }

  @Override
  public String getBlockCommentPrefix() {
    return "<!---";
  }

  @Override
  public String getBlockCommentSuffix() {
    return "--->";
  }

  @Override
  public String getCommentedBlockCommentPrefix() {
    return "&lt;!&mdash;";
  }

  @Override
  public String getCommentedBlockCommentSuffix() {
    return "&mdash;&gt;";
  }

  private static boolean isOffsetWithinCfscript(int offset, @NotNull Document document, @NotNull PsiFile file) {
    PsiElement at = getCfmlElementAtOffset(offset, file);
    if (at != null) {
      CfmlTagScriptImpl scriptTag = PsiTreeUtil.getParentOfType(at, CfmlTagScriptImpl.class);
      if (scriptTag != null) {
        return scriptTag.isInsideTag(offset);
      }
      else {
        PsiElement firstChild = file.getFirstChild();
        if (firstChild != null) {
          ASTNode theDeepestChild = firstChild.getNode();
          if (theDeepestChild == null) {
            return false;
          }
          while (theDeepestChild.getFirstChildNode() != null) {
            theDeepestChild = theDeepestChild.getFirstChildNode();
          }
          IElementType elementType = theDeepestChild.getElementType();
          if (elementType == CfscriptTokenTypes.COMMENT ||
              elementType == CfscriptTokenTypes.COMPONENT_KEYWORD ||
              elementType == CfscriptTokenTypes.INTERFACE_KEYWORD ||
              elementType == CfscriptTokenTypes.IMPORT_KEYWORD) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static PsiElement getCfmlElementAtOffset(int offset, PsiFile file) {
    final FileViewProvider fileViewProvider = file.getViewProvider();
    file = fileViewProvider.getPsi(fileViewProvider.getBaseLanguage());
    final Language cfmlLanguage = file.getLanguage();

    return fileViewProvider.findElementAt(offset, cfmlLanguage);
  }

  private static int skipWhiteSpaces(int offset, PsiFile file) {
    PsiElement at = getCfmlElementAtOffset(offset, file);
    if (at != null && at.getNode().getElementType() == CfmlTokenTypes.WHITE_SPACE) {
      return at.getTextRange().getEndOffset();
    }
    return offset;
  }

  @Override
  public MyCommenterData createLineCommentingState(int startLine, int endLine, @NotNull Document document, @NotNull PsiFile file) {
    int lineStartOffset = document.getLineStartOffset(startLine);
    return new MyCommenterData(isOffsetWithinCfscript(lineStartOffset, document, file), lineStartOffset);
  }

  @Override
  public MyCommenterData createBlockCommentingState(int selectionStart,
                                                    int selectionEnd,
                                                    @NotNull Document document,
                                                    @NotNull PsiFile file) {
    return new MyCommenterData(isOffsetWithinCfscript(selectionStart, document, file), selectionStart);
  }

  @Override
  public void commentLine(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    final int originalLineEndOffset = document.getLineEndOffset(line);
    offset = CharArrayUtil.shiftForward(document.getCharsSequence(), offset, " \t");
    if (data.isIsWithinCfscript()) {
      document.insertString(offset, CF_SCRIPT_LINE_COMMENT_PREFIX);
    }
    else {
      document.insertString(originalLineEndOffset, CFML_COMMENT_SUFFIX);
      document.insertString(offset, CFML_COMMENT_PREFIX);
    }
  }

  @Override
  public void uncommentLine(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    int rangeEnd = document.getLineEndOffset(line);

    if (!data.isIsWithinCfscript()) {
      document.deleteString(
        rangeEnd - CFML_COMMENT_SUFFIX.length(),
        rangeEnd);
    }

    final String commentPrefix = data.getLineCommentPrefix();
    document.deleteString(
      offset,
      offset + commentPrefix.length());
  }

  @Override
  public boolean isLineCommented(int line, int offset, @NotNull Document document, @NotNull MyCommenterData data) {
    int rangeEnd = document.getLineEndOffset(line);

    boolean commented = true;

    if (!data.isIsWithinCfscript()) {
      final String commentSuffix = CFML_COMMENT_SUFFIX;
      if (!CharArrayUtil.regionMatches(
        document.getCharsSequence(),
        rangeEnd - commentSuffix.length(),
        commentSuffix
      )) {
        commented = false;
      }
    }

    final String commentPrefix = data.getLineCommentPrefix();
    if (commented && !CharArrayUtil.regionMatches(
      document.getCharsSequence(),
      offset,
      commentPrefix
    )) {
      commented = false;
    }
    return commented;
  }

  @Override
  public String getCommentPrefix(int line, @NotNull Document document, @NotNull MyCommenterData data) {
    return data.getLineCommentPrefix();
  }

  // TODO: to uncomment all commented blocks withing selection
  @Override
  public TextRange getBlockCommentRange(int selectionStart, int selectionEnd, @NotNull Document document, @NotNull MyCommenterData data) {
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

  @Override
  public @NotNull TextRange insertBlockComment(int startOffset, int endOffset, Document document, MyCommenterData data) {
    int startLineNumber = document.getLineNumber(startOffset);
    int startLineStart = document.getLineStartOffset(startLineNumber);
    if (startLineStart == startOffset) {
      int endLineNumber = document.getLineNumber(endOffset);
      int endLineStart = document.getLineStartOffset(endLineNumber);
      if (endLineStart == endOffset) {
        String commentStart = data.getBlockCommentPrefix() + "\n";
        String commentEnd = data.getBlockCommentSuffix() + "\n";
        document.insertString(endOffset, commentEnd);
        document.insertString(startOffset, commentStart);
        return new TextRange(startOffset, endOffset + commentStart.length() + commentEnd.length());
      }
    }
    document.insertString(endOffset, data.getBlockCommentSuffix());
    document.insertString(startOffset, data.getBlockCommentPrefix());
    return new TextRange(startOffset, endOffset + data.getBlockCommentSuffix().length() + data.getBlockCommentPrefix().length());
  }

  static final class MyCommenterData extends CommenterDataHolder {
    private final boolean myIsWithinCfscript;
    private final int myStartOffset;

    private MyCommenterData(boolean isWithinCfscript, int startOffset) {
      myIsWithinCfscript = isWithinCfscript;
      myStartOffset = startOffset;
    }

    public boolean isIsWithinCfscript() {
      return myIsWithinCfscript;
    }

    public String getLineCommentPrefix() {
      if (myIsWithinCfscript) {
        return CfmlCommenter.CF_SCRIPT_LINE_COMMENT_PREFIX;
      }
      return CfmlCommenter.CFML_COMMENT_PREFIX;
    }

    public String getBlockCommentSuffix() {
      if (myIsWithinCfscript) {
        return CfmlCommenter.CF_SCRIPT_BLOCK_COMMENT_SUFFIX;
      }
      return CfmlCommenter.CFML_COMMENT_SUFFIX;
    }

    public String getBlockCommentPrefix() {
      if (myIsWithinCfscript) {
        return CfmlCommenter.CF_SCRIPT_BLOCK_COMMENT_PREFIX;
      }
      return CfmlCommenter.CFML_COMMENT_PREFIX;
    }

    public int getStartOffset() {
      return myStartOffset;
    }
  }
}
