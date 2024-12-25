// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.CFMLIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlFileType extends LanguageFileType {
  public static final CfmlFileType INSTANCE = new CfmlFileType();

  private CfmlFileType() {
    super(CfmlLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "CFML";
  }

  @Override
  public @NotNull String getDescription() {
    return CfmlBundle.message("filetype.coldfusion.description");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "cfm";
  }

  @Override
  public Icon getIcon() {
    return CFMLIcons.Cfml;
  }

  public @NonNls String @NotNull [] getExtensions() {
    return new String[]{"cfm", "cfml", "cfc"};
  }
}
