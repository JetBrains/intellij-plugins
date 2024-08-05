// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAndroidPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AndroidPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAndroidPackagingOptions> {
  public AndroidPackagingConfigurable(final Module module,
                                      final ModifiableAndroidPackagingOptions model,
                                      final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    super(module, model, airDescriptorInfoProvider);
  }

  @Override
  public @Nls String getDisplayName() {
    return getTabName();
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.Android";
  }

  public static String getTabName() {
    return FlexBundle.message("bc.tab.android.display.name");
  }
}
