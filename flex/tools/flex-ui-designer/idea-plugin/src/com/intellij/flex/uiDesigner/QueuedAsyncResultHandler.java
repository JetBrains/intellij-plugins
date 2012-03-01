package com.intellij.flex.uiDesigner;

import com.intellij.openapi.util.AsyncResult;

public abstract class QueuedAsyncResultHandler<T> implements AsyncResult.Handler<T> {
  protected abstract boolean isExpired();
  protected abstract void process(T t);

  @Override
  public final void run(T t) {
    if (!isExpired()) {
      process(t);
    }
  }
}
