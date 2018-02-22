package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

public interface PrettierLanguageService {
  @NotNull
  Future<FormatResult> format(@NotNull PsiFile file, @NotNull NodePackage prettierPackage, @Nullable TextRange range);

  /**
   * returns null if service not started or initSupportedFiles not called
   */
  @Nullable
  SupportedFilesInfo getSupportedFiles();

  @NotNull
  Future<Void> initSupportedFiles(NodePackage prettierPackage);

  @NotNull
  static PrettierLanguageServiceImpl getInstance(@NotNull Project project) {
    return ((PrettierLanguageServiceImpl)ServiceManager.getService(project, PrettierLanguageService.class));
  }

  class SupportedFilesInfo {
    @NotNull
    public final List<String> fileNames;
    @NotNull
    public final List<String> extensions;

    public SupportedFilesInfo(@NotNull List<String> fileNames, @NotNull List<String> extensions) {
      this.fileNames = fileNames;
      this.extensions = extensions;
    }
  }

  class FormatResult {
    private FormatResult(String result, String error) {
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
