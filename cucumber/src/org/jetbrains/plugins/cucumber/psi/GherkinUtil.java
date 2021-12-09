// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;

public final class GherkinUtil {
  @NotNull
  public static String getFeatureLanguage(@Nullable GherkinFile gherkinFile) {
    return gherkinFile != null ? gherkinFile.getLocaleLanguage() : GherkinFileImpl.getDefaultLocale();
  }
}
