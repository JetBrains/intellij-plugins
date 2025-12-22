// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.JSNamespace;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;


public final class ActionScriptContextLevel {

  public final @NotNull JSNamespace myNamespace;

  /**
   * 0 means top-level
   */
  public final int myRelativeLevel;

  public ActionScriptContextLevel(@NotNull JSNamespace namespace, int relativeLevel) {
    myNamespace = namespace;
    myRelativeLevel = relativeLevel;
  }

  @Override
  public String toString() {
    return myNamespace + " ," + myRelativeLevel;
  }

  public boolean isElementInScope(@NotNull JSPsiElementBase element, @NotNull JSNamespace elementJSNamespace) {
    PsiElement localScope = myNamespace.getLocalScope();
    if (localScope != null) {
      return isFromScope(element, localScope);
    }

    if (elementJSNamespace.isLocal() && !myNamespace.isLocal()) {
      return false;
    }

    return true;
  }

  private static boolean isFromScope(@NotNull JSPsiElementBase element, @NotNull PsiElement localScope) {
    PsiElement scope = element;
    while (scope != null) {
      if (localScope.equals(scope)) return true;
      scope = scope.getContext();
    }
    return false;
  }

  public boolean isGlobal() {
    return !myNamespace.isLocal();
  }
}
