// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartGenerateEqualsAndHashcodeAction extends BaseDartGenerateAction {

  private static final String EQUALS_OP = "==";
  private static final String HASHCODE = "hashCode";

  @Override
  protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateEqualsAndHashcodeHandler();
  }

  @Override
  protected boolean doEnable(final @Nullable DartClass dartClass) {
    if (dartClass == null) {
      return false;
    }
    return !doesClassContainEqualsAndHashCode(dartClass);
  }

  public static boolean doesClassContainEqualsAndHashCode(final @NotNull DartClass dartClass) {
    return doesClassContainMethod(dartClass, EQUALS_OP) && doesClassContainGetter(dartClass, HASHCODE);
  }
}
