package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AirDesktopPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAirDesktopPackagingOptions> {

  public AirDesktopPackagingConfigurable(final Module module, final ModifiableAirDesktopPackagingOptions model) {
    super(module, model, Mode.Desktop);
  }

  @Nls
  public String getDisplayName() {
    return "AIR Package";
  }
}
