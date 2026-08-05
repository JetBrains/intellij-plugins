// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.LegacyCompletionContributor;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.actionscript.ActionScriptTextReferenceResolver;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.completion.JSTextReferenceCompletionProvider;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptTextReference;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public final class FlexTextReferenceCompletionProvider extends CompletionContributor {
  @Override
  public void fillCompletionVariants(final @NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    LegacyCompletionContributor.processReferences(parameters, result, (reference, set) -> {
      if (reference instanceof ActionScriptTextReference textReference) {
        addVariants(textReference, parameters, set);
      }
    });
  }

  private static void addVariants(@NotNull ActionScriptTextReference reference,
                                  @NotNull CompletionParameters completionParameters,
                                  @NotNull CompletionResultSet resultSet) {
    final PsiElement element = reference.getElement();
    final PsiFile containingFile = element.getContainingFile();

    CompletionResultSink resultSink = new CompletionResultSink(containingFile, resultSet.getPrefixMatcher());

    String[] baseClassFqns = reference.getBaseClassFqns();
    if (baseClassFqns.length > 0) {
      setCompletionFilter(resultSink, baseClassFqns, containingFile);
    }

    ActionScriptTextReferenceResolver.processToSink(reference, containingFile, resultSink);

    List<LookupElement> localResults = resultSink.getResultsAsObjects();
    resultSet.addAllElements(localResults);
    forceQualifyIfNeeded(reference, localResults);
    if (element instanceof JSLiteralExpression) {
      List<LookupElement> smartResults = new ArrayList<>();
      JSTextReferenceCompletionProvider.calcDefaultVariants(reference, containingFile, localResults, smartResults, completionParameters,
                                                            resultSet);
    }
  }

  private static void setCompletionFilter(CompletionResultSink resultSink, String @NotNull [] baseClassFqns, PsiFile containingFile) {
    // Use filter only for completion. For highlighting appropriate error is added by JSAnnotatingVisitor
    final Module module = ModuleUtilCore.findModuleForPsiElement(containingFile);
    if (module != null) {
      final GlobalSearchScope scope = GlobalSearchScope.moduleWithLibrariesScope(module);

      final Condition<JSClass> filter;
      if (baseClassFqns.length == 1) {
        filter = new PublicInheritorFilter(module.getProject(), baseClassFqns[0], scope, false);
      }
      else {
        final List<Condition<JSClass>> conditions =
          ContainerUtil.map(baseClassFqns, fqn -> new PublicInheritorFilter(module.getProject(), fqn, scope, false));

        filter = aClass -> {
          for (Condition<JSClass> condition : conditions) {
            if (condition.value(aClass)) {
              return true;
            }
          }
          return false;
        };
      }

      resultSink.acceptOnlyClasses(filter);
    }
  }

  private static void forceQualifyIfNeeded(ActionScriptTextReference reference, List<LookupElement> results) {
    boolean isOnlyFqns = reference != null && reference.isOnlyFqns();

    if (isOnlyFqns && reference.isPrimary()) {
      for (LookupElement result : results) {
        JSLookupUtilImpl.setForceQualify(result);
      }
    }
  }
}