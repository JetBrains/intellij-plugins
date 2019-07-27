// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.fileWatcher;

import com.intellij.ide.macro.FilePathRelativeToProjectRootMacro;
import com.intellij.ide.macro.ProjectFileDirMacro;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.prettierjs.PrettierBundle;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class PrettierBackgroundTaskConsumer extends BackgroundTaskConsumer {
  @Override
  public boolean isAvailable(@NotNull PsiFile file) {
    return false;
  }

  @NotNull
  @Override
  public TaskOptions getOptionsTemplate() {
    TaskOptions options = new TaskOptions();
    options.setName("Prettier");
    options.setDescription(PrettierBundle.message("file.watcher.description"));
    options.setFileExtension(JavaScriptFileType.INSTANCE.getDefaultExtension());
    options.setScopeName(PsiBundle.message("psi.search.scope.project"));

    String projectFileDirMacro = new ProjectFileDirMacro().getName();
    String projectRootRelativePathMacro = new FilePathRelativeToProjectRootMacro().getName();
    options.setProgram("$" + projectFileDirMacro + "$" + "/node_modules/.bin/prettier");
    options.setArguments("--write $" + projectRootRelativePathMacro + "$");
    options.setWorkingDir("$" + projectFileDirMacro + "$");

    options.setOutput("$" + projectRootRelativePathMacro + "$");
    options.setImmediateSync(false);
    options.setTrackOnlyRoot(false);

    return options;
  }
}
