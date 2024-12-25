// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;


public class GherkinLanguage extends Language {
  public static GherkinLanguage INSTANCE = new GherkinLanguage();

  protected GherkinLanguage() {
    super("Gherkin");
  }

  @Override
  public @NotNull String getDisplayName() {
    return "Gherkin";
  }
}
