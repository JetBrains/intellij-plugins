package com.jetbrains.typoscript.lang;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;


public class TypoScriptLanguage extends Language {
  public static final TypoScriptLanguage INSTANCE = new TypoScriptLanguage();

  private TypoScriptLanguage() {
    super("TypoScript", "");
  }

  @Override
  public LanguageFileType getAssociatedFileType() {
    return TypoScriptFileType.INSTANCE;
  }
}
