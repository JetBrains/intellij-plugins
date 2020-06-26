// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
  @NotNull
  public JpsFlexCompilerOptions create() {
    return new JpsFlexCompilerOptionsImpl();
  }
}
