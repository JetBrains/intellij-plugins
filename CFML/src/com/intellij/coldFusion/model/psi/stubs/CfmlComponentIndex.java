// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class CfmlComponentIndex extends StringStubIndexExtension<CfmlComponent> {
  public static final StubIndexKey<String, CfmlComponent> KEY = StubIndexKey.createIndexKey("cfml.component.shortName");

  @Override
  @NotNull
  public StubIndexKey<String, CfmlComponent> getKey() {
    return KEY;
  }
}
