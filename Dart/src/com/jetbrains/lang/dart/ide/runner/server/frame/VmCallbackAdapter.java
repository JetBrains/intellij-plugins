package com.jetbrains.lang.dart.ide.runner.server.frame;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.jetbrains.lang.dart.ide.runner.server.google.VmCallback;
import com.jetbrains.lang.dart.ide.runner.server.google.VmResult;
import org.jetbrains.annotations.NotNull;

abstract class VmCallbackAdapter<T> implements VmCallback<T> {
  @NotNull private final XCompositeNode myNode;

  VmCallbackAdapter(@NotNull final XCompositeNode node) {
    myNode = node;
  }

  @Override
  public void handleResult(final VmResult<T> result) {
    if (!myNode.isObsolete()) {
      if (result.isError()) {
        myNode.setErrorMessage(result.getError());
      }
      else if (result.getResult() == null) {
        myNode.setErrorMessage("<no response from the Dart VM>");
      }
      else {
        handleGoodResult(result.getResult());
      }
    }
  }

  protected abstract void handleGoodResult(@NotNull final T result);
}
