// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlBackspaceHandler extends BackspaceHandlerDelegate {

  @Override
  public void beforeCharDeleted(char c, PsiFile file, Editor editor) {
    if (file.getLanguage() != CfmlLanguage.INSTANCE) return;
    if (c == '#') {
      if (CfmlTypedHandler.Companion.getOurEnableDoublePoundInsertion() && CfmlEditorUtil.countSharpsBalance(editor) == 0) {
        final int offset = editor.getCaretModel().getOffset();
        final Document doc = editor.getDocument();
        char charAtOffset = DocumentUtils.getCharAt(doc, offset);
        if (charAtOffset == '#') {
          doc.deleteString(offset, offset + 1);
        }
      }
    }
    else if (c == '{') {
      PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
      if (element != null && element.getNode().getElementType() == CfscriptTokenTypes.L_CURLYBRACKET) {
        final int offset = editor.getCaretModel().getOffset();
        final Document doc = editor.getDocument();
        char charAtOffset = DocumentUtils.getCharAt(doc, offset);
        if (charAtOffset == '}') {
          doc.deleteString(offset, offset + 1);
        }
      }
    }
  }

  @Override
  public boolean charDeleted(char c, PsiFile file, Editor editor) {
    return false;
  }
}
