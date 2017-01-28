package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LineSeparator;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class DartFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
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

  @Override
  public boolean isMyFileType(@NotNull final VirtualFile file) {
    if (file.isDirectory()) {
      return false;
    }

    try {
      String text = FileUtil.loadTextAndClose(new FileInputStream(VfsUtilCore.virtualToIoFile(file)));
      if (text.startsWith("#!")) {
        String shebang = text.substring(2, text.indexOf(LineSeparator.getSystemLineSeparator().getSeparatorString())).trim();

        if (shebang.startsWith("/usr/bin/env ")) {
          shebang = shebang.substring(13);
        }
        
        if (shebang.equals("dart") || shebang.startsWith("dart ")) { // Could have extra options.
          return true;
        }
      }
    }
    catch (IOException e) {
      return false;
    }

    return false;
  }
}
