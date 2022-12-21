// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

public final class Angular2TemplateScopesResolver {

  public static void resolve(final @NotNull PsiElement element, final @NotNull Processor<? super ResolveResult> processor) {
    PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    if (!checkLanguage(original)) {
      return;
    }
    boolean expressionIsInjected = original.getContainingFile().getLanguage().is(Angular2Language.INSTANCE);
    final PsiElement hostElement;
    if (expressionIsInjected) {
      //we are working within injection
      hostElement = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
      if (hostElement == null) {
        return;
      }
    }
    else {
      hostElement = null;
    }

    StreamEx.of(Angular2TemplateScopesProvider.EP_NAME.getExtensionList())
      .flatCollection(provider -> provider.getScopes(element, hostElement))
      .findFirst(s -> s.resolveAllScopesInHierarchy(processor));
  }

  public static boolean isImplicitReferenceExpression(JSReferenceExpression expression) {
    return ContainerUtil
      .or(Angular2TemplateScopesProvider.EP_NAME.getExtensionList(), provider -> provider.isImplicitReferenceExpression(expression));
  }

  private static boolean checkLanguage(@NotNull PsiElement element) {
    return element.getLanguage().is(Angular2Language.INSTANCE)
           || element.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
           || (element.getParent() != null
               && (element.getParent().getLanguage().is(Angular2Language.INSTANCE)
                   || element.getParent().getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
               ));
  }
}
