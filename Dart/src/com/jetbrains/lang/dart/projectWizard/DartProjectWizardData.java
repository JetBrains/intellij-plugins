// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectWizard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartProjectWizardData {
  public final @NotNull String dartSdkPath;
  public final @Nullable DartProjectTemplate myTemplate;

  public DartProjectWizardData(final @NotNull String dartSdkPath, final @Nullable DartProjectTemplate template) {
    this.dartSdkPath = dartSdkPath;
    myTemplate = template;
  }
}
