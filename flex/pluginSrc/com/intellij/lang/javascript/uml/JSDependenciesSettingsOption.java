package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.EnumSet;

/**
 * User: ksafonov
 */
public enum JSDependenciesSettingsOption {

  ONE_TO_ONE("uml.dependencies.one.to.one"),
  ONE_TO_MANY("uml.dependencies.one.to.many"),
  USAGES("uml.dependencies.usages"),
  SELF("uml.dependencies.self"),
  CREATE("uml.dependencies.create");

  private final String myResourceBundleKey;

  JSDependenciesSettingsOption(@PropertyKey(resourceBundle = FlexBundle.BUNDLE) final String resourceBundleKey) {
    myResourceBundleKey = resourceBundleKey;
  }

  public String getDisplayName() {
    return FlexBundle.message(myResourceBundleKey);
  }

  public static EnumSet<JSDependenciesSettingsOption> getEnabled() {
    EnumSet<JSDependenciesSettingsOption> result = EnumSet.noneOf(JSDependenciesSettingsOption.class);

    final DiagramProvider provider = DiagramProvider.findByID(JSUmlProvider.ID);
    DiagramConfiguration configuration = DiagramConfiguration.getConfiguration();
    for (JSDependenciesSettingsOption option : values()) {
      if (configuration.isEnabledByDefault(provider, option.getDisplayName())) {
        result.add(option);
      }
    }
    return result;
  }
}
