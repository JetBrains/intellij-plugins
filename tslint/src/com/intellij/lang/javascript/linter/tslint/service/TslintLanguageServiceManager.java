// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.service;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public final class TslintLanguageServiceManager extends MultiRootJSLinterLanguageServiceManager<TsLintLanguageService> {
  public TslintLanguageServiceManager(@NotNull Project project) {
    super(project, TslintUtil.PACKAGE_NAME);
  }

  @NotNull
  public static TslintLanguageServiceManager getInstance(@NotNull Project project) {
    return project.getService(TslintLanguageServiceManager.class);
  }

  @NotNull
  @Override
  protected TsLintLanguageService createServiceInstance(@NotNull NodePackage resolvedPackage, @NotNull VirtualFile workingDirectory) {
    return new TsLintLanguageService(myProject, resolvedPackage, workingDirectory);
  }
}