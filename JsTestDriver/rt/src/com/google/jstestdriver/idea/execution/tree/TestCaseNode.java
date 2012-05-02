package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TestCaseNode extends AbstractJstdNode<TestNode> {
  public TestCaseNode(@NotNull String name, @NotNull BrowserNode parent) {
    super(name, parent);
  }
}
