package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PlatformioFileType extends LanguageFileType {
  public static final String EXTENSION = "ini";
  public static final PlatformioFileType INSTANCE = new PlatformioFileType();
  public static final String FILE_NAME = "platformio.ini";

  public PlatformioFileType() {
    super(findLanguage());
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public @NotNull
  String getName() {
    return "platformio";
  }

  @Override
  public @NotNull
  String getDescription() {
    return ClionEmbeddedPlatformioBundle.message("platformio.file.type");
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

  private static Language findLanguage() {
    Language language = Language.findLanguageByID("Ini");
    return language == null ? PlainTextLanguage.INSTANCE : language;
  }

  @Override
  @Nullable
  public String getCharset(@NotNull VirtualFile virtualFile, byte @NotNull [] bytes) {
    return null;
  }

  @Contract("null->false")
  public static boolean isFileOfType(@Nullable VirtualFile file) {
    return file != null && FileTypeManager.getInstance().isFileOfType(file, INSTANCE);
  }
}
