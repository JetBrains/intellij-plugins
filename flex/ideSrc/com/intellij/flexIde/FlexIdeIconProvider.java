package com.intellij.flexIde;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.InternalFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexIdeIconProvider implements FileIconProvider, DumbAware {

  private static final Icon ICON = IconLoader.findIcon(ApplicationInfoEx.getInstanceEx().getSmallIconUrl());

  @Nullable
  @Override
  public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
    FileType fileType = file.getFileType();
    if (fileType instanceof InternalFileType) {
      return ICON;
    }
    return null;
  }

}
