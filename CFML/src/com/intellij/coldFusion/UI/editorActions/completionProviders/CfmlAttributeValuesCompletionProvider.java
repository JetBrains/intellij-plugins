// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlComponentReference;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

class CfmlAttributeValuesCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  public void addCompletions(final @NotNull CompletionParameters parameters,
                             final @NotNull ProcessingContext context,
                             final @NotNull CompletionResultSet result) {
    PsiElement element = parameters.getPosition();
    while (element != null && !(element instanceof CfmlAttributeImpl)) {
      element = element.getParent();
    }
    if (element == null) {
      return;
    }
    CfmlAttributeImpl attribute = (CfmlAttributeImpl)element;
    String attributeName = attribute.getFirstChild().getText();
    while (element != null && !(element instanceof CfmlTag)) {
      element = element.getParent();
    }
    if (element == null) {
      return;
    }
    CfmlTag tag = (CfmlTag)element;
    String tagName = tag.getTagName();

    String[] attributeValue = CfmlUtil.getAttributeValues(tagName, attributeName, parameters.getPosition().getProject());

    if ("type".equalsIgnoreCase(attributeName) && "cfargument".equalsIgnoreCase(tagName) ||
        "returntype".equalsIgnoreCase(attributeName) && "cffunction".equalsIgnoreCase(tagName)
       ) {
      Object[] objects =
        CfmlComponentReference.buildVariants(attribute.getPureAttributeValue(), element.getContainingFile(), element.getProject(), null, true);
      for(Object o:objects) {
        result.addElement((LookupElement)o);
      }
    }
    for (String s : attributeValue) {
      result.addElement(LookupElementBuilder.create(s).withCaseSensitivity(false));
    }
  }
}
