package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableIosPackagingOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;

public class IOSPackagingConfigurable extends AirPackagingConfigurableBase<ModifiableIosPackagingOptions> {

  public IOSPackagingConfigurable(final Module module, final ModifiableIosPackagingOptions model,
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
    return "iOS";
  }
}
