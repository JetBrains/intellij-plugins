package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import icons.ClionEmbeddedPlatformioIcons;
import ini4idea.IniLanguage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class PlatformioFileType extends LanguageFileType {
  public static final PlatformioFileType INSTANCE = new PlatformioFileType();
  public static final String FILE_NAME = "platformio.ini";

  private PlatformioFileType() {
    super(IniLanguage.INSTANCE, true);
  }

  @Override
  public @NotNull
  String getName() {
    return "platformio";
  }

  @Override
  public @NotNull
  String getDescription() {
    return ClionEmbeddedPlatformioBundle.message("filetype.platformio.description");
  }

  @Override
  public @NotNull
  String getDefaultExtension() {
    return "";
  }

  @Override
  public Icon getIcon() {
    return ClionEmbeddedPlatformioIcons.LogoPlatformIO;
  }

  @Contract("null->false")
  public static boolean isFileOfType(@Nullable VirtualFile file) {
    return file != null && FileTypeManager.getInstance().isFileOfType(file, INSTANCE);
  }
}
