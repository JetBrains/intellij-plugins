// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.stubs.impl.JSPropertyStubImpl;
import com.intellij.lang.javascript.types.JSPropertyElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;

import java.io.IOException;

public class Angular2PropertyStubImpl extends JSPropertyStubImpl {
  public Angular2PropertyStubImpl(JSProperty psi,
                                  StubElement parentStub,
                                  JSPropertyElementType type) {
    super(psi, parentStub, type);
  }

  public Angular2PropertyStubImpl(StubInputStream stream,
                                  StubElement parentStub,
                                  JSPropertyElementType type) throws IOException {
    super(stream, parentStub, type);
  }

  @Override
  public JSProperty createPsi() {
    return new Angular2PropertyImpl(this);
  }
}
