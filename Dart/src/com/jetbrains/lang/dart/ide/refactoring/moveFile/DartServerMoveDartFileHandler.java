// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring.moveFile;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public final class DartServerMoveDartFileHandler extends MoveFileHandler {

  @Override
  public boolean canProcessElement(PsiFile psiFile) {
    if (!(psiFile instanceof DartFile)) {
      return false;
    }
    final Project project = psiFile.getProject();
    final DartSdk dartSdk = DartSdk.getDartSdk(project);
    if (dartSdk == null || !DartAnalysisServerService.isDartSdkVersionSufficientForMoveFileRefactoring(dartSdk)) {
      return false;
    }
    return DartAnalysisServerService.getInstance(project).isInIncludedRoots(psiFile.getVirtualFile());
  }

  @Override
  public void prepareMovedFile(PsiFile psiFile, PsiDirectory moveDestination, Map<PsiElement, PsiElement> oldToNewMap) {
    final Project project = psiFile.getProject();
    final VirtualFile virtualFile = psiFile.getVirtualFile();

    final String newFilePath = moveDestination.getVirtualFile().getPath() + "/" + virtualFile.getName();
    final MoveFileRefactoring refactoring = new MoveFileRefactoring(project, virtualFile, newFilePath);

    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }

      if (initialStatus.hasError()) {
        final String message = initialStatus.getMessage();
        assert message != null;
        return;
      }

      // validate final status
      {
        final RefactoringStatus finalConditions = refactoring.checkFinalConditions();
        if (finalConditions != null && finalConditions.isOK()) {
          SourceChange change = refactoring.getChange();
          assert change != null;
          try {
            AssistUtils.applySourceChange(project, change, false);
          }
          catch (DartSourceEditException exception) {
            // Exception when trying to apply the creating the refactoring
            showMoveFileExceptionDialog(project, DartBundle.message("dart.refactoring.move.file.dialog.error.applying.change"));
          }
        }
        else {
          // Exception when creating the refactoring
          String message = StringUtil.notNullize(finalConditions == null ? null : finalConditions.getMessage());
          showMoveFileExceptionDialog(project, DartBundle.message("dart.refactoring.move.file.dialog.error.computing.change", message));
        }
      }
    }
  }

  @Override
  public @Nullable @Unmodifiable List<UsageInfo> findUsages(@NotNull PsiFile psiFile, @NotNull PsiDirectory newParent, boolean searchInComments, boolean searchInNonJavaFiles) {
    return null;
  }

  @Override
  public void retargetUsages(@Unmodifiable @NotNull List<? extends UsageInfo> usageInfos, @NotNull Map<PsiElement, PsiElement> oldToNewMap) {
  }

  @Override
  public void updateMovedFile(PsiFile psiFile) throws IncorrectOperationException {
    // This is not used as the file move has already happened in the framework by this point meaning that the file changes described by
    // the Dart Analysis Server would now be pointing to incorrect file paths if we tried to use them at this point.
  }

  private static void showMoveFileExceptionDialog(final @NotNull Project project, @NotNull @NlsContexts.DialogMessage String message) {
    ApplicationManager.getApplication()
      .invokeLater(() -> Messages.showErrorDialog(project, message, DartBundle.message("dart.refactoring.move.file.dialog.title")));
  }
}
