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
      } else {
        if (CfmlInsertHandlerUtil.isStringAtCaret(editor, "()")) {
          editor.getCaretModel().moveCaretRelatively(2, 0, false, false, true);
        } else {
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
