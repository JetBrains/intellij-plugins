package com.jetbrains.lang.dart.fixes;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.assists.AssistUtils;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class DartQuickFix implements IntentionAction {
  @NotNull private final DartQuickFixSet myQuickFixSet;
  @Nullable private SourceChange mySourceChange;

  public DartQuickFix(@NotNull final DartQuickFixSet quickFixSet) {
    myQuickFixSet = quickFixSet;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @NotNull
  @Override
  public String getText() {
    myQuickFixSet.ensureInitialized();
    return mySourceChange == null ? "" : mySourceChange.getMessage();
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if (mySourceChange == null) return;

    final SourceFileEdit fileEdit = mySourceChange.getEdits().get(0);
    final String filePath = FileUtil.toSystemIndependentName(fileEdit.getFile());

    final VirtualFile virtualFile;

    // Create the file if it does not exist.
    if (fileEdit.getFileStamp() == -1) {
      try {
        final String directoryPath = PathUtil.getParentPath(filePath);
        if (directoryPath.isEmpty()) throw new IOException("empty folder path");

        final VirtualFile directory = VfsUtil.createDirectoryIfMissing(directoryPath);
        if (directory == null) throw new IOException("failed to create folder " + FileUtil.toSystemDependentName(directoryPath));

        virtualFile = directory.createChildData(this, PathUtil.getFileName(filePath));
      }
      catch (IOException e) {
        final String message = DartBundle.message("failed.to.create.file.0.1", FileUtil.toSystemDependentName(filePath), e.getMessage());
        CommonRefactoringUtil.showErrorHint(project, editor, message, CommonBundle.getErrorTitle(), null);
        return;
      }
    }
    else {
      virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    if (virtualFile == null) return;

    if (!FileModificationService.getInstance().prepareVirtualFilesForWrite(project, Collections.singletonList(virtualFile))) return;

    final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) return;

    AssistUtils.applySourceChange(project, mySourceChange);
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    if (myQuickFixSet.getPsiModificationCount() != file.getManager().getModificationTracker().getModificationCount()) return false;

    myQuickFixSet.ensureInitialized();
    if (mySourceChange == null) return false;

    final List<SourceFileEdit> fileEdits = mySourceChange.getEdits();
    if (fileEdits.size() != 1) return false;

    final SourceFileEdit fileEdit = fileEdits.get(0);
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(fileEdit.getFile()));
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    if (fileEdit.getFileStamp() != -1) {
      if (virtualFile == null || !fileIndex.isInContent(virtualFile)) return false;
    }

    return true;
  }

  public void setSourceChange(@NotNull final SourceChange sourceChange) {
    mySourceChange = sourceChange;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
