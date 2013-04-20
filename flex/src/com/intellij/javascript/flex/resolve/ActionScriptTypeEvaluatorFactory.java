package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluatorFactory;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeEvaluatorFactory extends JSTypeEvaluatorFactory {
  @Override
  protected JSTypeEvaluator newInstance(BaseJSSymbolProcessor.EvaluateContext context,
                                        BaseJSSymbolProcessor.TypeProcessor processor,
                                        boolean ecma) {
    return new ActionScriptTypeEvaluator(context, processor, ecma);
  }
}
