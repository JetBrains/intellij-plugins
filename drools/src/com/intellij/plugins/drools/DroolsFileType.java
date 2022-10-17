// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DroolsFileType extends LanguageFileType {
  public static final DroolsFileType DROOLS_FILE_TYPE = new DroolsFileType();

  @NonNls
  public static final String DEFAULT_EXTENSION = "drl";

  private DroolsFileType() {
    super(DroolsLanguage.INSTANCE);
  }

  @Override
  @NotNull
  @NonNls
  public String getName() {
    return "Drools";
  }

  @Override
  @NonNls
  @NotNull
  public String getDescription() {
    return DroolsBundle.DROOLS;
  }

  @Override
  @NotNull
  @NonNls
  public String getDefaultExtension() {
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
