// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.psi.impl;

import com.intellij.lang.actionscript.ActionScriptInternalElementTypes;
import com.intellij.lang.actionscript.psi.JSE4XFilterQueryArgumentList;
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl;
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public final class JSE4XFilterQueryArgumentListImpl extends JSArgumentListImpl implements JSE4XFilterQueryArgumentList {
  public JSE4XFilterQueryArgumentListImpl() {
    super(ActionScriptInternalElementTypes.E4X_FILTER_QUERY_ARGUMENT_LIST);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (processor instanceof SinkResolveProcessor) {
      ((SinkResolveProcessor<?>)processor).setEncounteredXmlLiteral(true);
    }
    return super.processDeclarations(processor, state, lastParent, place);
  }
}