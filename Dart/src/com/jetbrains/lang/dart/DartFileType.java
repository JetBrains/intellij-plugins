// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DartFileType extends LanguageFileType {
  public static final LanguageFileType INSTANCE = new DartFileType();
  public static final String DEFAULT_EXTENSION = "dart";

  private DartFileType() {
    super(DartLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "Dart";
  }

  @Override
  public @NotNull String getDescription() {
    return DartBundle.message("filetype.dart.description");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_file;
  }
}
