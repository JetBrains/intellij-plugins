// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class ServerRefactoringDialog<T extends ServerRefactoring> extends RefactoringDialog {
  @Nullable protected final Editor myEditor;
  @NotNull protected final T myRefactoring;

  private boolean myHasPendingRequests;
  private RefactoringStatus myOptionsStatus;

  public ServerRefactoringDialog(@NotNull final Project project, @Nullable final Editor editor, @NotNull final T refactoring) {
    super(project, true);
    myEditor = editor;
    myRefactoring = refactoring;
    // Listen for responses.
    myRefactoring.setListener((hasPendingRequests, optionsStatus) -> {
      final Runnable runnable = () -> {
        myHasPendingRequests = hasPendingRequests;
        myOptionsStatus = optionsStatus;
        validateButtons();
      };

      final ModalityState modalityState = ModalityState.stateForComponent(getWindow());

      final Condition<?> expired = o -> !isShowing();

      ApplicationManager.getApplication().invokeLater(runnable, modalityState, expired);
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
        Messages.showErrorDialog(myProject, finalStatus.getMessage(), CommonBundle.getErrorTitle());
        return;
      }
    }

    if (hasPreviewButton() && isPreviewUsages() || isForcePreview()) {
      previewRefactoring();
    }
    else {
      doRefactoring(myRefactoring.getPotentialEdits());
    }

    close(DialogWrapper.OK_EXIT_CODE);
  }

  @Override
  protected boolean hasPreviewButton() {
    return false;
  }

  protected boolean isForcePreview() {
    return false;
  }

  protected void previewRefactoring() {
  }

  @Override
  protected boolean hasHelpAction() {
    return getHelpId() != null;
  }

  protected final void doRefactoring(@NotNull final Set<String> excludedIds) {
    final SourceChange change = myRefactoring.getChange();
    if (change == null) {
      Logger.getInstance(ServerRefactoringDialog.class)
        .error(myRefactoring.getClass().getSimpleName() + ".getChange() == null\n" + myOptionsStatus);
      return;
    }

    final String error = WriteAction.compute(() -> {
      try {
        AssistUtils.applySourceChange(myProject, change, false, excludedIds);
      }
      catch (DartSourceEditException e) {
        return e.getMessage();
      }

      return null;
    });

    if (error == null) {
      close(DialogWrapper.OK_EXIT_CODE);
    }
    else {
      Messages.showErrorDialog(myProject, error, CommonBundle.getErrorTitle());
    }
  }
}
