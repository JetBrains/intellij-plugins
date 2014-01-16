package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
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
}
