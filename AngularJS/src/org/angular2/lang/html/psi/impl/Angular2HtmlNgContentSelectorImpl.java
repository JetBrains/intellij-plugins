// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.ArrayUtil;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorImpl;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlNgContentSelectorImpl extends StubBasedPsiElementBase<Angular2HtmlNgContentSelectorStub>
  implements Angular2HtmlNgContentSelector, StubBasedPsiElement<Angular2HtmlNgContentSelectorStub> {

  public Angular2HtmlNgContentSelectorImpl(@NotNull Angular2HtmlNgContentSelectorStub stub,
                                           @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public Angular2HtmlNgContentSelectorImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull Angular2DirectiveSelector getSelector() {
    Angular2HtmlNgContentSelectorStub stub = getGreenStub();
    String text;
    if (stub != null) {
      text = stub.getSelector();
    }
    else {
      text = getText();
    }
    return new Angular2DirectiveSelectorImpl(this, text, 0);
  }

  @Override
  public String toString() {
    return "Angular2HtmlNgContentSelector (" + getSelector() + ")";
  }

  @Override
  public PsiReference getReference() {
    return ArrayUtil.getFirstElement(getReferences());
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitNgContentSelector(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
