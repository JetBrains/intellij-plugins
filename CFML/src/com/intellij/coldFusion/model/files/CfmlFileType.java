// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
  @NotNull
  public String getName() {
    return "CFML";
  }

  @Override
  @NotNull
  public String getDescription() {
    return CfmlBundle.message("file.type.description.coldfusion");
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return "cfm";
  }

  @Override
  public Icon getIcon() {
    return CFMLIcons.Cfml;
  }

  @NonNls
  public String @NotNull [] getExtensions() {
    return new String[]{"cfm", "cfml", "cfc"};
  }
}
