// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class CfmlInheritanceIndex extends StringStubIndexExtension<CfmlComponent> {
  public static final StubIndexKey<String, CfmlComponent> KEY = StubIndexKey.createIndexKey("cfml.inheritors.shortName");

  @Override
  public @NotNull StubIndexKey<String, CfmlComponent> getKey() {
    return KEY;
  }
}
