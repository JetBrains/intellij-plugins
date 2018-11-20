package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

public interface PrettierLanguageService {
  @Nullable
  Future<FormatResult> format(@NotNull String filePath,
                              String ignoreFilePath, @NotNull String text,
                              @NotNull NodePackage prettierPackage,
                              @Nullable TextRange range);

  @Nullable
  Future<SupportedFilesInfo> getSupportedFiles(@NotNull NodePackage prettierPackage);

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
    public static final FormatResult IGNORED = new FormatResult(null, null, null, true);
    public final String result;
    @Nullable public final LineSeparator lineSeparator;
    public final String error;
    public final boolean ignored;

    private FormatResult(String result, @Nullable LineSeparator lineSeparator, String error, boolean ignored) {
      this.result = result;
      this.lineSeparator = lineSeparator;
      this.error = error;
      this.ignored = ignored;
    }

    public static FormatResult error(String error) {
      return new FormatResult(null, null, error, false);
    }

    public static FormatResult formatted(String result, @Nullable LineSeparator lineSeparator) {
      return new FormatResult(result, lineSeparator, null, false);
    }
  }
}
