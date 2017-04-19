package org.angularjs.codeInsight;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.QualifiedItemProcessor;
import com.intellij.lang.javascript.psi.resolve.ResultSink;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.angularjs.index.AngularJS2IndexingHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSSpecificHandlersFactory extends JavaScriptSpecificHandlersFactory {
  @NotNull
  @Override
  public ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(
    JSReferenceExpressionImpl referenceExpression, boolean ignorePerformanceLimits) {
    return new AngularJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits);
  }

  @Override
  public <T extends ResultSink> QualifiedItemProcessor<T> createQualifiedItemProcessor(@NotNull T sink, @NotNull PsiElement place) {
    JSClass clazz = AngularJS2IndexingHandler.findDirectiveClass(place);
    if (clazz != null && DialectDetector.isTypeScript(clazz)) {
      return new TypeScriptQualifiedItemProcessor<>(sink, place.getContainingFile());
    }

    return super.createQualifiedItemProcessor(sink, place);
  }
}