package com.jetbrains.plugins.jade;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JadeToPugTransitionHelper {
  public static final @NotNull List<String> ALL_EXTENSIONS = List.of("jade", "pug");

  static String trimAnyExtension(@NotNull String given, @NotNull List<String> ends) {
    for (String end : ends) {
      if (given.endsWith("." + end)) {
        return given.substring(0, given.length() - end.length() - 1);
      }
    }
    return given;
  }

  public static @NotNull String getExtension(PsiFile file) {
    final String extension = FileUtilRt.getExtension(file.getName());
    if (file instanceof JadeFileImpl && !extension.isEmpty()) {
      return extension;
    }
    else {
      return "jade";
    }
  }

  public static boolean isPugElement(@NotNull PsiElement element) {
    if (!element.isValid()) {
      return false;
    }
    final PsiFile file = element.getContainingFile();
    return !FileUtilRt.extensionEquals(file.getName(), "jade");
  }
}
