// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.terraform.config.util.TFExecutor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TFFmtFileAction extends TFExternalToolsAction {
  @Override
  protected TFExecutor createExecutor(@NotNull Project project, @Nullable Module module, @NotNull @Nls String title, @NotNull VirtualFile virtualFile) {
    String filePath = virtualFile.getCanonicalPath();
    assert filePath != null;
    return createExecutor(project, module, title, filePath).withWorkDirectory(virtualFile.getParent().getCanonicalPath());
  }

  @Override
  @NotNull
  protected TFExecutor createExecutor(@NotNull Project project, @Nullable Module module, @NotNull @Nls String title, @NotNull String filePath) {
    return TFExecutor.in(project, module)
        .withPresentableName(title)
        .withParameters("fmt", filePath)
        .showOutputOnError();
  }
}