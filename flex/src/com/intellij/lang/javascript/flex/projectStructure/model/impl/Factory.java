// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import org.jetbrains.annotations.NotNull;

public final class Factory {

  public static ModifiableCompilerOptions createCompilerOptions() {
    return new CompilerOptionsImpl();
  }

  public static ModifiableFlexBuildConfiguration createBuildConfiguration() {
    return new FlexBuildConfigurationImpl();
  }

  public static ModifiableDependencyType createDependencyTypeInstance() {
    return new DependencyTypeImpl();
  }

  public static SdkEntry createSdkEntry(@NotNull String name) {
    return new SdkEntryImpl(name);
  }

  public static ModifiableFlexBuildConfiguration getCopy(@NotNull FlexBuildConfiguration bc) {
    return ((FlexBuildConfigurationImpl)bc).getCopy();
  }

  public static ModifiableFlexBuildConfiguration getTemporaryCopyForCompilation(@NotNull FlexBuildConfiguration bc) {
    final FlexBuildConfigurationImpl copy = ((FlexBuildConfigurationImpl)bc).getCopy();
    copy.setTempBCForCompilation(true);
    return copy;
  }
}
