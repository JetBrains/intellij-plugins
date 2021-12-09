/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.rename;

import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.application.TransactionId;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.core.GaugeCli;
import com.thoughtworks.gauge.inspection.GaugeError;
import com.thoughtworks.gauge.undo.UndoHandler;
import com.thoughtworks.gauge.util.GaugeUtil;
import gauge.messages.Api;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

final class GaugeRefactorHandler {

  private final Project project;
  private final PsiFile file;
  private final Editor editor;

  GaugeRefactorHandler(Project project, PsiFile file, Editor editor) {
    this.project = project;
    this.file = file;
    this.editor = editor;
  }

  void compileAndRefactor(String currentStepText, String newStepText, @NotNull RefactorStatusCallback refactorStatusCallback) {
    refactorStatusCallback.onStatusChange(GaugeBundle.message("gauge.status.compiling"));
    TransactionId contextTransaction = TransactionGuard.getInstance().getContextTransaction();
    CompilerManager.getInstance(project).make((aborted, errors, warnings, context) -> {
      if (errors > 0) {
        refactorStatusCallback.onFinish(new RefactoringStatus(false, GaugeBundle.message("please.fix.all.errors.before.refactoring")));
        return;
      }
      refactor(currentStepText, newStepText, contextTransaction, context, refactorStatusCallback);
    });
  }

  private void refactor(String currentStepText,
                        String newStepText,
                        TransactionId contextTransaction,
                        CompileContext context,
                        RefactorStatusCallback refactorStatusCallback) {
    refactorStatusCallback.onStatusChange(GaugeBundle.message("status.refactoring"));

    Module module = GaugeUtil.moduleForPsiElement(file);
    TransactionGuard.getInstance().submitTransaction(() -> {
    }, contextTransaction, () -> {
      Api.PerformRefactoringResponse response;
      FileDocumentManager.getInstance().saveAllDocuments();
      FileDocumentManager.getInstance().saveDocumentAsIs(editor.getDocument());
      GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(project);

      GaugeCli gaugeCli = bootstrapService.getGaugeCli(module, true);
      try {
        response = gaugeCli.getGaugeConnection().sendPerformRefactoringRequest(currentStepText, newStepText);
      }
      catch (Exception e) {
        refactorStatusCallback
          .onFinish(new RefactoringStatus(false, GaugeBundle.message("could.not.execute.refactor.command", e.toString())));
        return;
      }
      new UndoHandler(response.getFilesChangedList(), module.getProject(), GaugeBundle.message("command.name.refactoring")).handle();
      if (!response.getSuccess()) {
        showMessage(response, context, refactorStatusCallback);
        return;
      }
      refactorStatusCallback.onFinish(new RefactoringStatus(true));
    });
  }

  private static void showMessage(Api.PerformRefactoringResponse response,
                                  CompileContext context,
                                  RefactorStatusCallback refactorStatusCallback) {
    refactorStatusCallback.onFinish(new RefactoringStatus(false, GaugeBundle.message("please.fix.all.errors.before.refactoring")));
    for (@NlsSafe String error : response.getErrorsList()) {
      GaugeError gaugeError = GaugeError.parseCliError(error);
      if (gaugeError != null) {
        context.addMessage(CompilerMessageCategory.ERROR, gaugeError.getMessage(), Paths.get(gaugeError.getFileName()).toUri().toString(),
                           gaugeError.getLineNumber(), -1);
      }
      else {
        context.addMessage(CompilerMessageCategory.ERROR, error, null, -1, -1);
      }
    }
  }
}
