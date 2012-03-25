package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;

public class AirDesktopPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableAirDesktopPackagingOptions> {
  public static final String TAB_NAME = FlexBundle.message("bc.tab.air.desktop.display.name");

  public AirDesktopPackagingConfigurable(final Module module,
                                         final ModifiableAirDesktopPackagingOptions model,
                                         final Computable<String> mainClassComputable,
                                         final Computable<String> airVersionComputable,
                                         final Computable<Boolean> androidEnabledComputable,
                                         final Computable<Boolean> iosEnabledComputable,
                                         final Consumer<String> createdDescriptorConsumer) {
    super(module, model, mainClassComputable, airVersionComputable, androidEnabledComputable, iosEnabledComputable,
          createdDescriptorConsumer);
  }

  @Nls
  public String getDisplayName() {
    return TAB_NAME;
  }
}
