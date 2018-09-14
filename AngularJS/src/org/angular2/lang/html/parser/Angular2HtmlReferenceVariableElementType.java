// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.types.JSVariableElementType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.lang.html.psi.impl.Angular2HtmlReferenceVariableImpl;
import org.angular2.lang.html.psi.impl.Angular2HtmlReferenceVariableStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Angular2HtmlReferenceVariableElementType extends JSVariableElementType {

  public Angular2HtmlReferenceVariableElementType() {
    super("NG:REFERENCE_VARIABLE");
  }

  @NotNull
  @Override
  public JSVariableStub<JSVariable> createStub(@NotNull JSVariable psi, StubElement parentStub) {
    return new Angular2HtmlReferenceVariableStubImpl(psi, parentStub, this);
  }

  @Override
  public boolean shouldCreateStub(final ASTNode node) {
    return false;
  }

  @Override
  public PsiElement construct(ASTNode node) {
    return new Angular2HtmlReferenceVariableImpl(node);
  }

  @NotNull
  @Override
  public JSVariableStub<JSVariable> deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new Angular2HtmlReferenceVariableStubImpl(dataStream, parentStub, this);
  }
}
