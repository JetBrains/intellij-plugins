// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.refactoring.introduceVariable;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.refactoring.introduce.BaseIntroduceSettings;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseInplaceIntroducer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ActionScriptIntroduceVariableHandler extends JSIntroduceVariableHandler {

  @Override
  protected @NotNull JSBaseInplaceIntroducer<BaseIntroduceSettings> createIntroducer(BaseIntroduceContext<BaseIntroduceSettings> context,
                                                                                     PsiElement scope,
                                                                                     Editor editor,
                                                                                     Project project,
                                                                                     JSExpression[] occurrences,
                                                                                     Runnable callback) {
    return new ActionScriptVariableInplaceIntroducer(project, editor, occurrences, this, context, callback);
  }
}
