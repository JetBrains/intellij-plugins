// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.findUsages;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.xml.util.HtmlUtil;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

public class AngularJSReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  protected AngularJSReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters,
                           final @NotNull Processor<? super PsiReference> consumer) {
    final JSImplicitElement directive;
    final PsiElement element = queryParameters.getElementToSearch();
    if ((directive = DirectiveUtil.getDirective(element)) != null && directive.getName() != null) {
      for (String attrName : DirectiveUtil.getAttributeNameVariations(directive.getName())) {
        queryParameters.getOptimizer().searchWord(attrName, queryParameters.getEffectiveSearchScope(), false, directive);
        queryParameters.getOptimizer().searchWord("x-" + attrName, queryParameters.getEffectiveSearchScope(), false, directive);
        queryParameters.getOptimizer()
          .searchWord(HtmlUtil.HTML5_DATA_ATTR_PREFIX + attrName, queryParameters.getEffectiveSearchScope(), false, directive);
      }
    }
  }
}
