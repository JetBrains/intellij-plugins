package com.jetbrains.plugins.meteor.spacebars.lang;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public final class SpacebarsLanguageDialect extends Language {
  public static final SpacebarsLanguageDialect INSTANCE = new SpacebarsLanguageDialect();

  private SpacebarsLanguageDialect() {
    super(HbLanguage.INSTANCE, "Spacebars");
  }

  @Nullable
  @Override
  public LanguageFileType getAssociatedFileType() {
    return SpacebarsFileType.SPACEBARS_INSTANCE;
  }
}
