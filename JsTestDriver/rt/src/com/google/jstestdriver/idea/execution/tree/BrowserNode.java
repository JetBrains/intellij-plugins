package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class BrowserNode extends AbstractJstdNode<TestCaseNode> {
  public BrowserNode(@NotNull String name, @NotNull RootNode parent) {
    super(name, parent);
  }
}
