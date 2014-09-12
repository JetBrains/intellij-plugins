package com.dmarcotte.handlebars;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbLanguage extends Language implements TemplateLanguage {
  public static final HbLanguage INSTANCE = new HbLanguage();

  @SuppressWarnings("SameReturnValue") // ideally this would be public static, but the static inits in the tests get cranky when we do that
  public static LanguageFileType getDefaultTemplateLang() {
    return StdFileTypes.HTML;
  }

  public HbLanguage() {
    super("Handlebars", "text/x-handlebars-template", "text/x-handlebars");
  }

  public HbLanguage(@Nullable Language baseLanguage, @NotNull @NonNls final String ID, @NotNull @NonNls final String... mimeTypes) {
    super(baseLanguage, ID, mimeTypes);
  }
}
