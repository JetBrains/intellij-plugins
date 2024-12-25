// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class JdlFileType extends LanguageFileType {
  public static final JdlFileType INSTANCE = new JdlFileType();

  private JdlFileType() {
    super(JdlLanguage.INSTANCE);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return "JHipster JDL";
  }

  @Override
  public @NotNull String getDescription() {
    return JdlBundle.message("label.jhipster.specific.domain.language");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "jdl";
  }

  @Override
  public @NotNull Icon getIcon() {
    return JdlIconsMapping.FILE_ICON;
  }
}
