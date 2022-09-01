// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.types.JSVariableElementType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableImpl;
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class Angular2TemplateVariableElementType extends JSVariableElementType {
  Angular2TemplateVariableElementType() {
    super("TEMPLATE_VARIABLE");
  }

  @Override
  public String toString() {
    return Angular2StubElementTypes.EXTERNAL_ID_PREFIX + super.getDebugName();
  }

  @Override
  public @NotNull String getExternalId() {
    return toString();
  }

  @Override
  public @NotNull JSVariableStub<JSVariable> createStub(@NotNull JSVariable psi, StubElement parentStub) {
    return new Angular2TemplateVariableStubImpl(psi, parentStub, this);
  }

  @Override
  public boolean shouldCreateStub(final ASTNode node) {
    return false;
  }

  @Override
  public PsiElement construct(ASTNode node) {
    return new Angular2TemplateVariableImpl(node);
  }

  @Override
  public @NotNull JSVariableStub<JSVariable> deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new Angular2TemplateVariableStubImpl(dataStream, parentStub, this);
  }
}
