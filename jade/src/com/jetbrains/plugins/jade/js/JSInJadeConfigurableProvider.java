// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

public final class JSInJadeConfigurableProvider extends JSInheritedLanguagesConfigurableProvider {
  @Override
  public boolean isBadExpressionStatement(@NotNull PsiElement element) {
    return !isInEmbeddedExpression(element);
  }

  @Override
  public boolean isNeedToBeTerminated(@NotNull PsiElement element) {
    return !isOneLiner();
  }

  private static boolean isOneLiner() {
    return true;
  }

  private static boolean isInEmbeddedExpression(PsiElement element) {
    JSInJadeEmbeddedContentImpl jsInJadeEmbeddedContent = PsiTreeUtil.getParentOfType(element, JSInJadeEmbeddedContentImpl.class);
    if (jsInJadeEmbeddedContent == null) {
      return false;
    }

    return JadeTokenTypes.JS_EXPR.equals(jsInJadeEmbeddedContent.getNode().getElementType());
  }
}
