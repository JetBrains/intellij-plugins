package com.intellij.tapestry.intellij.editorActions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.tapestry.intellij.util.TapestryPropertyNamingUtil;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;

/**
 * @author Alexey Chmutov
 */
public class TapestryMethodReferenceSearcher implements QueryExecutor<PsiReference, MethodReferencesSearch.SearchParameters> {
  public boolean execute(final MethodReferencesSearch.SearchParameters parameters, final Processor<PsiReference> consumer) {
    final PsiMethod method = parameters.getMethod();
    final Ref<String> propNameRef = Ref.create(null);
    final Ref<Project> project = Ref.create(null);
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        if (!method.isValid()) return;
        project.set(method.getProject());
        propNameRef.set(TapestryPropertyNamingUtil.getPropertyNameFromAccessor(method));
      }
    });
    if (project.isNull()) return true;
    SearchScope searchScope = parameters.getScope();
    if (searchScope instanceof GlobalSearchScope) {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)searchScope, TmlFileType.INSTANCE);
    }

    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      public boolean execute(PsiElement element, int offsetInElement) {
        ProgressManager.checkCanceled();
        final PsiReference[] refs = element.getReferences();
        for (PsiReference ref : refs) {
          if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && ref.isReferenceTo(method)) {
            return consumer.process(ref);
          }
        }
        return true;
      }
    };
    final PsiSearchHelper helper = PsiManager.getInstance(project.get()).getSearchHelper();
    final String propName = propNameRef.get();
    final boolean propertyFound =
      propName != null && helper.processElementsWithWord(processor, searchScope, propName, UsageSearchContext.IN_FOREIGN_LANGUAGES, false);
    final boolean methodFound =
      helper.processElementsWithWord(processor, searchScope, method.getName(), UsageSearchContext.IN_FOREIGN_LANGUAGES, false);
    return propertyFound || methodFound;
  }
}
