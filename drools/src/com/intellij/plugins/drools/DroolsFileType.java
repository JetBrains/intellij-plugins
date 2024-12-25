// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DroolsFileType extends LanguageFileType {
  public static final DroolsFileType DROOLS_FILE_TYPE = new DroolsFileType();

  public static final @NonNls String DEFAULT_EXTENSION = "drl";

  private DroolsFileType() {
    super(DroolsLanguage.INSTANCE);
  }

  @Override
  public @NotNull @NonNls String getName() {
    return "Drools";
  }

  @Override
  public @NonNls @NotNull String getDescription() {
    return DroolsBundle.DROOLS;
  }

  @Override
  public @NotNull @NonNls String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return JbossDroolsIcons.Drools_16;
  }

  @Override
  public String getCharset(@NotNull VirtualFile file, final byte @NotNull [] content) {
    return CharsetToolkit.UTF8;
  }
}
