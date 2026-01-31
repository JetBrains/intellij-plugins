// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSEmbeddedContent;
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

public class JSInJadeEmbeddedContentImpl extends ASTWrapperPsiElement implements JSEmbeddedContent {

  public JSInJadeEmbeddedContentImpl(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JavaScriptInJadeLanguageDialect.INSTANCE;
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    boolean result = JSResolveUtil.processDeclarationsInScope(this, processor, state, lastParent, place);
    if (result) {
      processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
    }
    return result;
  }

  @Override
  public String toString() {
    String s = getClass().getSimpleName();
    final IElementType type = getNode().getElementType();
    if (type != JadeTokenTypes.JS_CODE_BLOCK_PATCHED) s += ":" + type;
    return s;
  }

  @Override
  public void subtreeChanged() {
    super.subtreeChanged();
    JSControlFlowService.getService(getProject()).resetControlFlow(this);
  }

  @Override
  public IElementType getElementType() {
    return getNode().getElementType();
  }
}
