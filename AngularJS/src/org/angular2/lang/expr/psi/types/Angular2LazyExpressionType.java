// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.types;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSLazyExpressionType;
import com.intellij.lang.typescript.resolve.TypeScriptGenericTypesEvaluator;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2ContextualTypeEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2LazyExpressionType extends JSLazyExpressionType {

  @NotNull
  public static List<JSType> mapAsArguments(@NotNull JSExpression[] arguments, boolean contextual) {
    return ContainerUtil.map(arguments, el -> new Angular2LazyExpressionType(el, contextual));
  }

  public Angular2LazyExpressionType(@NotNull JSExpression expression,
                                    boolean contextual) {
    super(expression, contextual);
  }

  @NotNull
  @Override
  protected JSType evaluateExpression() {
    if (myContextual) {
      return TypeScriptGenericTypesEvaluator.getParameterExpressionType(
        Angular2ContextualTypeEvaluator.getContextualType(myExpression));
    }
    return super.evaluateExpression();
  }
}
