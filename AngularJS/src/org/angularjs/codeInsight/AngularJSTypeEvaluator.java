package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTypeEvaluator extends JSTypeEvaluator {
  public AngularJSTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                BaseJSSymbolProcessor.TypeProcessor processor, boolean ecma) {
    super(context, processor, ecma);
  }



}
