// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.jps.flex.build;

import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.java.ExcludedJavaSourceRootProvider;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

public final class FlexExcludedJavaSourceRootProvider extends ExcludedJavaSourceRootProvider {
  @Override
  public boolean isExcludedFromCompilation(@NotNull JpsModule module, @NotNull JpsModuleSourceRoot root) {
    return module.getModuleType() == JpsFlexModuleType.INSTANCE;
  }
}
