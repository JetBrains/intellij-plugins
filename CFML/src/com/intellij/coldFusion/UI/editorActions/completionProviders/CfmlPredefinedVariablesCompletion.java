// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.coldFusion.model.psi.CfmlReference;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CfmlPredefinedVariablesCompletion extends CompletionProvider<CompletionParameters> {
  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                @NotNull ProcessingContext context,
                                @NotNull CompletionResultSet result) {
    Map<String, Integer> myPredefinedVariables = CfmlLangInfo.getInstance(parameters.getPosition().getProject()).getPredefinedVariables();
    String tagName = getNameForCfmlTag(parameters);
    for (String s : myPredefinedVariables.keySet()) {
      if (!s.contains("[x]")) {
        if (tagName != null && s.startsWith(tagName)) {
          result.addElement(LookupElementBuilder.create(s.substring(s.indexOf(".") + 1)).withCaseSensitivity(false));
        }
        else if (tagName == null &&
                 !result.getPrefixMatcher().getPrefix().isEmpty() &&
                 parameters.getPosition().getParent() instanceof CfmlReference) {
          result.addElement(LookupElementBuilder.create(s).withCaseSensitivity(false));
        }
      }
    }
  }

  private static @Nullable String getNameForCfmlTag(@NotNull CompletionParameters pars) {
    PsiElement elem = pars.getPosition().getParent().getFirstChild();
    if (elem instanceof CfmlReferenceExpression) {
      PsiElement resElem = ((CfmlReferenceExpression)elem).resolve();
      if (resElem != null) {
        PsiElement parent = resElem.getParent();
        if (!(parent instanceof CfmlTagImpl)) {
          return null;
        }
        String name = ((CfmlTagImpl)parent).getTagName();
        return name.startsWith("cf") ? name.substring(2) : name;
      }
    }
    return null;
  }
}
