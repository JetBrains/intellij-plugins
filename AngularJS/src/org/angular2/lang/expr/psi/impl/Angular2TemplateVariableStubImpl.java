// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lang.javascript.psi.stubs.impl.JSVariableStubBaseImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Angular2TemplateVariableStubImpl extends JSVariableStubBaseImpl<JSVariable> implements JSVariableStub<JSVariable> {
  public Angular2TemplateVariableStubImpl(JSVariable clazz,
                                          final StubElement parent,
                                          @NotNull JSStubElementType<?, JSVariable> elementType) {
    super(clazz, parent, elementType, 0);
  }

  public Angular2TemplateVariableStubImpl(final StubInputStream dataStream,
                                          final StubElement parentStub,
                                          @NotNull IStubElementType elementType)
    throws IOException {
    super(dataStream, parentStub, elementType);
  }

  @Override
  public JSVariable createPsi() {
    return new Angular2TemplateVariableImpl(this);
  }

  @Override
  protected boolean doIndexQualifiedName() {
    return false;
  }

  @Override
  protected boolean doIndexForQualifiedNameIndex() {
    return false;
  }

  @Override
  protected boolean doIndexForGlobalQualifiedNameIndex() {
    return false;
  }
}