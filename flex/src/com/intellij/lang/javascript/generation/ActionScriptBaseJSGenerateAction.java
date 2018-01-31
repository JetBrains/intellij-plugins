package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


public abstract class ActionScriptBaseJSGenerateAction extends BaseJSGenerateAction {

  @Override
  protected boolean isApplicableForMemberContainer(@NotNull PsiElement jsClass, PsiFile psiFile, @NotNull Editor editor) {
    return super.isApplicableForMemberContainer(jsClass, psiFile, editor) &&
           DialectDetector.isActionScript(jsClass);
  }
}
