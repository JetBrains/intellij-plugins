// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.impl.CfmlTagComponentImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlTagComponentElementTypeImpl extends CfmlComponentElementType {
  public CfmlTagComponentElementTypeImpl(final @NotNull @NonNls String debugName) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  @Override
  public CfmlComponent createPsi(@NotNull CfmlComponentStub stub) {
    return new CfmlTagComponentImpl(stub);
  }
}
