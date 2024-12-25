// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.coldFusion.model.psi.CfmlExpression;
import com.intellij.coldFusion.model.psi.CfmlFunctionCallExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CfmlJavaClassNamesCompletion extends CompletionProvider<CompletionParameters> {
  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                @NotNull ProcessingContext context,
                                final @NotNull CompletionResultSet result) {
    PsiElement element = parameters.getPosition();
    CfmlFunctionCallExpression parentOfType = PsiTreeUtil.getParentOfType(element, CfmlFunctionCallExpression.class);
    if (parentOfType != null && parentOfType.isCreateObject()) {
      CfmlExpression[] arguments = parentOfType.getArguments();
      if (arguments.length == 2 && "\"java\"".equalsIgnoreCase(arguments[0].getText())) {
        AllClassesGetter
          .processJavaClasses(parameters, result.getPrefixMatcher(), parameters.getInvocationCount() <= 1,
                              psiClass -> result.addElement(AllClassesGetter.createLookupItem(psiClass, AllClassesGetter.TRY_SHORTENING)));
      }
    }
  }
}
