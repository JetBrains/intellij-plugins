// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSSuppressionHolder;
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl;
import com.intellij.psi.HintedReferenceHost;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2EmbeddedExpressionImpl extends JSElementImpl
  implements JSSuppressionHolder, Angular2EmbeddedExpression, HintedReferenceHost {

  public Angular2EmbeddedExpressionImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public @NotNull Language getLanguage() {
    return Angular2Language.INSTANCE;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSElementVisitor) {
      ((JSElementVisitor)visitor).visitJSEmbeddedContent(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public void subtreeChanged() {
    super.subtreeChanged();
    JSControlFlowService.getService(getProject()).resetFlow(this);
  }

  @Override
  public boolean allowTopLevelThis() {
    return true;
  }

  @Override
  public @Nullable Character getQuoteChar() {
    return JSEmbeddedContentImpl.getQuoteChar(this);
  }

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull PsiReferenceService.Hints hints) {
    return PsiReference.EMPTY_ARRAY;
  }

  @Override
  public boolean shouldAskParentForReferences(@NotNull PsiReferenceService.Hints hints) {
    return false;
  }
}
