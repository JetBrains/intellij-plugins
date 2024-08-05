// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.PropertyKey;

import java.util.EnumSet;

public enum FlashUmlDependenciesSettingsOption {

  ONE_TO_ONE("uml.dependencies.one.to.one"),
  ONE_TO_MANY("uml.dependencies.one.to.many"),
  USAGES("uml.dependencies.usages"),
  SELF("uml.dependencies.self"),
  CREATE("uml.dependencies.create");

  private final String myResourceBundleKey;

  FlashUmlDependenciesSettingsOption(final @PropertyKey(resourceBundle = FlexBundle.BUNDLE) String resourceBundleKey) {
    myResourceBundleKey = resourceBundleKey;
  }

  public @Nls String getDisplayName() {
    return FlexBundle.message(myResourceBundleKey);
  }

  public static EnumSet<FlashUmlDependenciesSettingsOption> getEnabled() {
    EnumSet<FlashUmlDependenciesSettingsOption> result = EnumSet.noneOf(FlashUmlDependenciesSettingsOption.class);

    final DiagramProvider provider = DiagramProvider.findByID(FlashUmlProvider.ID);
    DiagramConfiguration configuration = DiagramConfiguration.getInstance();
    for (FlashUmlDependenciesSettingsOption option : values()) {
      if (configuration.isEnabledByDefault(provider, option.getDisplayName())) {
        result.add(option);
      }
    }
    return result;
  }
}
