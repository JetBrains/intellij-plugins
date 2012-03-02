package com.intellij.flex.uiDesigner;

import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.containers.Queue;
import com.intellij.util.ui.UIUtil;

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
    // ProgressManager requires dispatch thread
    UIUtil.invokeLaterIfNeeded(renderAction);
  }

  @Override
  public void run() {
    queue.pullFirst();
    if (!queue.isEmpty()) {
      execute(queue.peekFirst());
    }
  }

  public AsyncResult<DocumentInfo> findResult(final PsiFile file) {
    if (queue.size() == 1) {
      RenderAction action = queue.peekFirst();
      return action.file == file ? action.result : null;
    }

    final Ref<AsyncResult<DocumentInfo>> result = new Ref<AsyncResult<DocumentInfo>>();
    queue.process(new Processor<RenderAction>() {
      @Override
      public boolean process(RenderAction action) {
        if (action.file == file) {
          result.set(action.result);
          return false;
        }

        return true;
      }
    });

    return result.get();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  protected abstract static class RenderAction implements Runnable {
    protected final XmlFile file;
    protected final AsyncResult<DocumentInfo> result;

    protected RenderAction(XmlFile psiFile, AsyncResult<DocumentInfo> renderResult) {
      file = psiFile;
      result = renderResult;
    }
  }
}