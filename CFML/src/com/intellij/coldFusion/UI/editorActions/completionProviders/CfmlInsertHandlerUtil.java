/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
