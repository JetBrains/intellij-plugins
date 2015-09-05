package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import com.intellij.util.containers.Queue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

class RenderActionQueue implements Runnable {
  private final Queue<RenderAction> queue = new Queue<RenderAction>(4);
  private boolean suspended;
  // pending due to suspend
  private boolean wasPending;

  public void suspend() {
    suspended = true;
  }

  public void resume() {
    suspended = false;
    if (wasPending) {
      wasPending = false;
      execute(queue.peekFirst());
    }
  }

  public void add(RenderAction renderAction) {
    queue.addLast(renderAction);
    if (queue.size() == 1) {
      execute(renderAction);
    }
  }

  private void execute(RenderAction renderAction) {
    if (suspended) {
      assert !wasPending;
      wasPending = true;
      return;
    }

    renderAction.result.doWhenProcessed(this);
    Application application = ApplicationManager.getApplication();
    boolean isDispatchThread = application.isDispatchThread();
    if (renderAction.isNeedEdt()) {
      if (isDispatchThread) {
        renderAction.run();
      }
      else {
        application.invokeLater(renderAction);
      }
    }
    else {
      if (isDispatchThread) {
        application.executeOnPooledThread(renderAction);
      }
      else {
        renderAction.run();
      }
    }
  }

  @Override
  public void run() {
    queue.pullFirst();
    if (!queue.isEmpty()) {
      execute(queue.peekFirst());
    }
  }

  public void processActions(Processor<RenderAction> processor) {
    synchronized (queue) {
      queue.process(processor);
    }
  }

  public AsyncResult<DocumentInfo> findResult(PsiFile psiFile) {
   return findResult(psiFile.getVirtualFile());
  }

  @SuppressWarnings("unchecked")
  private <T extends AsyncResult> T findResult(@Nullable final VirtualFile file) {
    if (queue.size() == 1) {
      RenderAction action = queue.peekFirst();
      return Comparing.equal(action.file, file) ? (T)action.result : null;
    }

    final Ref<AsyncResult> result = new Ref<AsyncResult>();
    processActions(new Processor<RenderAction>() {
      @Override
      public boolean process(RenderAction action) {
        if (Comparing.equal(action.file, file)) {
          result.set(action.result);
          return false;
        }

        return true;
      }
    });

    return (T)result.get();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  protected abstract static class RenderAction<T extends AsyncResult> implements Runnable {
    protected final VirtualFile file;
    protected final Project project;
    protected final T result;

    protected RenderAction(@Nullable Project project, @Nullable VirtualFile file, @NotNull T renderResult) {
      this.project = project;
      this.file = file;
      result = renderResult;
    }

    abstract protected boolean isNeedEdt();

    @Override
    public final void run() {
      ComponentManager disposable = project == null ? ApplicationManager.getApplication() : project;
      if (disposable == null || disposable.isDisposed()) {
       result.setRejected();
      }
      else {
        doRun();
      }
    }

    protected abstract void doRun();
  }
}