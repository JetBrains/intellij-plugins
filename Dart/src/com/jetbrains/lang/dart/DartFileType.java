// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @NotNull
  @Override
  public String getName() {
    return "Dart";
  }

  @NotNull
  @Override
  public String getDescription() {
    return DartBundle.message("file.type.description.dart");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_file;
  }
}
