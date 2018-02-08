package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

public interface PrettierLanguageService {
  @NotNull
  Future<FormatResult> format(@NotNull PsiFile file, @NotNull NodePackage prettierPackage, @Nullable TextRange range);

  @NotNull
  static PrettierLanguageService getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, PrettierLanguageService.class);
  }

  class FormatResult {
    public FormatResult(String result, String error) {
      this.result = result;
      this.error = error;
    }

    public static FormatResult error(String error) {
      return new FormatResult(null, error);
    }

    public static FormatResult formatted(String result) {
      return new FormatResult(result, null);
    }

    public final String result;
    public final String error;
  }
}
