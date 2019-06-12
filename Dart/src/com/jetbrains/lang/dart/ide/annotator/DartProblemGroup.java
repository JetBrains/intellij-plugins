// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressableProblemGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.DartFile;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartProblemGroup implements SuppressableProblemGroup {
  private static final SuppressIntentionAction[] NO_ACTIONS = {};

  @NotNull private final String myErrorCode;
  @NotNull private final String myErrorSeverity;

  public DartProblemGroup(@NotNull final String errorCode, @NotNull final String errorSeverity) {
    myErrorCode = errorCode;
    myErrorSeverity = errorSeverity;
  }

  @NotNull
  @Override
  public SuppressIntentionAction[] getSuppressActions(@Nullable final PsiElement element) {
    if (element != null && element.getContainingFile() instanceof DartFile) {
      return new SuppressIntentionAction[]{
        new DartSuppressAction(myErrorCode, myErrorSeverity, false, false),
        new DartSuppressAction(myErrorCode, myErrorSeverity, false, true)
      };
    }
    return NO_ACTIONS;
  }

  @Nullable
  @Override
  public String getProblemName() {
    return null;
  }

  public static class DartSuppressAction extends SuppressIntentionAction implements Comparable<IntentionAction> {
    public static final String IGNORE_PREFIX = "ignore:";
    @NotNull private final String myErrorCode;
    @NotNull private final String myErrorSeverity;
    private final boolean myEolComment;

    /**
     * @param eolComment {@code true} means that {@code //ignore} comment should be placed in the end of the current line, {@code false} -> on previous line
     */
    public DartSuppressAction(@NotNull String errorCode, @NotNull String errorSeverity, boolean topLevelAction, boolean eolComment) {
      myErrorCode = errorCode;
      myErrorSeverity = errorSeverity;
      myEolComment = eolComment;
      String severityText = errorSeverity.equals(AnalysisErrorSeverity.INFO) ? "warning" : StringUtil.toLowerCase(errorSeverity);
      if (topLevelAction) {
        // Suppress 'unused_local' warning
        setText("Suppress '" + errorCode + "' " + severityText);
      }
      else if (eolComment) {
        // Suppress 'unused_local' using EOL comment
        setText("Suppress '" + errorCode + "' using EOL comment");
      }
      else {
        // Suppress 'unused_local'
        setText("Suppress '" + errorCode + "'");
      }
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "Suppress errors and warnings in Dart code";
    }

    @Override
    public int compareTo(final IntentionAction o) {
      if (o instanceof DartSuppressAction) {
        return ((DartSuppressAction)o).myEolComment ? -1 : 1;
      }
      return 0;
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
      if (editor == null) return false;
      if (myErrorSeverity.equals(AnalysisErrorSeverity.ERROR)) return false;

      final Document document = editor.getDocument();
      final int line = document.getLineNumber(element.getTextRange().getStartOffset());
      // if //ignore: comment is already there then suggest to update it, but do not suggest to add another one
      if (myEolComment) {
        return !hasIgnoreCommentOnPrevLine(document, line) || hasEolIgnoreComment(document, line);
      }
      else {
        return !hasEolIgnoreComment(document, line) || hasIgnoreCommentOnPrevLine(document, line);
      }
    }

    private static boolean hasEolIgnoreComment(@NotNull final Document document, final int line) {
      final CharSequence lineText =
        document.getCharsSequence().subSequence(document.getLineStartOffset(line), document.getLineEndOffset(line));
      if (!StringUtil.contains(lineText, IGNORE_PREFIX)) return false;

      int index = lineText.toString().lastIndexOf(IGNORE_PREFIX);
      while (index > 0) {
        char ch = lineText.charAt(--index);
        switch (ch) {
          case ' ':
            continue;
          case '/':
            return index >= 2 && lineText.charAt(index - 1) == '/' && lineText.charAt(index - 2) != '/';
          default:
            return false;
        }
      }
      return false;
    }

    private static boolean hasIgnoreCommentOnPrevLine(@NotNull final Document document, final int line) {
      if (line == 0) return false;

      final CharSequence prevLine =
        document.getCharsSequence().subSequence(document.getLineStartOffset(line - 1), document.getLineEndOffset(line - 1));

      int index = -1;
      while (++index < prevLine.length()) {
        char ch = prevLine.charAt(index);
        switch (ch) {
          case ' ':
            continue;
          case '/':
            if (prevLine.length() > index + 1 && prevLine.charAt(index + 1) == '/') {
              final String comment = prevLine.subSequence(index + 2, prevLine.length()).toString();
              if (StringUtil.trimLeading(comment, ' ').startsWith(IGNORE_PREFIX)) {
                return true;
              }
            }
            return false;
          default:
            return false;
        }
      }
      return false;
    }

    @Override
    public void invoke(@NotNull final Project project,
                       final Editor editor,
                       @NotNull final PsiElement element) throws IncorrectOperationException {
      if (editor == null) return;

      final Document document = editor.getDocument();
      final int line = document.getLineNumber(element.getTextRange().getStartOffset());

      if (myEolComment) {
        if (hasEolIgnoreComment(document, line)) {
          appendErrorCode(document, line, myErrorCode);
        }
        else {
          addEolComment(document, line, myErrorCode);
        }
      }
      else {
        if (hasIgnoreCommentOnPrevLine(document, line)) {
          appendErrorCode(document, line - 1, myErrorCode);
        }
        else {
          addCommentOnPrevLine(document, line, myErrorCode);
        }
      }
    }

    private static void appendErrorCode(@NotNull final Document document, final int line, @NotNull final String errorCode) {
      final int lineEndOffset = document.getLineEndOffset(line);
      int index = lineEndOffset - 1;
      while (index >= 0 && document.getCharsSequence().charAt(index) == ' ') {
        index--;
      }
      document.replaceString(index + 1, lineEndOffset, ", " + errorCode);
    }

    private static void addEolComment(@NotNull final Document document, final int line, @NotNull final String errorCode) {
      final int lineStartOffset = document.getLineStartOffset(line);
      final int lineEndOffset = document.getLineEndOffset(line);
      final CharSequence lineText = document.getCharsSequence().subSequence(lineStartOffset, lineEndOffset);
      final int commentIndex = StringUtil.indexOf(lineText, "//");
      if (commentIndex >= 0) {
        // before existing comment
        document.insertString(lineStartOffset + commentIndex, "// ignore: " + errorCode + ", ");
      }
      else {
        int index = lineEndOffset - 1;
        while (index >= 0 && document.getCharsSequence().charAt(index) == ' ') {
          index--;
        }
        document.replaceString(index + 1, lineEndOffset, " // ignore: " + errorCode);
      }
    }

    private static void addCommentOnPrevLine(@NotNull final Document document, final int line, @NotNull final String errorCode) {
      final int lineStartOffset = document.getLineStartOffset(line);
      int offset = 0;
      while (document.getCharsSequence().charAt(lineStartOffset + offset) == ' ') {
        offset++;
      }
      document.insertString(lineStartOffset, StringUtil.repeat(" ", offset) + "// ignore: " + errorCode + "\n");
    }
  }
}