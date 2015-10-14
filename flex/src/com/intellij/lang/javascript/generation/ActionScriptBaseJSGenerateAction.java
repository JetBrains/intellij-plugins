package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


public abstract class ActionScriptBaseJSGenerateAction extends BaseJSGenerateAction {

  @Override
  protected boolean isApplicableForJsClass(@NotNull JSClass jsClass, PsiFile psiFile, @NotNull Editor editor) {
    return super.isApplicableForJsClass(jsClass, psiFile, editor) &&
           JavaScriptSupportLoader.ECMA_SCRIPT_L4 == DialectDetector.languageOfElement(jsClass);
  }
}
