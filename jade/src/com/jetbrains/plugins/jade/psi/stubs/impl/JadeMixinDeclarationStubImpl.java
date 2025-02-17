// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import org.jetbrains.annotations.Nullable;

public class JadeMixinDeclarationStubImpl extends StubBase<JadeMixinDeclarationImpl> implements NamedStub<JadeMixinDeclarationImpl> {

  private final String myName;

  public JadeMixinDeclarationStubImpl(StubElement parent,
                                         IStubElementType elementType, @Nullable String name) {
    super(parent, elementType);
    myName = name;
  }

  @Override
  public @Nullable String getName() {
    return myName;
  }
}
