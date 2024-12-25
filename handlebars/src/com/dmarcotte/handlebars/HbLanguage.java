// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbLanguage extends Language implements TemplateLanguage, InjectableLanguage {
  public static final HbLanguage INSTANCE = new HbLanguage();

  @SuppressWarnings("SameReturnValue") // ideally this would be public static, but the static inits in the tests get cranky when we do that
  public static LanguageFileType getDefaultTemplateLang() {
    return HtmlFileType.INSTANCE;
  }

  private HbLanguage() {
    super("Handlebars", "text/x-handlebars-template", "text/x-handlebars", "text/ractive");
  }

  private HbLanguage(@Nullable Language baseLanguage, final @NotNull @NonNls String ID, final @NonNls String @NotNull ... mimeTypes) {
    super(baseLanguage, ID, mimeTypes);
  }
}
