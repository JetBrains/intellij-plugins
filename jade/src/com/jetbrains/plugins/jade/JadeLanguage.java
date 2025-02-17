// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.jetbrains.annotations.NotNull;

public final class JadeLanguage extends Language implements TemplateLanguage, InjectableLanguage {

  public static final JadeLanguage INSTANCE = new JadeLanguage();

  private JadeLanguage() {
    super("Jade", "text/jade", "text/pug");
  }

  @Override
  public LanguageFileType getAssociatedFileType() {
    return JadeFileType.INSTANCE;
  }

  @Override
  public @NotNull String getDisplayName() {
    return "Pug (ex-Jade)";
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

}
