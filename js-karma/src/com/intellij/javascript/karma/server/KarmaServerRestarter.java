// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.server;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.CheckedDisposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.NioFiles;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class KarmaServerRestarter {

  private final AtomicInteger myActiveRunners = new AtomicInteger(0);
  private final AtomicBoolean myConfigChanged = new AtomicBoolean(false);

  public KarmaServerRestarter(@NotNull String configurationFilePath, @NotNull Disposable parentDisposable) {
    Path configurationFile = NioFiles.toPath(configurationFilePath);
    if (configurationFile != null && Files.isRegularFile(configurationFile)) {
      listenForConfigurationFileChanges(configurationFile, parentDisposable);
    }
  }

  private void listenForConfigurationFileChanges(@NotNull Path configurationFile,
                                                 @NotNull Disposable parentDisposable) {
    CheckedDisposable checkedDisposable = Disposer.newCheckedDisposable(parentDisposable);
    ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(configurationFile);
      if (virtualFile != null && virtualFile.isValid() && !virtualFile.isDirectory()) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document != null && !checkedDisposable.isDisposed()) {
          document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent e) {
              myConfigChanged.set(true);
            }
          }, parentDisposable);
        }
      }
    }), ModalityState.any());
  }

  public void onRunnerExecutionStarted(final @NotNull OSProcessHandler processHandler) {
    myActiveRunners.incrementAndGet();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        myActiveRunners.decrementAndGet();
        processHandler.removeProcessListener(this);
      }
    });
  }

  public boolean isRestartRequired() {
    return myActiveRunners.get() == 0 && myConfigChanged.get();
  }
}
