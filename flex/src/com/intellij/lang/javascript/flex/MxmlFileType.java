// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class MxmlFileType extends XmlLikeFileType {
  public static final LanguageFileType MXML = new MxmlFileType();

  private MxmlFileType() {
    super(MxmlLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "MXML";
  }

  @Override
  public @NotNull String getDescription() {
    return "MXML";
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "mxml";
  }

  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Xml; // TODO own icon
  }
}
