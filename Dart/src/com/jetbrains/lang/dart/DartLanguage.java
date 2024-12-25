// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public final class DartLanguage extends Language {
  public static final Language INSTANCE = new DartLanguage();

  public static final String DART_MIME_TYPE = "application/dart";

  private DartLanguage() {
    super("Dart", DART_MIME_TYPE);
  }

  @Override
  public @Nullable LanguageFileType getAssociatedFileType() {
    return DartFileType.INSTANCE;
  }
}
