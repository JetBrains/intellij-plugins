// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilCore;
import org.angular2.codeInsight.refs.Angular2RefUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2Pipe;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Angular2CompletionContributor extends CompletionContributor {
  private static final JSLookupPriority NG_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY;
  private static final JSLookupPriority NG_PRIVATE_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC;

  @Override
  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
    if (!getElementLanguage(parameters).is(Angular2Language.INSTANCE)) return;
    //TODO support Expansion form completions

    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());

    if (ref instanceof JSReferenceExpressionImpl && ((JSReferenceExpressionImpl)ref).getQualifier() == null) {
      final PsiElement parent = ((JSReferenceExpressionImpl)ref).getParent();
      if (addPipeVariants(result, parameters, (JSReferenceExpression)ref, parent)) {
        result.stopHere();
        return;
      }
      Angular2Processor.process(parameters.getPosition(), element -> {
        final String name = element.getName();
        if (name != null) {
          result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(
            element, name, Angular2RefUtil.isPrivateMember(element) ? NG_PRIVATE_VARIABLE_PRIORITY : NG_VARIABLE_PRIORITY, false, false));
        }
      });
      result.stopHere();
    }
  }

  private static boolean addPipeVariants(final CompletionResultSet result,
                                         CompletionParameters parameters,
                                         JSReferenceExpression ref,
                                         PsiElement parent) {
    if (Angular2Pipe.isPipeNameReference(ref)) {
      addResults(result, parameters, AngularIndexUtil.getAllKeys(AngularFilterIndex.KEY, parent.getProject()));
      return true;
    }
    return false;
  }

  static void addResults(final CompletionResultSet result, CompletionParameters parameters, final Collection<String> keys) {
    for (String controller : keys) {
      result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(null, controller, NG_VARIABLE_PRIORITY, false, false));
    }
  }

  static Language getElementLanguage(final CompletionParameters parameters) {
    return ReadAction.compute(() -> PsiUtilCore.getLanguageAtOffset(parameters.getPosition().getContainingFile(), parameters.getOffset()));
  }
}
