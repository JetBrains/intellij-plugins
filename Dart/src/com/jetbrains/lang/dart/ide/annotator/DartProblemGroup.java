// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressableProblemGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.fixes.DartQuickFix;
import com.jetbrains.lang.dart.psi.DartFile;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartProblemGroup implements SuppressableProblemGroup {
  private static final SuppressIntentionAction[] NO_ACTIONS = {};

  private final @Nullable String myErrorCode;

  private boolean myShowOwnSuppressActions;
  private @Nullable List<DartServerBasedIgnoreAction> myIgnoreFixes;

  public DartProblemGroup(@Nullable String errorCode) {
    myErrorCode = errorCode;
  }

  public void setShowOwnSuppressActions(boolean showOwnSuppressActions) {
    myShowOwnSuppressActions = showOwnSuppressActions;
  }

  public void addIgnoreFix(@NotNull SourceChange sourceChange) {
    if (myIgnoreFixes == null) {
      myIgnoreFixes = new ArrayList<>(2);
    }
    int index = myIgnoreFixes.size();
    myIgnoreFixes.add(new DartServerBasedIgnoreAction(index, sourceChange));
  }

  @Override
  public SuppressIntentionAction @NotNull [] getSuppressActions(@Nullable PsiElement element) {
    if (myIgnoreFixes != null) {
      return myIgnoreFixes.toArray(SuppressIntentionAction.EMPTY_ARRAY);
    }

    if (myShowOwnSuppressActions && myErrorCode != null && element != null && element.getContainingFile() instanceof DartFile) {
      return new SuppressIntentionAction[]{
        new DartSuppressAction(myErrorCode, false),
        new DartSuppressAction(myErrorCode, true)
      };
    }

    return NO_ACTIONS;
  }

  @Override
  public @Nullable String getProblemName() {
    return null;
  }

  private static class DartServerBasedIgnoreAction extends SuppressIntentionAction implements Comparable<IntentionAction> {
    private final int myIndex;
    private final @NotNull SourceChange mySourceChange;

    private DartServerBasedIgnoreAction(int index, @NotNull SourceChange sourceChange) {
      myIndex = index;
      mySourceChange = sourceChange;
    }

    @Override
    public @NotNull String getText() {
      @NlsSafe String message = mySourceChange.getMessage();
      return message;
    }

    @Override
    public @NotNull String getFamilyName() {
      return DartBundle.message("intention.family.name.suppress.errors.and.warnings.in.dart.code");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
      DartQuickFix.doInvoke(project, editor, element.getContainingFile(), mySourceChange, null);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
      return DartQuickFix.isAvailable(project, mySourceChange);
    }

    @Override
    public int compareTo(@NotNull IntentionAction o) {
      if (o instanceof DartServerBasedIgnoreAction) {
        return myIndex - ((DartServerBasedIgnoreAction)o).myIndex;
      }
      return 0;
    }
  }

  public static class DartSuppressAction extends SuppressIntentionAction implements Comparable<IntentionAction> {
    private static final String IGNORE_PREFIX = "ignore:";

    private final @NotNull String myErrorCode;
    private final boolean myEolComment;

    /**
     * @param eolComment {@code true} means that {@code //ignore} comment should be placed in the end of the current line, {@code false} -> on previous line
     */
    public DartSuppressAction(@NotNull String errorCode, boolean eolComment) {
      myErrorCode = errorCode;
      myEolComment = eolComment;

      if (eolComment) {
        // Suppress 'unused_local' using EOL comment
        setText(DartBundle.message("intention.text.suppress.0.using.eol.comment", errorCode));
      }
      else {
        // Suppress 'unused_local'
        setText(DartBundle.message("intention.text.suppress.0", errorCode));
      }
    }

    @Override
    public @NotNull String getFamilyName() {
      return DartBundle.message("intention.family.name.suppress.errors.and.warnings.in.dart.code");
    }

    @Override
    public int compareTo(final IntentionAction o) {
      if (o instanceof DartSuppressAction) {
        return ((DartSuppressAction)o).myEolComment ? -1 : 1;
      }
      return 0;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
      if (editor == null) return false;

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

    private static boolean hasEolIgnoreComment(@NotNull Document document, int line) {
      final CharSequence lineText =
        document.getCharsSequence().subSequence(document.getLineStartOffset(line), document.getLineEndOffset(line));
      if (!StringUtil.contains(lineText, IGNORE_PREFIX)) return false;

      int index = lineText.toString().lastIndexOf(IGNORE_PREFIX);
      while (index > 0) {
        char ch = lineText.charAt(--index);
        switch (ch) {
          case ' ' -> {
          }
          case '/' -> {
            return index >= 2 && lineText.charAt(index - 1) == '/' && lineText.charAt(index - 2) != '/';
          }
          default -> {
            return false;
          }
        }
      }
      return false;
    }

    private static boolean hasIgnoreCommentOnPrevLine(@NotNull Document document, int line) {
      if (line == 0) return false;

      final CharSequence prevLine =
        document.getCharsSequence().subSequence(document.getLineStartOffset(line - 1), document.getLineEndOffset(line - 1));

      int index = -1;
      while (++index < prevLine.length()) {
        char ch = prevLine.charAt(index);
        switch (ch) {
          case ' ' -> {
          }
          case '/' -> {
            if (prevLine.length() > index + 1 && prevLine.charAt(index + 1) == '/') {
              final String comment = prevLine.subSequence(index + 2, prevLine.length()).toString();
              if (StringUtil.trimLeading(comment, ' ').startsWith(IGNORE_PREFIX)) {
                return true;
              }
            }
            return false;
          }
          default -> {
            return false;
          }
        }
      }
      return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
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

    private static void appendErrorCode(@NotNull Document document, int line, @NotNull String errorCode) {
      final int lineEndOffset = document.getLineEndOffset(line);
      int index = lineEndOffset - 1;
      while (index >= 0 && document.getCharsSequence().charAt(index) == ' ') {
        index--;
      }
      document.replaceString(index + 1, lineEndOffset, ", " + errorCode);
    }

    private static void addEolComment(@NotNull Document document, int line, @NotNull String errorCode) {
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

    private static void addCommentOnPrevLine(@NotNull Document document, int line, @NotNull String errorCode) {
      final int lineStartOffset = document.getLineStartOffset(line);
      int offset = 0;
      while (document.getCharsSequence().charAt(lineStartOffset + offset) == ' ') {
        offset++;
      }
      document.insertString(lineStartOffset, StringUtil.repeat(" ", offset) + "// ignore: " + errorCode + "\n");
    }
  }
}