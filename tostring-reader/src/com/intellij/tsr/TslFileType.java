// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class TslFileType extends LanguageFileType {

  public static final TslFileType INSTANCE = new TslFileType();

  public TslFileType() {
    super(TslLanguage.INSTANCE);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return "ToString";
  }

  @Override
  public @NotNull String getDescription() {
    return ToStringReaderBundle.message("label.java.toString.output");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "toString";
  }

  @Override
  public Icon getIcon() {
    return TslIcons.FILE_ICON;
  }
}
