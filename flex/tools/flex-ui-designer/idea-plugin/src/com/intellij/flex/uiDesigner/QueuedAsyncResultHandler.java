package com.intellij.flex.uiDesigner;

import com.intellij.util.Consumer;

public abstract class QueuedAsyncResultHandler<T> implements Consumer<T> {
  protected abstract boolean isExpired();
  protected abstract void process(T t);

  @Override
  public final void consume(T t) {
    if (!isExpired()) {
      process(t);
    }
  }
}
