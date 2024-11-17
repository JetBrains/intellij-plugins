// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.javascript.flex.index.ActionScriptCustomIndexer;
import com.intellij.javascript.flex.resolve.*;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ActionScriptExpectedTypeEvaluator;
import com.intellij.lang.javascript.index.JSCustomIndexer;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.ExpectedTypeEvaluator;
import com.intellij.lang.javascript.psi.JSExpectedTypeKind;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptSpecificHandlersFactory extends JSDialectSpecificHandlersFactory {
  @Override
  public @NotNull JSTypeEvaluator newTypeEvaluator(@NotNull JSEvaluateContext context) {
    return new ActionScriptTypeEvaluator(context);
  }

  @Override
  public @NotNull JSTypeGuardEvaluator getTypeGuardEvaluator() {
    return ActionScriptTypeGuardEvaluator.INSTANCE;
  }

  @Override
  public @NotNull ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(@NotNull JSReferenceExpressionImpl referenceExpression,
                                                                                                                boolean ignorePerformanceLimits) {
    return new ActionScriptReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits);
  }

  @Override
  protected @NotNull ExpectedTypeEvaluator newExpectedTypeEvaluator(@NotNull PsiElement parent,
                                                                    @NotNull JSExpectedTypeKind expectedTypeKind) {
    return new ActionScriptExpectedTypeEvaluator(parent, expectedTypeKind);
  }

  @Override
  public @Nullable JSLookupPriority getSpecificCompletionVariantPriority(final @NotNull PsiElement element) {
    if (element instanceof JSQualifiedNamedElement) {
      final String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
      if (qName != null && "avmplus".equals(StringUtil.getPackageName(qName))) {
        return JSLookupPriority.NO_RELEVANT_SMARTNESS_PRIORITY;
      }
    }

    return null;
  }

  @Override
  public @NotNull JSClassResolver getClassResolver() {
    return ActionScriptClassResolver.getInstance();
  }

  @Override
  public @NotNull JSImportHandler getImportHandler() {
    return ActionScriptImportHandler.getInstance();
  }

  @Override
  public @NotNull JSTypeHelper getTypeHelper() {
    return ActionScriptTypeHelper.getInstance();
  }

  @Override
  public @NotNull JSCustomIndexer createCustomIndexer(@NotNull PsiFile file, @NotNull JSIndexContentBuilder indexBuilder) {
    return new ActionScriptCustomIndexer(file, indexBuilder);
  }

  @Override
  public @NotNull AccessibilityProcessingHandler createAccessibilityProcessingHandler(@Nullable PsiElement place, boolean skipNsResolving) {
    return new ActionScriptAccessibilityProcessingHandler(place, skipNsResolving);
  }

  @Override
  public <T extends ResultSink> QualifiedItemProcessor<T> createQualifiedItemProcessor(@NotNull T sink, @NotNull PsiElement place) {
    return new ActionScriptQualifiedItemProcessor<>(sink, place.getContainingFile());
  }

  @Override
  public @NotNull JSGenericTypesEvaluator getGenericTypeEvaluator() {
    return JSGenericTypesEvaluator.NO_OP;
  }
}
