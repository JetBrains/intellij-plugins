// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class SwfFileType implements FileType {
  public static final FileType SWF_FILE_TYPE = new SwfFileType();

  private SwfFileType() {
  }

  @Override
  public @NotNull String getName() {
    return "SWF";
  }

  @Override
  public @NotNull String getDescription() {
    return "Adobe Flash animation";
  }

  @Override
  public @Nls @NotNull String getDisplayName() {
    return "Adobe Flash";
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "swf";
  }

  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.JavaClass;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
