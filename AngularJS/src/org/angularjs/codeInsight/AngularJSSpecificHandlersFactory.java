package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSSpecificHandlersFactory extends JavaScriptSpecificHandlersFactory {
  @Override
  public @NotNull ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(
    JSReferenceExpressionImpl referenceExpression, boolean ignorePerformanceLimits) {
    return new AngularJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits);
  }
}