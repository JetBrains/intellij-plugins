// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.fixes;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartFileInfo;
import com.jetbrains.lang.dart.analyzer.DartFileInfoKt;
import com.jetbrains.lang.dart.analyzer.DartLocalFileInfo;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class DartQuickFix implements IntentionAction, Comparable<IntentionAction> {

  @NotNull private final DartQuickFixSet myQuickFixSet;
  private final int myIndex;
  @Nullable private SourceChange mySourceChange;

  public DartQuickFix(@NotNull final DartQuickFixSet quickFixSet, final int index) {
    myIndex = index;
    myQuickFixSet = quickFixSet;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return DartBundle.message("intention.family.name");
  }

  @NotNull
  @Override
  public String getText() {
    myQuickFixSet.ensureInitialized();

    if (mySourceChange == null) return "";

    @NlsSafe String message = mySourceChange.getMessage();
    return message;
  }

  @Override
  public int compareTo(IntentionAction o) {
    if (o instanceof DartQuickFix) {
      return myIndex - ((DartQuickFix)o).myIndex;
    }
    return 0;
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if (editor == null || file == null) {
      // not sure this can ever happen
      return;
    }

    if (mySourceChange != null) {
      if (!file.isPhysical() && !ApplicationManager.getApplication().isWriteAccessAllowed()) {
        doInvokeForPreview(file, mySourceChange);
        return;
      }

      doInvoke(project, editor, file, mySourceChange, this);
    }
  }

  public static void doInvoke(@NotNull Project project,
                              @NotNull Editor editor,
                              @NotNull PsiFile file,
                              @NotNull SourceChange sourceChange,
                              @Nullable DartQuickFix dartQuickFix) {
    SourceFileEdit fileEdit = sourceChange.getEdits().get(0);
    String filePathOrUri = fileEdit.getFile();
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(project, filePathOrUri);
    if (!(fileInfo instanceof DartLocalFileInfo localFileInfo)) return;

    String filePath = localFileInfo.getFilePath();
    final VirtualFile virtualFile;

    // Create the file if it does not exist.
    if (fileEdit.getFileStamp() == -1) {
      try {
        final String directoryPath = PathUtil.getParentPath(filePath);
        if (directoryPath.isEmpty()) throw new IOException("empty folder path");

        final VirtualFile directory = VfsUtil.createDirectoryIfMissing(directoryPath);
        if (directory == null) throw new IOException("failed to create folder " + FileUtil.toSystemDependentName(directoryPath));

        virtualFile = directory.createChildData(sourceChange, PathUtil.getFileName(filePath));
      }
      catch (IOException e) {
        final String message = DartBundle.message("failed.to.create.file.0.1", FileUtil.toSystemDependentName(filePath), e.getMessage());
        CommonRefactoringUtil.showErrorHint(project, editor, message, CommonBundle.getErrorTitle(), null);
        return;
      }
    }
    else {
      virtualFile = localFileInfo.findFile();
    }

    if (virtualFile == null) return;

    if (!FileModificationService.getInstance().prepareVirtualFilesForWrite(project, Collections.singletonList(virtualFile))) return;

    final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) return;

    if (dartQuickFix != null) {
      DartAnalysisServerService.getInstance(project).fireBeforeQuickFixInvoked(dartQuickFix, editor, file);
    }

    try {
      AssistUtils.applySourceChange(project, sourceChange, true);
    }
    catch (DartSourceEditException e) {
      CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
    }
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    if (editor == null || file == null) {
      // not sure this can ever happen
      return false;
    }

    myQuickFixSet.ensureInitialized();

    return mySourceChange != null && isAvailable(project, mySourceChange);
  }

  public static boolean isAvailable(@NotNull Project project, @NotNull SourceChange sourceChange) {
    final List<SourceFileEdit> fileEdits = sourceChange.getEdits();
    if (fileEdits.isEmpty()) {
      return false;
    }

    for (SourceFileEdit fileEdit : fileEdits) {
      final VirtualFile virtualFile = AssistUtils.findVirtualFile(project, fileEdit);
      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

      if (fileEdit.getFileStamp() != -1) {
        if (virtualFile == null || !fileIndex.isInContent(virtualFile)) return false;
      }
    }

    return true;
  }

  void setSourceChange(@Nullable final SourceChange sourceChange) {
    mySourceChange = sourceChange;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  @Override
  public @Nullable FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
    return isPreviewAvailable(target, mySourceChange) ? this : null;
  }

  public static boolean isPreviewAvailable(@NotNull PsiFile target, @Nullable SourceChange sourceChange) {
    if (sourceChange == null) {
      return false;
    }

    String filePathOrUri = sourceChange.getEdits().get(0).getFile();
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(target.getProject(), filePathOrUri);

    VirtualFile vFile = target.getOriginalFile().getVirtualFile();
    return vFile != null && fileInfo instanceof DartLocalFileInfo localFileInfo && localFileInfo.getFilePath().equals(vFile.getPath());
  }

  public static void doInvokeForPreview(@NotNull PsiFile psiFile, @NotNull SourceChange sourceChange) {
    assert !psiFile.isPhysical() &&
           !ApplicationManager.getApplication().isWriteAccessAllowed() &&
           isPreviewAvailable(psiFile, sourceChange);

    // #isPreviewAvailable() has checked that sourceChange.getEdits().get(0) modifies _this_ PsiFile, not some other
    Document document = psiFile.getViewProvider().getDocument();
    for (SourceEdit edit : sourceChange.getEdits().get(0).getEdits()) {
      String replacement = StringUtil.convertLineSeparators(edit.getReplacement());
      int startOffset = edit.getOffset();
      int endOffset = edit.getOffset() + edit.getLength();
      if (endOffset > document.getTextLength()) {
        return; // IDEA-345982
      }
      document.replaceString(startOffset, endOffset, replacement);
    }
  }
}
