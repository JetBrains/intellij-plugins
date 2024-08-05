// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public class JavaScriptGenerateToStringAction extends ActionScriptBaseJSGenerateAction {

  @Override
  protected @NotNull BaseJSGenerateHandler getGenerateHandler() {
    return new ActionScriptGenerateToStringHandler();
  }

  @Override
  protected boolean isApplicableForMemberContainer(final @NotNull PsiElement jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return jsClass instanceof JSClass && super.isApplicableForMemberContainer(jsClass, psiFile, editor)
           && ((JSClass)jsClass).findFunctionByNameAndKind("toString", JSFunction.FunctionKind.SIMPLE) == null;
  }
}