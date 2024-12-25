// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public final class JpsFlexCompilerOptionsRole extends JpsElementChildRoleBase<JpsFlexCompilerOptions>
  implements JpsElementCreator<JpsFlexCompilerOptions> {

  public static final JpsFlexCompilerOptionsRole INSTANCE = new JpsFlexCompilerOptionsRole();

  private JpsFlexCompilerOptionsRole() {
    super("flex compiler options");
  }

  @Override
  public @NotNull JpsFlexCompilerOptions create() {
    return new JpsFlexCompilerOptionsImpl();
  }
}
