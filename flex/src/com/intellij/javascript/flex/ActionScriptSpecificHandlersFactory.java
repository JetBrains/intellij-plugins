package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.javascript.flex.resolve.ActionScriptTypeEvaluator;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ActionScriptSymbolVisitor;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
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
}
