// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.psi.impl;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSStatementWithLabelReferenceImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptGotoStatementImpl extends JSStatementWithLabelReferenceImpl implements JSStatementWithLabelReference {
  public ActionScriptGotoStatementImpl(IElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSElementVisitor) {
      ((JSElementVisitor)visitor).visitJSStatementWithLabelReference(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  protected @NotNull LabelReference createReference(PsiElement label) {
    return new GotoLabelReference(label);
  }

  protected final class GotoLabelReference extends LabelReference {

    GotoLabelReference(PsiElement _labelNode) {
      super(_labelNode);
    }

    @Override
    protected void processElements(PsiElementProcessor<? super JSLabeledStatement> processor) {
      JSExecutionScope scope = PsiTreeUtil.getParentOfType(ActionScriptGotoStatementImpl.this, JSExecutionScope.class);
      if (scope == null) return;

      final Ref<Boolean> result = Ref.create(Boolean.TRUE);
      scope.acceptChildren(new JSRecursiveWalkingElementVisitor() {
        @Override
        public void visitJSLabeledStatement(@NotNull JSLabeledStatement node) {
          if (result.get()) result.set(processor.execute(node));
        }
      });
    }
  }
}
