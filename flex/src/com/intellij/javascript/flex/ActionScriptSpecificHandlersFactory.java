package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.javascript.flex.index.ActionScriptCustomIndexer;
import com.intellij.javascript.flex.resolve.*;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ActionScriptExpectedTypeEvaluator;
import com.intellij.lang.javascript.index.JSCustomIndexer;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.ExpectedTypeEvaluator;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptSpecificHandlersFactory extends JSDialectSpecificHandlersFactory {
  @NotNull
  @Override
  public JSTypeEvaluator newTypeEvaluator(JSEvaluateContext context, JSTypeProcessor processor, boolean ecma) {
    return new ActionScriptTypeEvaluator(context, processor, ecma);
  }

  @NotNull
  @Override
  public JSResolveUtil.Resolver<JSReferenceExpressionImpl> createReferenceExpressionResolver(JSReferenceExpressionImpl referenceExpression,
                                                                                             PsiFile containingFile) {
    return new ActionScriptReferenceExpressionResolver(referenceExpression, containingFile);
  }

  @NotNull
  @Override
  public ExpectedTypeEvaluator newExpectedTypeEvaluator(JSExpression parent) {
    return new ActionScriptExpectedTypeEvaluator(parent);
  }

  public int getSpecificCompletionVariantPriority(final PsiElement element) {
    if (element instanceof JSQualifiedNamedElement) {
      final String qName = ((JSQualifiedNamedElement)element).getQualifiedName();
      if (qName != null && "avmplus".equals(StringUtil.getPackageName(qName))) {
        return JSLookupPriority.NO_RELEVANT_SMARTNESS_PRIORITY;
      }
    }

    return -1;
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
}
