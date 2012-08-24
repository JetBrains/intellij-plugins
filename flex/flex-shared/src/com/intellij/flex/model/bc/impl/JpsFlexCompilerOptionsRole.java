package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;

public class JpsFlexCompilerOptionsRole extends JpsElementChildRoleBase<JpsFlexCompilerOptions>
  implements JpsElementCreator<JpsFlexCompilerOptions> {

  public static final JpsFlexCompilerOptionsRole INSTANCE = new JpsFlexCompilerOptionsRole();

  private JpsFlexCompilerOptionsRole() {
    super("flex compiler options");
  }

  @NotNull
  public JpsFlexCompilerOptions create() {
    return new JpsFlexCompilerOptionsImpl();
  }
}
