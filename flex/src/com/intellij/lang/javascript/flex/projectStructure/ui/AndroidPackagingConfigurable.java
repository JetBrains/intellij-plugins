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
  @Nls
  public String getDisplayName() {
    return getTAB_NAME();
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.Android";
  }

  public static String getTAB_NAME() {
    return FlexBundle.message("bc.tab.android.display.name");
  }
}
