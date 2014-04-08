package org.angularjs.findUsages;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  protected AngularJSReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull final Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    final JSNamedElementProxy directive = DirectiveUtil.getDirective(element);
    if (directive == null) return;

    queryParameters.getOptimizer().searchWord(directive.getName(), queryParameters.getEffectiveSearchScope(), true, directive);
  }
}
