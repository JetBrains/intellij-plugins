package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.javascript.flex.resolve.ActionScriptReferenceExpressionResolver;
import com.intellij.javascript.flex.resolve.ActionScriptTypeEvaluator;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ActionScriptExpectedTypeEvaluator;
import com.intellij.lang.javascript.flex.ActionScriptSymbolVisitor;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.ExpectedTypeEvaluator;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptSpecificHandlersFactory extends JSDialectSpecificHandlersFactory {
  @NotNull
  @Override
  public JSTypeEvaluator newTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                             BaseJSSymbolProcessor.TypeProcessor processor,
                                             boolean ecma) {
    return new ActionScriptTypeEvaluator(context, processor, ecma);
  }

  @NotNull
  @Override
  public JSCompletionKeywordsContributor newCompletionKeywordsContributor() {
    return new ActionScriptCompletionKeywordsContributor();
  }

  @NotNull
  @Override
  public JSSymbolVisitor newSymbolVisitor(JSNamespace namespace,
                                          JSSymbolUtil.JavaScriptSymbolProcessorEx symbolVisitor,
                                          PsiFile file) {
    return new ActionScriptSymbolVisitor(namespace, symbolVisitor, file);
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
        return VariantsProcessor.LookupPriority.NO_RELEVANT_SMARTNESS_PRIORITY;
      }
    }

    return -1;
  }

  @NotNull
  @Override
  public JSClassResolver newClassResolver() {
    return new ActionScriptClassResolver();
  }
}
