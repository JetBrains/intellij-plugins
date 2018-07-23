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
package com.intellij.lang.javascript.linter.tslint.fix;


import com.intellij.execution.ExecutionException;
import com.intellij.history.LocalHistory;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterFixAction;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

@SuppressWarnings("ComponentNotRegistered")
public class TsLintFileFixAction extends JSLinterFixAction {

  public TsLintFileFixAction() {
    super(TsLintBundle.message("tslint.framework.title"),
          TsLintBundle.message("tslint.action.fix.all.problem.title"), null);
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(JSBundle.message("javascript.linter.action.fix.problems.file.text", TsLintBundle.message("tslint.framework.title")));
  }

  @NotNull
  @Override
  protected JSLinterConfiguration getConfiguration(Project project) {
    return TsLintConfiguration.getInstance(project);
  }

  @Override
  protected Task createTask(@NotNull Project project, @NotNull Collection<VirtualFile> filesToProcess, @NotNull Runnable completeCallback) {
    LocalHistory
      .getInstance().putSystemLabel(project, JSBundle
      .message("javascript.linter.action.fix.problems.name.start", TsLintBundle.message("tslint.framework.title")));
    return new Task.Backgroundable(project, TsLintBundle.message("tslint.action.background.title"), true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        TsLintLanguageService service = TsLintLanguageService.getService(project);
        TsLintState state = TsLintConfiguration.getInstance(project).getExtendedState().getState();
        for (VirtualFile file : filesToProcess) {
          indicator.setText("Processing file " + file.getCanonicalPath());
          final Future<List<TsLinterError>> future = ReadAction.compute(() -> service.highlightAndFix(file, state));
          try {
            JSLanguageServiceUtil.awaitLanguageService(future, service);
          }
          catch (ExecutionException e) {
            JSLinterGuesser.NOTIFICATION_GROUP.createNotification("TSLint: " + e.getMessage(), MessageType.ERROR).notify(project);
          }
        }

        completeCallback.run();
      }
    };
  }

  protected Collection<FileType> getFileTypes() {
    return TypeScriptUtil.TYPESCRIPT_FILE_TYPES;
  }
}