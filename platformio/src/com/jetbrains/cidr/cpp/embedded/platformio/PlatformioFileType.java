package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PlatformioFileType implements FileType {
  public static final String EXTENSION = "ini";
  public static final PlatformioFileType INSTANCE = new PlatformioFileType();
  public static final String FILE_NAME = "platformio.ini";

  @Override
  public @NotNull
  String getName() {
    return "platformio";
  }

  @Override
  public @NotNull
  String getDescription() {
    return "PlatformIO";
  }

  @Override
  public @NotNull
  String getDefaultExtension() {
    return EXTENSION;
  }

  @Override
  public @Nullable
  Icon getIcon() {
    return ClionEmbeddedPlatformioIcons.Platformio;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public @Nullable
  String getCharset(@NotNull VirtualFile virtualFile, @NotNull byte[] bytes) {
    return null;
  }

  public static boolean isFileOfType(@Nullable VirtualFile file) {
    return file != null && FileTypeManager.getInstance().isFileOfType(file, INSTANCE);
  }
}
