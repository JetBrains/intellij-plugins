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
package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

/**
 * Created by Lera Nikolaenko
 * Date: 16.10.2008
 */
public class CfmlBackspaceHandler extends BackspaceHandlerDelegate {

  public void beforeCharDeleted(char c, PsiFile file, Editor editor) {
    if (file.getLanguage() != CfmlLanguage.INSTANCE) return;
    if (c == '#') {
      if (CfmlEditorUtil.countSharpsBalance(editor) == 0) {
        final int offset = editor.getCaretModel().getOffset();
        final Document doc = editor.getDocument();
        char charAtOffset = DocumentUtils.getCharAt(doc, offset);
        if (charAtOffset == '#') {
          doc.deleteString(offset, offset + 1);
        }
      }
    }
    else if (c == '{' && file.findElementAt(editor.getCaretModel().getOffset()) == CfscriptTokenTypes.L_CURLYBRACKET) {
      final int offset = editor.getCaretModel().getOffset();
      final Document doc = editor.getDocument();
      char charAtOffset = DocumentUtils.getCharAt(doc, offset);
      if (charAtOffset == '}') {
        doc.deleteString(offset, offset + 1);
      }
    }
  }

  public boolean charDeleted(char c, PsiFile file, Editor editor) {
    if (file.getLanguage() != CfmlLanguage.INSTANCE) return false;
    return false;
  }
}
