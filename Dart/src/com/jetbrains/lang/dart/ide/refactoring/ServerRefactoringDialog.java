/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class ServerRefactoringDialog extends RefactoringDialog {
  @Nullable protected final Editor myEditor;
  @NotNull private final ServerRefactoring myRefactoring;

  private boolean myHasPendingRequests;
  private RefactoringStatus myOptionsStatus;

  public ServerRefactoringDialog(@NotNull Project project, @Nullable Editor editor, @NotNull ServerRefactoring refactoring) {
    super(project, true);
    myEditor = editor;
    myRefactoring = refactoring;
    // Listen for responses.
    myRefactoring.setListener(new ServerRefactoring.ServerRefactoringListener() {
      @Override
      public void requestStateChanged(final boolean hasPendingRequests, @NotNull final RefactoringStatus optionsStatus) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            myHasPendingRequests = hasPendingRequests;
            myOptionsStatus = optionsStatus;
            validateButtons();
          }
        });
      }
    });
  }

  @Override
  protected void canRun() throws ConfigurationException {
    // has pending requests
    if (myHasPendingRequests || myOptionsStatus == null) {
      throw new ConfigurationException(null);
    }
    // has a fatal error
    if (myOptionsStatus.hasFatalError()) {
      throw new ConfigurationException(myOptionsStatus.getMessage());
    }
  }

  @Override
  protected void doAction() {
    // Validate final status.
    {
      final RefactoringStatus finalStatus = myRefactoring.checkFinalConditions();
      if (finalStatus == null) {
        return;
      }
      if (finalStatus.hasError()) {
        Messages.showErrorDialog(myProject, finalStatus.getMessage(), "Error");
        return;
      }
    }
    // Apply the change.
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final SourceChange change = myRefactoring.getChange();
        assert change != null;
        final Set<String> excludedIds = myRefactoring.getPotentialEdits();
        AssistUtils.applySourceChange(myProject, change, excludedIds);
        close(DialogWrapper.OK_EXIT_CODE);
      }
    });
  }

  @Override
  protected boolean hasPreviewButton() {
    return false;
  }
}
