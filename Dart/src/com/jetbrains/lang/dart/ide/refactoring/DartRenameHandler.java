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

import com.intellij.ide.TitledHandler;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameHandler;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartNamedElement;
import com.jetbrains.lang.dart.util.DartElementLocation;
import org.jetbrains.annotations.NotNull;

public class DartRenameHandler implements RenameHandler, TitledHandler {
  @Override
  public String getActionTitle() {
    return "Dart Rename Refactoring";
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext context) {
    showRenameDialog(project, editor, context);
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext context) {
    showRenameDialog(project, null, context);
  }

  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    if (element instanceof DartNamedElement) {
      return true;
    }
    if (element instanceof DartComponent) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  private static void showRenameDialog(@NotNull Project project, Editor editor, DataContext context) {
    final PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(context);
    if (element == null) {
      return;
    }

    final DartElementLocation elementLocation = DartElementLocation.of(element);
    final ServerRenameRefactoring refactoring = new ServerRenameRefactoring(elementLocation.file, elementLocation.offset, 0);

    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }
      if (initialStatus.hasError()) {
        Messages.showErrorDialog(project, initialStatus.getMessage(), "Error");
        return;
      }
    }

    RenameDialog.showRenameDialog(context, new DartRenameDialog(project, element, editor, refactoring));
  }
}

class DartRenameDialog extends RenameDialog {
  private final ServerRenameRefactoring myRefactoring;
  private boolean myHasPendingRequests;
  private RefactoringStatus myOptionsStatus;

  public DartRenameDialog(@NotNull Project project,
                          @NotNull PsiElement element,
                          Editor editor,
                          @NotNull ServerRenameRefactoring refactoring) {
    super(project, element, null, editor);
    myRefactoring = refactoring;
    // Listen for responses.
    myRefactoring.setListener(new ServerRefactoring.ServerRefactoringListener() {
      @Override
      public void requestStateChanged(final boolean hasPendingRequests, final RefactoringStatus optionsStatus) {
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
    if (myRefactoring == null) return;
    // the same name
    if (Comparing.strEqual(getNewName(), myRefactoring.getOldName())) {
      throw new ConfigurationException(null);
    }
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
        AssistUtils.applySourceChange(myProject, myRefactoring.getChange());
        close(DialogWrapper.OK_EXIT_CODE);
      }
    });
  }

  @Override
  protected void processNewNameChanged() {
    final String newName = getNewName();
    myRefactoring.setNewName(newName);
    super.processNewNameChanged();
  }
}