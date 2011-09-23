package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAndroidPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AndroidPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAndroidPackagingOptions> {

  public AndroidPackagingConfigurable(final Module module, final ModifiableAndroidPackagingOptions model) {
    super(module, model, Mode.Android);
  }

  @Nls
  public String getDisplayName() {
    return "Android";
  }
}
