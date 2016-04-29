package org.jetbrains.plugins.cucumber.codeinsight;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

/**
 * @author yole
 */
public class CucumberEnterHandler extends EnterHandlerDelegateAdapter {
  public static final String PYSTRING_QUOTES = "\"\"\"";

  public Result preprocessEnter(@NotNull PsiFile file,
                                @NotNull Editor editor,
                                @NotNull Ref<Integer> caretOffset,
                                @NotNull Ref<Integer> caretAdvance,
                                @NotNull DataContext dataContext,
                                EditorActionHandler originalHandler) {
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
        EditorModificationUtil.insertStringAtCaret(editor, "\n" + space + "\n" + space + PYSTRING_QUOTES);
        editor.getCaretModel().moveCaretRelatively(-3, -1, false, false, true);
        return Result.Stop;
      }
    }
    return Result.Continue;
  }
}
