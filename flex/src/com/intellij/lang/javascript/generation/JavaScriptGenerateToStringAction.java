package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 1:01:05 AM
 */
public class JavaScriptGenerateToStringAction extends BaseJSGenerateAction {

  protected BaseJSGenerateHandler getGenerateHandler() {
    return new JavaScriptGenerateToStringHandler();
  }

  protected boolean isApplicableForJsClass(final @NotNull JSClass jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return jsClass.findFunctionByNameAndKind("toString", JSFunction.FunctionKind.SIMPLE) == null;
  }
}