// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.prettierjs;

import com.intellij.ide.macro.FilePathRelativeToProjectRootMacro;
import com.intellij.ide.macro.ProjectFileDirMacro;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer;
import com.intellij.plugins.watcher.model.TaskOptions;
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
    options.setProgram("$" + projectFileDirMacro + "$" + "/node_modules/.bin/prettier" + (SystemInfo.isWindows ? ".cmd" : ""));
    options.setArguments("--write $" + projectRootRelativePathMacro + "$");
    options.setWorkingDir("$" + projectFileDirMacro + "$");

    options.setOutput("$" + projectRootRelativePathMacro + "$");
    options.setImmediateSync(false);

    return options;
  }
}
