package com.intellij.flex.uiDesigner;

import com.intellij.diagnostic.LogMessageEx;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.libraries.InitException;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

abstract class DocumentTask extends Task.Backgroundable {
  protected static final Logger LOG = Logger.getInstance(DesignerApplicationLauncher.class.getName());

  protected ProgressIndicator indicator;
  protected final ProblemsHolder problemsHolder = new ProblemsHolder();

  protected final Module module;
  protected final PostTask postTask;

  public DocumentTask(@NotNull final Module module, @NotNull final PostTask postTask) {
    this(module, false, postTask);
  }

  public DocumentTask(@NotNull final Module module, final boolean debug, @NotNull final PostTask postTask) {
    super(module.getProject(), DesignerApplicationManager.getOpenActionTitle(debug));

    this.module = module;
    this.postTask = postTask;
  }

  @Override
  public final void run(@NotNull final ProgressIndicator indicator) {
    this.indicator = indicator;

    try {
      Throwable error = null;

      beforeRun();

      boolean result = false;
      try {
        result = doRun(indicator);
      }
      catch (ProcessCanceledException ignored) {
        // Don't log ProcessCanceledException
      }
      catch (Throwable e) {
        error = e;
      }

      if (!result || indicator.isCanceled()) {
        problemsHolder.disableLog();

        processErrorOrCancel();
      }

      if (error != null) {
        LOG.error(error);
      }
    }
    finally {
      Disposer.dispose(postTask);
    }
  }

  protected void beforeRun() {
  }

  abstract boolean doRun(ProgressIndicator indicator) throws
                                                      IOException, java.util.concurrent.ExecutionException, InterruptedException,
                                                      TimeoutException;

  protected abstract void processErrorOrCancel();

  protected static void processInitException(InitException e, Module module, boolean debug) {
    DesignerApplicationManager.notifyUser(debug, e.getMessage(), module);
    if (e.attachments == null) {
      LOG.error(e.getCause());
    }
    else {
      final Collection<Attachment> attachments = new ArrayList<Attachment>(e.attachments.length);
      for (Attachment attachment : e.attachments) {
        if (attachment != null) {
          attachments.add(attachment);
        }
        else {
          break;
        }
      }

      LOG.error(LogMessageEx.createEvent(e.getMessage(), e.technicalMessage + "\n" + ExceptionUtil.getThrowableText(e), e.getMessage(),
                                         null, attachments));
    }
  }

  abstract static class PostTask implements Disposable {
    abstract boolean run(Module module,
                         ProjectComponentReferenceCounter projectComponentReferenceCounter,
                         ProgressIndicator indicator,
                         ProblemsHolder problemsHolder);

    @Override
    public void dispose() {
    }
  }
}
