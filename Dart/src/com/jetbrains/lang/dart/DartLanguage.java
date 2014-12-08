package com.jetbrains.lang.dart;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public class DartLanguage extends Language {
  public static final Language INSTANCE = new DartLanguage();

  public static final String DART_MIME_TYPE = "application/dart";

  private DartLanguage() {
    super("Dart", DART_MIME_TYPE);
  }

  @Nullable
  @Override
  public LanguageFileType getAssociatedFileType() {
    return DartFileType.INSTANCE;
  }
}
