// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PrettierFileDocumentManagerListener implements FileDocumentManagerListener {
  private static final ReformatWithPrettierAction.ErrorHandler ERROR_HANDLER = new ReformatWithPrettierAction.ErrorHandler() {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull String text, @Nullable Runnable onLinkClick) {
    }
  };

  private Set<Document> myDocumentsToProcess = new THashSet<>();

  private boolean mySavingFormattedFiles;

  @Override
  public void beforeAllDocumentsSaving() {
    if (mySavingFormattedFiles) return;

    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      PrettierConfiguration prettierConfiguration = PrettierConfiguration.getInstance(project);
      if (prettierConfiguration.isRunOnSave()) {
        scheduleDocumentProcessing(FileDocumentManager.getInstance().getUnsavedDocuments());
        return;
      }
    }
  }

  private void scheduleDocumentProcessing(Document[] documents) {
    boolean processingAlreadyScheduled = !myDocumentsToProcess.isEmpty();

    myDocumentsToProcess.addAll(Arrays.asList(documents));

    if (!processingAlreadyScheduled) {
      // invokeLater() is required because beforeAllDocumentsSaving() runs in write action but Prettier formatter must be called not in write action
      // don't pass any disposable to invokeLater() here, we must clear myDocumentsToProcess.
      ApplicationManager.getApplication().invokeLater(() -> processSavedDocuments(), ModalityState.NON_MODAL);
    }
  }

  private static boolean shouldBeFormattedWithPrettier(@NotNull VirtualFile file) {
    String path = file.getPath();
    return path.endsWith(".js") || path.endsWith(".ts");
  }

  private void processSavedDocuments() {
    Document[] documents = myDocumentsToProcess.toArray(Document.EMPTY_ARRAY);
    myDocumentsToProcess.clear();

    // Although invokeLater() is called with ModalityState.NON_MODAL argument, somehow this might be called in modal context (for example on Commit File action)
    // It's quite weird if Prettier progress appears or documents are changed is modal context, let's ignore the request.
    if (ModalityState.current() != ModalityState.NON_MODAL) return;

    FileDocumentManager manager = FileDocumentManager.getInstance();
    List<VirtualFile> files = ContainerUtil.mapNotNull(documents, document -> {
      VirtualFile file = manager.getFile(document);
      return file != null && file.isInLocalFileSystem() ? file : null;
    });

    if (files.isEmpty()) return;

    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      PrettierConfiguration prettierConfiguration = PrettierConfiguration.getInstance(project);
      if (!prettierConfiguration.isRunOnSave()) continue;

      List<VirtualFile> filesToProcess = ContainerUtil.filter(files, file -> shouldBeFormattedWithPrettier(file));
      if (!filesToProcess.isEmpty()) {
        ReformatWithPrettierAction.processVirtualFiles(project, filesToProcess, ERROR_HANDLER);
      }
    }

    mySavingFormattedFiles = true;
    try {
      for (Document document : documents) {
        manager.saveDocument(document);
      }
    }
    finally {
      mySavingFormattedFiles = false;
    }
  }
}
