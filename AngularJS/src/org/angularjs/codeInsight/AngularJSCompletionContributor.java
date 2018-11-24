package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilCore;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSCompletionContributor extends CompletionContributor {
  private static final JSLookupPriority NG_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY;

  @Override
  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
    if (!getElementLanguage(parameters).is(AngularJSLanguage.INSTANCE)) return;
    if (AngularMessageFormatCompletion.messageFormatCompletion(parameters, result)) return;
    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());

    if (ref instanceof JSReferenceExpressionImpl && ((JSReferenceExpressionImpl)ref).getQualifier() == null) {
      final PsiElement parent = ((JSReferenceExpressionImpl)ref).getParent();
      if (addFilterVariants(result, parameters, ref, parent)) return;
      if (addControllerVariants(result, parameters, ref, parent)) return;
      AngularJSProcessor.process(parameters.getPosition(), element -> {
        final String name = element.getName();
        if (name != null) {
          result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(element, name, NG_VARIABLE_PRIORITY, false, false));
        }
      });
    }
  }

  private static boolean addControllerVariants(CompletionResultSet result, CompletionParameters parameters, PsiReference ref, PsiElement parent) {
    if (AngularJSAsExpression.isAsControllerRef(ref, parent)) {
      addResults(result, parameters, AngularIndexUtil.getAllKeys(AngularControllerIndex.KEY, parent.getProject()));
      return true;
    }
    return false;
  }

  private static boolean addFilterVariants(final CompletionResultSet result, CompletionParameters parameters, PsiReference ref, PsiElement parent) {
    if (AngularJSFilterExpression.isFilterNameRef(ref, parent)) {
      addResults(result, parameters, AngularIndexUtil.getAllKeys(AngularFilterIndex.KEY, parent.getProject()));
      return true;
    }
    return false;
  }

  static void addResults(final CompletionResultSet result, CompletionParameters parameters, final Collection<String> keys) {
    for (String controller : keys) {
      result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(null, controller, NG_VARIABLE_PRIORITY, false, false));
    }
    result.runRemainingContributors(parameters, result1 -> {
      final String string = result1.getLookupElement().getLookupString();
      if (!keys.contains(string)) {
        result.passResult(result1);
      }
    });
  }

  static Language getElementLanguage(final CompletionParameters parameters) {
    final AccessToken l = ReadAction.start();
    try {
      return PsiUtilCore.getLanguageAtOffset(parameters.getPosition().getContainingFile(), parameters.getOffset());
    } finally {
      l.finish();
    }
  }
}
