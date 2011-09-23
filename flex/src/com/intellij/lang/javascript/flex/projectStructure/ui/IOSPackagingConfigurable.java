package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableIosPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class IOSPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableIosPackagingOptions> {

  public IOSPackagingConfigurable(final Module module, final ModifiableIosPackagingOptions model) {
    super(module, model, Mode.IOS);
  }

  @Nls
  public String getDisplayName() {
    return "iOS";
  }
}
