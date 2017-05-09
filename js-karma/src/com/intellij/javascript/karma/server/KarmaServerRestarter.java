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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class KarmaServerRestarter {

  private final AtomicInteger myActiveRunners = new AtomicInteger(0);
  private final AtomicBoolean myConfigChanged = new AtomicBoolean(false);
  private boolean myRestartRequested = false;

  public KarmaServerRestarter(@NotNull File configurationFile, @NotNull Disposable parentDisposable) {
    listenForConfigurationFileChanges(configurationFile, parentDisposable);
  }

  private void listenForConfigurationFileChanges(@NotNull final File configurationFile,
                                                 @NotNull final Disposable parentDisposable) {
    ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(configurationFile);
      if (virtualFile != null && virtualFile.isValid() && !virtualFile.isDirectory()) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document != null && !Disposer.isDisposed(parentDisposable)) {
          document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
              myConfigChanged.set(true);
            }
          }, parentDisposable);
        }
      }
    }), ModalityState.any());
  }

  public void onRunnerExecutionStarted(@NotNull final OSProcessHandler processHandler) {
    myActiveRunners.incrementAndGet();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        myActiveRunners.decrementAndGet();
        processHandler.removeProcessListener(this);
      }
    });
  }

  public boolean isRestartRequired() {
    return myActiveRunners.get() == 0 && (myRestartRequested || myConfigChanged.get());
  }

  public void requestRestart() {
    myRestartRequested = true;
  }

}
