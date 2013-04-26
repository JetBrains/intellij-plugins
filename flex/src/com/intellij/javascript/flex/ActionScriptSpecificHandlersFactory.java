package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.javascript.flex.resolve.ActionScriptTypeEvaluator;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptSpecificHandlersFactory extends JSDialectSpecificHandlersFactory {
  @Override
  public JSTypeEvaluator newTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                             BaseJSSymbolProcessor.TypeProcessor processor,
                                             boolean ecma) {
    return new ActionScriptTypeEvaluator(context, processor, ecma);
  }

  @Override
  public JSCompletionKeywordsContributor newCompletionKeywordsContributor() {
    return new ActionScriptCompletionKeywordsContributor();
  }
}
