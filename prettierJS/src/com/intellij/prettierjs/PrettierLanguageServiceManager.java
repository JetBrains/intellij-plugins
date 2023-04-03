// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PrettierLanguageServiceManager extends MultiRootJSLinterLanguageServiceManager<PrettierLanguageServiceImpl> {
  public PrettierLanguageServiceManager(@NotNull Project project) {
    super(project, PrettierUtil.PACKAGE_NAME);
  }

  @Override
  protected @NotNull PrettierLanguageServiceImpl createServiceInstance(@NotNull NodePackage resolvedPackage,
                                                                       @NotNull VirtualFile workingDirectory) {
    return new PrettierLanguageServiceImpl(myProject, workingDirectory);
  }
}
