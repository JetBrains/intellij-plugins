package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableIosPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class IOSPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableIosPackagingOptions> {
  public IOSPackagingConfigurable(final Module module,
                                  final ModifiableIosPackagingOptions model,
                                  final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    super(module, model, airDescriptorInfoProvider);
  }

  @Override
  @Nls
  public String getDisplayName() {
    return getTabName();
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.iOS";
  }

  public static String getTabName() {
    return FlexBundle.message("bc.tab.ios.packaging.display.name");
  }
}
