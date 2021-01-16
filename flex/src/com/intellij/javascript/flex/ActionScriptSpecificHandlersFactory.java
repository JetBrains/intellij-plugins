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
public class ActionScriptSpecificHandlersFactory extends JSDialectSpecificHandlersFactory {
  @NotNull
  @Override
  public JSTypeEvaluator newTypeEvaluator(@NotNull JSEvaluateContext context) {
    return new ActionScriptTypeEvaluator(context);
  }

  @Override
  public @NotNull JSTypeGuardEvaluator getTypeGuardEvaluator() {
    return ActionScriptTypeGuardEvaluator.INSTANCE;
  }

  @NotNull
  @Override
  public ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(JSReferenceExpressionImpl referenceExpression,
                                                                                                       boolean ignorePerformanceLimits) {
    return new ActionScriptReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits);
  }

  @NotNull
  @Override
  protected ExpectedTypeEvaluator newExpectedTypeEvaluator(PsiElement parent,
                                                           @NotNull JSExpectedTypeKind expectedTypeKind) {
    return new ActionScriptExpectedTypeEvaluator(parent, expectedTypeKind);
  }

  @Override
  @Nullable
  public JSLookupPriority getSpecificCompletionVariantPriority(final PsiElement element) {
    if (element instanceof JSQualifiedNamedElement) {
      final String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
      if (qName != null && "avmplus".equals(StringUtil.getPackageName(qName))) {
        return JSLookupPriority.NO_RELEVANT_SMARTNESS_PRIORITY;
      }
    }

    return null;
  }

  @NotNull
  @Override
  public JSClassResolver getClassResolver() {
    return ActionScriptClassResolver.getInstance();
  }

  @NotNull
  @Override
  public JSImportHandler getImportHandler() {
    return ActionScriptImportHandler.getInstance();
  }

  @NotNull
  @Override
  public JSTypeHelper getTypeHelper() {
    return ActionScriptTypeHelper.getInstance();
  }

  @NotNull
  @Override
  public JSCustomIndexer createCustomIndexer(@NotNull PsiFile file, @NotNull JSIndexContentBuilder indexBuilder) {
    return new ActionScriptCustomIndexer(file, indexBuilder);
  }

  @NotNull
  @Override
  public AccessibilityProcessingHandler createAccessibilityProcessingHandler(@Nullable PsiElement place, boolean skipNsResolving) {
    return new ActionScriptAccessibilityProcessingHandler(place, skipNsResolving);
  }

  @Override
  public <T extends ResultSink> QualifiedItemProcessor<T> createQualifiedItemProcessor(@NotNull T sink, @NotNull PsiElement place) {
    return new QualifiedItemProcessor<>(sink, place.getContainingFile());
  }

  @Override
  public @NotNull JSGenericTypesEvaluator getGenericTypeEvaluator() {
    return JSGenericTypesEvaluator.NO_OP;
  }
}
