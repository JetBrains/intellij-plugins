package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.psi.PsiDocumentManager;

public class CfmlInsertHandlerUtil {
  private CfmlInsertHandlerUtil() {
  }

  public static boolean isStringAtCaret(Editor editor, String string) {
    final int startOffset = editor.getCaretModel().getOffset();
    final String fileText = editor.getDocument().getText();
    if (fileText.length() < startOffset + string.length()) return false;
    return fileText.substring(startOffset, startOffset + string.length()).equals(string);
  }

  public static void insertStringAtCaret(Editor editor, String string) {
    EditorModificationUtil.insertStringAtCaret(editor, string);
    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
  }

}
