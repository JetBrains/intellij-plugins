package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractSuiteNode<T extends AbstractNodeWithParent> extends AbstractNodeWithParent<T> {
  public AbstractSuiteNode(@NotNull String name, @NotNull AbstractNode parent) {
    super(name, parent);
  }

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    return TC.newTestSuiteStartedMessage(this);
  }
}
