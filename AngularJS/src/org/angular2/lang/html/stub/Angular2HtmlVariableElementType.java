// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.types.JSVariableElementType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl;
import org.angular2.lang.html.stub.impl.Angular2HtmlAttrVariableStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Angular2HtmlVariableElementType extends JSVariableElementType {
  private final Angular2HtmlAttrVariable.Kind myKind;

  public Angular2HtmlVariableElementType(Angular2HtmlAttrVariable.Kind kind) {
    super(kind.name() + "_VARIABLE");
    myKind = kind;
  }

  @NonNls
  @Override
  public String toString() {
    return Angular2HtmlStubElementTypes.EXTERNAL_ID_PREFIX + super.getDebugName();
  }

  @Override
  public @NotNull String getExternalId() {
    return toString();
  }

  @Override
  public @NotNull JSVariableStub<JSVariable> createStub(@NotNull JSVariable psi, StubElement parentStub) {
    return new Angular2HtmlAttrVariableStubImpl(psi, parentStub, this);
  }

  @Override
  public boolean shouldCreateStub(final ASTNode node) {
    return false;
  }

  @Override
  public PsiElement construct(ASTNode node) {
    return new Angular2HtmlAttrVariableImpl(node);
  }

  @Override
  public @NotNull JSVariableStub<JSVariable> deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new Angular2HtmlAttrVariableStubImpl(dataStream, parentStub, this);
  }

  public Angular2HtmlAttrVariable.Kind getKind() {
    return myKind;
  }
}
