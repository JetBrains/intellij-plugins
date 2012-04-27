package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AirDesktopPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAirDesktopPackagingOptions> {
  public static final String TAB_NAME = FlexBundle.message("bc.tab.air.desktop.display.name");

  public AirDesktopPackagingConfigurable(final Module module,
                                         final ModifiableAirDesktopPackagingOptions model,
                                         final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    super(module, model, airDescriptorInfoProvider);
  }

  @Nls
  public String getDisplayName() {
    return TAB_NAME;
  }

  public String getHelpTopic() {
    return "BuildConfigurationPage.AIRPackage";
  }
}
