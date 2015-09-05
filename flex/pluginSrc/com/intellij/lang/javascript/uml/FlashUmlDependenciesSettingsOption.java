package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.EnumSet;

/**
 * User: ksafonov
 */
public enum FlashUmlDependenciesSettingsOption {

  ONE_TO_ONE("uml.dependencies.one.to.one"),
  ONE_TO_MANY("uml.dependencies.one.to.many"),
  USAGES("uml.dependencies.usages"),
  SELF("uml.dependencies.self"),
  CREATE("uml.dependencies.create");

  private final String myResourceBundleKey;

  FlashUmlDependenciesSettingsOption(@PropertyKey(resourceBundle = FlexBundle.BUNDLE) final String resourceBundleKey) {
    myResourceBundleKey = resourceBundleKey;
  }

  public String getDisplayName() {
    return FlexBundle.message(myResourceBundleKey);
  }

  public static EnumSet<FlashUmlDependenciesSettingsOption> getEnabled() {
    EnumSet<FlashUmlDependenciesSettingsOption> result = EnumSet.noneOf(FlashUmlDependenciesSettingsOption.class);

    final DiagramProvider provider = DiagramProvider.findByID(FlashUmlProvider.ID);
    DiagramConfiguration configuration = DiagramConfiguration.getConfiguration();
    for (FlashUmlDependenciesSettingsOption option : values()) {
      if (configuration.isEnabledByDefault(provider, option.getDisplayName())) {
        result.add(option);
      }
    }
    return result;
  }
}
