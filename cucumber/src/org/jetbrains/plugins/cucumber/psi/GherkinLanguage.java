// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;


public class GherkinLanguage extends Language {
  public static GherkinLanguage INSTANCE = new GherkinLanguage();

  protected GherkinLanguage() {
    super("Gherkin");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Gherkin";
  }
}
