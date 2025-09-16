// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.codeinsight;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtilEx;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;


@NotNullByDefault
public final class CucumberEnterHandler implements EnterHandlerDelegate {
  public static final String PYSTRING_QUOTES = "\"\"\"";

  @Override
  public Result preprocessEnter(PsiFile file,
                                Editor editor,
                                Ref<Integer> caretOffset,
                                Ref<Integer> caretAdvance,
                                DataContext dataContext,
                                @Nullable EditorActionHandler originalHandler) {
    if (!(file instanceof GherkinFile)) {
      return Result.Continue;
    }
    int caretOffsetValue = caretOffset.get().intValue();
    if (caretOffsetValue < 3) {
      return Result.Continue;
    }
    final Document document = editor.getDocument();
    final String docText = document.getText();
    PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
    final PsiElement probableQuotes = file.findElementAt(caretOffsetValue - 1);
    if (probableQuotes != null && probableQuotes.getNode().getElementType() == GherkinTokenTypes.PYSTRING) {
      final PsiElement probablePyStringText =
        document.getTextLength() == PYSTRING_QUOTES.length() ? null : file.findElementAt(caretOffsetValue - 1 - PYSTRING_QUOTES.length());
      if (probablePyStringText == null || probablePyStringText.getNode().getElementType() != GherkinTokenTypes.PYSTRING_TEXT) {
        int line = document.getLineNumber(caretOffsetValue);
        int lineStart = document.getLineStartOffset(line);
        int textStart = CharArrayUtil.shiftForward(docText, lineStart, " \t");
        final String space = docText.subSequence(lineStart, textStart).toString();

        // insert closing triple quote
        EditorModificationUtilEx.insertStringAtCaret(editor, "\n" + space + "\n" + space + PYSTRING_QUOTES);
        editor.getCaretModel().moveCaretRelatively(-3, -1, false, false, true);
        return Result.Stop;
      }
    }
    return Result.Continue;
  }
}
