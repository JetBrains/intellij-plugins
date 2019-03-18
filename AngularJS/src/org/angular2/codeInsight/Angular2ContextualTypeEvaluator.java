// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSExpectedTypeKind;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeEvaluationResult;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This type evaluator is based on {@link com.intellij.lang.javascript.ecmascript6.TypeScriptContextualTypeEvaluator} and
 * it's main purpose is to provide type of expression without literal types widening.
 */
public class Angular2ContextualTypeEvaluator extends Angular2TypeEvaluator {

  @NonNls private static final Key<ParameterizedCachedValue<JSTypeEvaluationResult, JSExpression>> CONTEXTUAL_KEY =
    Key.create("angular2.contextual.evaluator.expression.type");

  private static final ParameterizedCachedValueProvider<JSTypeEvaluationResult, JSExpression> CONTEXTUAL_PROVIDER =
    param -> {
      JSTypeEvaluationProcessor typeProcessor = new JSTypeFromResolveResultProcessor(new JSSimpleTypeProcessor());
      PsiFile targetFile = param.getContainingFile();
      Angular2ContextualTypeEvaluator evaluator =
        new Angular2ContextualTypeEvaluator(new JSEvaluateContext(targetFile), typeProcessor);

      evaluator.evaluateTypes(param, JSEvaluateContext.JSEvaluationPlace.RESOLVE_OVERLOADS);
      final JSTypeEvaluationResult type = typeProcessor.getResult();
      return CachedValueProvider.Result.create(type, PsiModificationTracker.MODIFICATION_COUNT);
    };

  @Nullable
  public static JSTypeEvaluationResult getContextualType(@NotNull JSExpression element) {
    if (!canProcessWithEvaluationGuard(element, JSEvaluateContext.JSEvaluationPlace.RESOLVE_OVERLOADS)) return null;

    return CachedValuesManager
      .getManager(element.getProject())
      .getParameterizedCachedValue(element, CONTEXTUAL_KEY, CONTEXTUAL_PROVIDER, false, element);
  }

  public Angular2ContextualTypeEvaluator(JSEvaluateContext context,
                                         JSTypeProcessor processor) {
    super(context, processor);
  }

  @NotNull
  @Override
  protected JSExpectedTypeKind getContextualExpectedTypeKind() {
    return JSExpectedTypeKind.CONTEXTUAL_WITH_OVERLOADS;
  }

  @Nullable
  @Override
  protected JSType getExpressionTypeInContext(@Nullable JSExpression toEvaluate) {
    if (toEvaluate == null) return null;
    JSTypeEvaluationResult type = getContextualType(toEvaluate);

    return type == null ? null : type.getType();
  }

  @Nullable
  @Override
  protected JSTypeEvaluationResult getTypeEvaluationResultInContext(@Nullable JSExpression toEvaluate) {
    return toEvaluate == null ? null : getContextualType(toEvaluate);
  }
}
