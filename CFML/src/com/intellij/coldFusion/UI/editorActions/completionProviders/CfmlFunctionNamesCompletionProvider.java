// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.coldFusion.UI.CfmlLookUpItemUtil;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

class CfmlFunctionNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  public void addCompletions(@NotNull final CompletionParameters parameters,
                             @NotNull final ProcessingContext context,
                             @NotNull final CompletionResultSet result) {
    for (CfmlFunctionDescription s : CfmlLangInfo.getInstance(parameters.getPosition().getProject()).getFunctionParameters().values()) {
      addFunctionName(result.caseInsensitive(), s);
    }/*
        for (String s : CfmlPsiUtil.getFunctionsNamesDefined(parameters.getOriginalFile())) {
            addFunctionName(result, lookupElementFactory, s);
        }
        */
  }

  private static void addFunctionName(CompletionResultSet result, CfmlFunctionDescription s) {
    result.addElement(CfmlLookUpItemUtil.functionDescriptionToLookupItem(s)/*LookupElementBuilder.create(s).setInsertHandler(ParenthesesInsertHandler.WITH_PARAMETERS).setIcon(Icons.METHOD_ICON)*/);
  }
}
