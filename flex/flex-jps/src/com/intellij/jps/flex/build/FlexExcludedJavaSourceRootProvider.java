package com.intellij.jps.flex.build;

import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.ExcludedJavaSourceRootProvider;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

public class FlexExcludedJavaSourceRootProvider extends ExcludedJavaSourceRootProvider {
  @Override
  public boolean isExcludedFromCompilation(@NotNull JpsModule module, @NotNull JpsModuleSourceRoot root) {
    return module.getModuleType() == JpsFlexModuleType.INSTANCE;
  }
}
