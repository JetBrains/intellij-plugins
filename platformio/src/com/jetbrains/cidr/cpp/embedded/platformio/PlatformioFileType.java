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

import static com.intellij.lang.Language.findLanguageByID;
import static icons.ClionEmbeddedPlatformioIcons.Platformio;

public class PlatformioFileType extends LanguageFileType {
  public static final String EXTENSION = "ini";
  public static final PlatformioFileType INSTANCE = new PlatformioFileType();
  public static final String FILE_NAME = "platformio.ini";

  private PlatformioFileType() {
    super(findLanguage());
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
    return EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return Platformio;
  }

  private static Language findLanguage() {
    final var language = findLanguageByID("Ini");
    return language == null ? PlainTextLanguage.INSTANCE : language;
  }

  @Contract("null->false")
  public static boolean isFileOfType(final @Nullable VirtualFile file) {
    return file != null && FileTypeManager.getInstance().isFileOfType(file, INSTANCE);
  }
}
