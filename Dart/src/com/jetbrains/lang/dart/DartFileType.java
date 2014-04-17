package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartFileType extends LanguageFileType {
  public static final LanguageFileType INSTANCE = new DartFileType();
  public static final String DEFAULT_EXTENSION = "dart";

  private DartFileType() {
    super(DartLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Dart";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Dart files";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_file;
  }

  @Override
  public String getCharset(@NotNull VirtualFile virtualFile, @NotNull byte[] bytes) {
    return null;
  }
}
