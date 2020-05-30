// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.stubs.JSPropertyStub;
import com.intellij.lang.javascript.types.JSPropertyElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.lang.expr.psi.impl.Angular2PropertyImpl;
import org.angular2.lang.expr.psi.impl.Angular2PropertyStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Angular2PropertyElementType extends JSPropertyElementType {

  public Angular2PropertyElementType() {
    this("NG:PROPERTY");
  }

  protected Angular2PropertyElementType(@NonNls String debugName) {
    super(debugName);
  }

  @Override
  public JSElement construct(ASTNode node) {
    return new Angular2PropertyImpl(node);
  }

  @Override
  public @NotNull JSPropertyStub createStub(@NotNull JSProperty psi, StubElement parentStub) {
    return new Angular2PropertyStubImpl(psi, parentStub, this);
  }

  @Override
  public @NotNull JSPropertyStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new Angular2PropertyStubImpl(dataStream, parentStub, this);
  }

  @Override
  public boolean shouldIndexSymbol(@NotNull JSProperty psi) {
    return psi.getIndexingData() == null;
  }
}