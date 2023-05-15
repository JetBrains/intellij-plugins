// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.project.BaseProjectDirectories;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface PrettierLanguageService {
  @Nullable
  CompletableFuture<FormatResult> format(@NotNull String filePath,
                                         String ignoreFilePath, @NotNull String text,
                                         @NotNull NodePackage prettierPackage,
                                         @Nullable TextRange range);

  @NotNull
  static PrettierLanguageServiceImpl getInstance(@NotNull Project project,
                                                 @NotNull VirtualFile contextVirtualFile,
                                                 @NotNull NodePackage prettierPackage) {
    VirtualFile packageJson = PackageJsonUtil.findUpPackageJson(contextVirtualFile);
    VirtualFile workingDirectory = packageJson != null ? packageJson.getParent() : null;
    if (workingDirectory == null) {
      workingDirectory = BaseProjectDirectories.getInstance(project).getBaseDirectoryFor(contextVirtualFile);
    }
    if (workingDirectory == null) {
      workingDirectory = contextVirtualFile.getParent();
    }
    return project.getService(PrettierLanguageServiceManager.class)
      .useService(workingDirectory, NodePackageRef.create(prettierPackage), service -> service);
  }

  final class FormatResult {
    public static final FormatResult IGNORED = new FormatResult(null, null, true, false);
    public static final FormatResult UNSUPPORTED = new FormatResult(null, null, false, true);
    public final String result;
    public final String error;
    public final boolean ignored;
    public final boolean unsupported;

    private FormatResult(String result, String error, boolean ignored, boolean unsupported) {
      this.result = result;
      this.error = error;
      this.ignored = ignored;
      this.unsupported = unsupported;
    }

    public static FormatResult error(String error) {
      return new FormatResult(null, error, false, false);
    }

    public static FormatResult formatted(String result) {
      return new FormatResult(result, null, false, false);
    }
  }
}
