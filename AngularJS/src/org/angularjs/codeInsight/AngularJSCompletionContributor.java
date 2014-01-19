package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.VariantsProcessor;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSFilterExpression;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSCompletionContributor extends CompletionContributor {
  private static final int NG_VARIABLE_PRIORITY = VariantsProcessor.LookupPriority.LOCAL_SCOPE_MAX_PRIORITY;

  @Override
  public void fillCompletionVariants(final CompletionParameters parameters, final CompletionResultSet result) {
    if (!getElementLanguage(parameters).is(AngularJSLanguage.INSTANCE)) return;
    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());

    if (ref instanceof JSReferenceExpressionImpl) {
      final PsiElement parent = ((JSReferenceExpressionImpl)ref).getParent();
      if (addFilterVariants(result, ref, parent)) return;
      AngularJSProcessor.process(parameters.getPosition(), new Consumer<JSNamedElement>() {
        @Override
        public void consume(JSNamedElement element) {
          result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(element, element.getName(), NG_VARIABLE_PRIORITY, false, false));
        }
      });
    }
  }

  private static boolean addFilterVariants(CompletionResultSet result, PsiReference ref, PsiElement parent) {
    if (AngularJSFilterExpression.isFilterNameRef(ref, parent)) {
      for (String filter : AngularIndexUtil.getAllKeys(AngularFilterIndex.INDEX_ID, parent.getProject())) {
        result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(null, filter, NG_VARIABLE_PRIORITY, false, false));
      }
      return true;
    }
    return false;
  }

  private static Language getElementLanguage(final CompletionParameters parameters) {
    final AccessToken l = ReadAction.start();
    try {
      return PsiUtilCore.getLanguageAtOffset(parameters.getPosition().getContainingFile(), parameters.getOffset());
    } finally {
      l.finish();
    }
  }
}
