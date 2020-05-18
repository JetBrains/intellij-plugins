package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.angularjs.codeInsight.refs.AngularJSDIReferencesProvider;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;

public class AngularJSDICompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(final @NotNull CompletionParameters parameters, final @NotNull CompletionResultSet result) {
    if (AngularJSCompletionContributor.getElementLanguage(parameters).isKindOf(JavascriptLanguage.INSTANCE)) {
      final PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      addDependencyInjectionVariants(result, parameters, ref, parameters.getPosition());
    }
  }

  private static void addDependencyInjectionVariants(CompletionResultSet result,
                                                     CompletionParameters parameters,
                                                     PsiReference ref,
                                                     PsiElement parent) {
    if (ref instanceof AngularJSDIReferencesProvider.AngularJSDIReference) {
      AngularJSCompletionContributor
        .addResults(result, parameters, AngularIndexUtil.getAllKeys(AngularSymbolIndex.KEY, parent.getProject()));
    }
  }
}
