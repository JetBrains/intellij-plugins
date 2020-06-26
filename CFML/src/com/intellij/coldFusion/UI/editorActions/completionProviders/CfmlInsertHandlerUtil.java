// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.psi.PsiDocumentManager;

public final class CfmlInsertHandlerUtil {
  private CfmlInsertHandlerUtil() {
  }

  public static boolean isStringAtCaret(Editor editor, String string) {
    final int startOffset = editor.getCaretModel().getOffset();
    final String fileText = editor.getDocument().getText();
    return fileText.startsWith(string, startOffset);
  }

  public static void insertStringAtCaret(Editor editor, String string) {
    EditorModificationUtil.insertStringAtCaret(editor, string);
    PsiDocumentManager.getInstance(editor.getProject()).commitDocument(editor.getDocument());
  }
}
