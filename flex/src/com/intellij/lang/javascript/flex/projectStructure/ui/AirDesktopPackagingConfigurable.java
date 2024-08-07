// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AirDesktopPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAirDesktopPackagingOptions> {
  public AirDesktopPackagingConfigurable(final Module module,
                                         final ModifiableAirDesktopPackagingOptions model,
                                         final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    super(module, model, airDescriptorInfoProvider);
  }

  @Override
  public @Nls String getDisplayName() {
    return getTabName();
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.AIRPackage";
  }

  public static @Nls String getTabName() {
    return FlexBundle.message("bc.tab.air.desktop.display.name");
  }
}
