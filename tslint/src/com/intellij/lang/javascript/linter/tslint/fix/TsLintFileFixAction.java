// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.fix;


import com.intellij.execution.ExecutionException;
import com.intellij.history.LocalHistory;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterFixAction;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class TsLintFileFixAction extends JSLinterFixAction {

  public TsLintFileFixAction() {
    super(TsLintBundle.messagePointer("tslint.framework.title"),
          TsLintBundle.messagePointer("tslint.action.fix.all.problem.title"), null);
  }

  @Override
  protected boolean isFileAccepted(@NotNull Project project, @NotNull VirtualFile file) {
    FileType fileType = file.getFileType();
    return TypeScriptUtil.TYPESCRIPT_FILE_TYPES.contains(fileType)
           || DialectDetector.JAVASCRIPT_FILE_TYPES.contains(fileType);
  }

  @Override
  protected @NotNull JSLinterConfiguration getConfiguration(@NotNull Project project) {
    return TsLintConfiguration.getInstance(project);
  }

  @Override
  protected Task createTask(@NotNull Project project,
                            @NotNull Collection<? extends VirtualFile> filesToProcess,
                            @NotNull Runnable completeCallback,
                            boolean modalProgress) {
    LocalHistory
      .getInstance().putSystemLabel(project, JavaScriptBundle
      .message("javascript.linter.action.fix.problems.name.start", TsLintBundle.message("tslint.framework.title")));

    Consumer<ProgressIndicator> task = indicator -> {
      TslintLanguageServiceManager languageServiceManager = TslintLanguageServiceManager.getInstance(project);
      TsLintState state = TsLintConfiguration.getInstance(project).getExtendedState().getState();
      for (VirtualFile file : filesToProcess) {
        indicator.setText(TsLintBundle.message("tslint.progress.text.processing.file", file.getCanonicalPath()));
        languageServiceManager.useService(file, state.getNodePackageRef(), service -> {
          if (service == null) {
            return null;
          }
          final Future<List<TsLinterError>> future = ReadAction.compute(() -> service.highlightAndFix(file, state));
          try {
            JSLanguageServiceUtil.awaitLanguageService(future, service, file);
          }
          catch (ExecutionException e) {
            JSLinterGuesser.NOTIFICATION_GROUP.createNotification(
              TsLintBundle.message("tslint.notification.content", e.getMessage()), MessageType.ERROR).notify(project);
          }
          return null;
        });
      }

      completeCallback.run();
    };

    if (modalProgress) {
      return new Task.Modal(project, TsLintBundle.message("tslint.action.modal.title"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) { task.consume(indicator); }
      };
    }
    else {
      return new Task.Backgroundable(project, TsLintBundle.message("tslint.action.background.title"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) { task.consume(indicator); }
      };
    }
  }
}