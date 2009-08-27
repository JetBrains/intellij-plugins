package com.intellij.tapestry.intellij.lang.completion;

import com.intellij.codeInsight.completion.CompletionVariant;
import com.intellij.codeInsight.completion.DefaultInsertHandler;
import com.intellij.codeInsight.completion.HtmlCompletionData;
import com.intellij.psi.filters.NotFilter;
import com.intellij.psi.filters.TextFilter;
import com.intellij.psi.filters.position.LeftNeighbour;

/**
 * Provides auto-completion for Tapestry components.
 */
public class TemplateCompletionData extends HtmlCompletionData {

  public TemplateCompletionData() {
    super();

    CompletionVariant completionVariant = new CompletionVariant(new NotFilter(new LeftNeighbour(new TextFilter("."))));
    completionVariant.includeScopeClass(com.intellij.psi.impl.source.tree.LeafPsiElement.class, true);
    completionVariant.addCompletion(new ParameterValueContextGetter());
    completionVariant.setInsertHandler(new DefaultInsertHandler());
    registerVariant(completionVariant);
  }
}
