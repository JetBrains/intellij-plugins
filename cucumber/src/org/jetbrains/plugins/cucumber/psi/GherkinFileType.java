// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;

import javax.swing.*;


public final class GherkinFileType extends LanguageFileType {
  public static final GherkinFileType INSTANCE = new GherkinFileType();

  private GherkinFileType() {
    super(GherkinLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "Cucumber";
  }

  @Override
  public @NotNull String getDescription() {
    return CucumberBundle.message("filetype.cucumber.scenario.description");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "feature";
  }

  @Override
  public Icon getIcon() {
    return CucumberIcons.Cucumber;
  }
}
