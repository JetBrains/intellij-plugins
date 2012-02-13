package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class DocumentTaskExecutor extends DocumentTask {
  public DocumentTaskExecutor(@NotNull final Module module, @NotNull final PostTask postTask) {
    super(module, postTask);
  }

  @Override
  boolean doRun(ProgressIndicator indicator) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final ProjectComponentReferenceCounter projectComponentReferenceCounter;
    try {
      projectComponentReferenceCounter = LibraryManager.getInstance().initLibrarySets(module, problemsHolder);
    }
    catch (InitException e) {
      processInitException(e, module, false);
      return false;
    }

    return postTask.run(module, projectComponentReferenceCounter, indicator, problemsHolder);
  }

  @Override
  protected void processErrorOrCancel() {
  }
}
