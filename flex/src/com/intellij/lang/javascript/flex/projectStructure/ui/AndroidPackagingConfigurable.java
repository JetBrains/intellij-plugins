package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAndroidPackagingOptions;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nls;

public class AndroidPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAndroidPackagingOptions> {
  public static final String TAB_NAME = FlexBundle.message("bc.tab.android.display.name");

  public AndroidPackagingConfigurable(final Module module,
                                      final ModifiableAndroidPackagingOptions model,
                                      final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    super(module, model, airDescriptorInfoProvider);
  }

  @Nls
  public String getDisplayName() {
    return TAB_NAME;
  }

  public String getHelpTopic() {
    return "BuildConfigurationPage.Android";
  }
}
