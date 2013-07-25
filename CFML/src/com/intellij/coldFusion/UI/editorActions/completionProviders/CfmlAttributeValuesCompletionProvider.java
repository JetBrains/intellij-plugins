package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: vnikolaenko
 * Date: 17.03.2009
 */
class CfmlAttributeValuesCompletionProvider extends CompletionProvider<CompletionParameters> {
  public void addCompletions(@NotNull final CompletionParameters parameters,
                             final ProcessingContext context,
                             @NotNull final CompletionResultSet result) {
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
    if (attributeValue == null) {
      return;
    }
    for (String s : attributeValue) {
      result.addElement(LookupElementBuilder.create(s).withCaseSensitivity(false));
    }
  }
}
