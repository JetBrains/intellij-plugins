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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.hint.ShowParameterInfoHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class CfmlMethodInsertHandler implements InsertHandler<LookupElement> {
  private static class CfmlMethodInsertHandlerHolder {
    private static final CfmlMethodInsertHandler instance = new CfmlMethodInsertHandler();
  }

  public static CfmlMethodInsertHandler getInstance() {
    return CfmlMethodInsertHandlerHolder.instance;
  }

  protected CfmlMethodInsertHandler() {

  }

  public void handleInsert(InsertionContext context, LookupElement lookupElement) {
    Editor editor = context.getEditor();
    if (CfmlPsiUtil.isFunctionDefinition(lookupElement.getObject())) {
      final CfmlFunction function = CfmlPsiUtil.getFunctionDefinition(lookupElement.getObject());
      if (!CfmlInsertHandlerUtil.isStringAtCaret(editor, "(")) {
        CfmlInsertHandlerUtil.insertStringAtCaret(editor, "()");
        if (function.getParameters().length > 0) {
          editor.getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
          showParameterInfo(editor);
        }
      }
      else {
        if (CfmlInsertHandlerUtil.isStringAtCaret(editor, "()")) {
          editor.getCaretModel().moveCaretRelatively(2, 0, false, false, true);
        }
        else {
          editor.getCaretModel().moveCaretRelatively(1, 0, false, false, true);
          showParameterInfo(editor);
        }
      }
    }
  }

  public static void showParameterInfo(Editor editor) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
    new ShowParameterInfoHandler().invoke(editor.getProject(), editor, psiFile);
  }
}
