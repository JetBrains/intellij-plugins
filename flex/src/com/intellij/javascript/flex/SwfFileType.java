// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SwfFileType implements FileType {
  public static final FileType SWF_FILE_TYPE = new SwfFileType();

  private SwfFileType() {
  }

  @Override
  @NotNull
  public String getName() {
    return "SWF";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "Adobe Flash animation";
  }

  @Nls
  @Override
  public @NotNull String getDisplayName() {
    return "Adobe Flash";
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
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
