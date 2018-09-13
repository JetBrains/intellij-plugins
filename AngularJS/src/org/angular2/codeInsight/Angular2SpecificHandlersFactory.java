// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.findUsages.JSDialectSpecificReadWriteAccessDetector;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.QualifiedItemProcessor;
import com.intellij.lang.javascript.psi.resolve.ResultSink;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver;
import org.angular2.findUsages.Angular2ReadWriteAccessDetector;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;

public class Angular2SpecificHandlersFactory extends JavaScriptSpecificHandlersFactory {

  @NotNull
  @Override
  public ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(
    JSReferenceExpressionImpl referenceExpression, boolean ignorePerformanceLimits) {
    return new Angular2ReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits);
  }

  @Override
  public <T extends ResultSink> QualifiedItemProcessor<T> createQualifiedItemProcessor(@NotNull T sink, @NotNull PsiElement place) {
    JSClass clazz = Angular2IndexingHandler.findDirectiveClass(place);
    if (clazz != null && DialectDetector.isTypeScript(clazz)) {
      return new TypeScriptQualifiedItemProcessor<>(sink, place.getContainingFile());
    }

    return super.createQualifiedItemProcessor(sink, place);
  }

  @NotNull
  @Override
  public JSDialectSpecificReadWriteAccessDetector getReadWriteAccessDetector() {
    return Angular2ReadWriteAccessDetector.INSTANCE;
  }
}