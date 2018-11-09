// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

abstract class DocumentTask extends Task.Backgroundable {
  protected static final Logger LOG = Logger.getInstance(DesignerApplicationLauncher.class.getName());

  protected ProgressIndicator indicator;
  protected final ProblemsHolder problemsHolder = new ProblemsHolder();

  protected final Module module;
  protected final PostTask postTask;

  DocumentTask(@NotNull Module module, @NotNull PostTask postTask) {
    this(module, false, postTask);
  }

  DocumentTask(@NotNull Module module, boolean debug, @NotNull PostTask postTask) {
    super(module.getProject(), DesignerApplicationManager.getOpenActionTitle(debug));

    this.module = module;
    this.postTask = postTask;
  }

  @Override
  public final void run(@NotNull ProgressIndicator indicator) {
    this.indicator = indicator;

    try {
      Throwable error = null;
      beforeRun();
      boolean result = false;
      try {
        result = doRun(indicator);
      }
      catch (ProcessCanceledException ignored) {
        // don't log ProcessCanceledException
      }
      catch (Throwable e) {
        error = e;
      }

      if (!result || indicator.isCanceled()) {
        problemsHolder.disableLog();
        processErrorOrCancel();
      }

      if (error != null) {
        try {
          LOG.error(error);
        }
        catch (AssertionError ignored) {
        }
      }
    }
    finally {
      Disposer.dispose(postTask);
    }
  }

  protected void beforeRun() {
  }

  abstract boolean doRun(ProgressIndicator indicator) throws
                                                      IOException, ExecutionException, InterruptedException,
                                                      TimeoutException;

  protected abstract void processErrorOrCancel();

  protected static void processInitException(InitException e, Module module, boolean debug) {
    DesignerApplicationManager.notifyUser(debug, e.getMessage(), module);
    if (e.attachments == null) {
      LOG.error(e.technicalMessage, e);
    }
    else {
      LOG.error(e.technicalMessage, e, e.attachments);
    }
  }

  abstract static class PostTask implements Disposable {
    abstract boolean run(Module module,
                         @Nullable ProjectComponentReferenceCounter projectComponentReferenceCounter,
                         ProgressIndicator indicator,
                         ProblemsHolder problemsHolder);

    @Override
    public void dispose() {
    }
  }
}
