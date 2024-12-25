// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.editorActions.TypedHandlerUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartTypeHandler extends TypedHandlerDelegate {
  private boolean myAfterTypeOrComponentName = false;
  private boolean myAfterDollarInStringInterpolation = false;

  private static final TokenSet INVALID_INSIDE_REFERENCE =
    TokenSet.create(DartTokenTypes.SEMICOLON, DartTokenTypes.LBRACE, DartTokenTypes.RBRACE);

  @Override
  public @NotNull Result beforeCharTyped(final char c, final @NotNull Project project, final @NotNull Editor editor, final @NotNull PsiFile file, final @NotNull FileType fileType) {
    myAfterTypeOrComponentName = false;
    myAfterDollarInStringInterpolation = false;

    if (fileType != DartFileType.INSTANCE) return Result.CONTINUE;

    if (c == '<' && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);
      myAfterTypeOrComponentName = isAfterTypeOrComponentName(file, editor.getCaretModel().getOffset());
      return Result.CONTINUE;
    }

    if (c == '>' && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
      if (TypedHandlerUtil.handleGenericGT(editor, DartTokenTypes.LT, DartTokenTypes.GT, INVALID_INSIDE_REFERENCE)) {
        return Result.STOP;
      }
      return Result.CONTINUE;
    }

    if (c == '{') {
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);

      final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset() - 1);

      if (CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET && isAfterDollarInStringInterpolation(element)) {
        myAfterDollarInStringInterpolation = true;
        return Result.CONTINUE;
      }

      PsiElement nextLeaf = element == null ? null : PsiTreeUtil.nextLeaf(element);
      if (nextLeaf instanceof PsiWhiteSpace) {
        nextLeaf = PsiTreeUtil.nextLeaf(nextLeaf);
      }

      if (PsiTreeUtil.getParentOfType(element, DartLazyParseableBlock.class, false) != null ||
          nextLeaf != null && nextLeaf.getText().equals("=>")) {
        // Use case 1: manually wrapping code with {} in 'if', 'while', etc (if (a) <caret>return;). Closing '}' will be auto-inserted on Enter.
        // Use case 2: manual transformation of arrow block to standard (foo()<caret> => 499;)
        EditorModificationUtil.insertStringAtCaret(editor, "{");
        return Result.STOP;
      }
    }

    return Result.CONTINUE;
  }

  @Override
  public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (c == '<' && myAfterTypeOrComponentName) {
      myAfterTypeOrComponentName = false;
      EditorModificationUtil.insertStringAtCaret(editor, ">", false, 0);
      return Result.STOP;
    }

    if (c == '{' && myAfterDollarInStringInterpolation) {
      myAfterDollarInStringInterpolation = false;
      // for global vars matching '}' is already added at this point by com.intellij.codeInsight.editorActions.TypedHandler.handleAfterLParen()
      // but if this fragment is inside another {} block standard TypedHandler doesn't work because it finds matching '}'
      if (editor.getDocument().getCharsSequence().charAt(editor.getCaretModel().getOffset()) != '}') {
        EditorModificationUtil.insertStringAtCaret(editor, "}", false, 0);
        return Result.STOP;
      }
    }

    if (c == ':' && autoIndentCase(editor, project, file)) {
      return Result.STOP;
    }

    return Result.CONTINUE;
  }

  private static boolean isAfterTypeOrComponentName(final @NotNull PsiFile file, final int offset) {
    final PsiElement element = file.findElementAt(offset - 1);
    final PsiElement previousElement = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(element, false);
    return PsiTreeUtil.getParentOfType(previousElement, DartType.class, DartComponentName.class) != null;
  }

  private static boolean isAfterDollarInStringInterpolation(final @Nullable PsiElement elementAtOffsetMinusOne) {
    return elementAtOffsetMinusOne != null &&
           elementAtOffsetMinusOne.getNode().getElementType() == DartTokenTypes.SHORT_TEMPLATE_ENTRY_START;
  }

  // similar to com.intellij.codeInsight.editorActions.JavaTypedHandler.autoIndentCase()
  private static boolean autoIndentCase(Editor editor, Project project, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
    PsiElement currElement = file.findElementAt(offset - 1);
    if (currElement != null) {
      PsiElement parent = currElement.getParent();
      if (parent instanceof DartSwitchCase || parent instanceof DartDefaultCase) {
        CodeStyleManager.getInstance(project).adjustLineIndent(file, parent.getTextRange().getStartOffset());
        return true;
      }
    }
    return false;
  }
}
