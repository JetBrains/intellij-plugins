// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartGenerateToStringAction extends BaseDartGenerateAction {

  private static final String TO_STRING = "toString";

  @Override
  protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateToStringHandler();
  }

  @Override
  protected boolean doEnable(@Nullable DartClass dartClass) {
    if (dartClass == null) {
      return false;
    }
    return !doesClassContainMethod(dartClass, TO_STRING);
  }
}
