// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.psi.ecmal4.impl;

import com.intellij.lang.actionscript.ActionScriptTextReferenceResolver;
import com.intellij.lang.javascript.psi.impl.JSTextReference;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public class ActionScriptTextReference extends JSTextReference {

  protected ActionScriptTextReference(@NotNull ActionScriptReferenceSet set,
                                      @NotNull String s, int offset) {
    super(set, s, offset);
  }

  public boolean isOnlyFqns() {
    return getSet().isOnlyFqns();
  }

  public String @NotNull [] getBaseClassFqns() {
    return getSet().getBaseClassFqns();
  }

  @Override
  public @NotNull ActionScriptReferenceSet getSet() {
    return (ActionScriptReferenceSet)super.getSet();
  }

  @Override
  public boolean useActionScriptIndex(@NotNull PsiFile psiFile) {
    return super.useActionScriptIndex(psiFile) || isOnlyFqns();
  }

  @Override
  protected ResolveResult @NotNull [] doResolve() {
    if("*".equals(myCanonicalText) || "?".equals(myCanonicalText)) {
      return new ResolveResult[] { new JSResolveResult(mySet.getElement())};
    }

    return ActionScriptTextReferenceResolver.resolve(this);
  }
}
