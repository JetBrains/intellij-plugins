// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;

public final class GherkinUtil {
  public static @NotNull String getFeatureLanguage(@Nullable GherkinFile gherkinFile) {
    return gherkinFile != null ? gherkinFile.getLocaleLanguage() : GherkinFileImpl.getDefaultLocale();
  }
}
