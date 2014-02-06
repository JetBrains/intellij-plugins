package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSSpecificHandlersFactory extends JavaScriptSpecificHandlersFactory {
  @NotNull
  @Override
  public JSResolveUtil.Resolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(JSReferenceExpressionImpl referenceExpression,
                                                                                             PsiFile containingFile) {
    return new AngularJSReferenceExpressionResolver(referenceExpression, containingFile);
  }

  @NotNull
  @Override
  public JSTypeEvaluator newTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                          BaseJSSymbolProcessor.TypeProcessor processor,
                                          boolean ecma) {
    return new AngularJSTypeEvaluator(context, processor, ecma);
  }
}
