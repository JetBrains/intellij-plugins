// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLocalVariable;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSVariableBaseImpl;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class JSLocalVariableImpl extends JSVariableBaseImpl<JSVariableStub<JSVariable>, JSVariable> implements JSLocalVariable {
  public JSLocalVariableImpl(final ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSElementVisitor) {
      ((JSElementVisitor)visitor).visitJSLocalVariable(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public boolean isLocal() {
    return true;
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    PsiElement fun = PsiTreeUtil.getParentOfType(this, JSFunction.class);
    if (fun == null) {
      // usually happens only if there are psi modifications, like inline function and before reparse, see IDEA-87950
      fun = getContainingFile();
    }
    return new LocalSearchScope(fun);
  }

  @Override
  public Icon getIcon(int flags) {
    return ElementBase.iconWithVisibilityIfNeeded(
      flags,
      blendFlags(IconManager.getInstance().getPlatformIcon(PlatformIcons.Variable), false, isConst()),
      JSAttributeList.AccessType.PACKAGE_LOCAL.getIcon()
    );
  }

  @Override
  public PsiElement getTypeElement() {
    return ActionScriptPsiImplUtil.getTypeElementFromDeclaration(this);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (ActionScriptVariableImpl.skipLocal(this, processor, lastParent, place)) return true;
    return super.processDeclarations(processor, state, lastParent, place);
  }
}
