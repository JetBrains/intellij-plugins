package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class AbstractSuiteNode<T extends AbstractNodeWithParent> extends AbstractNodeWithParent<T> {
  public AbstractSuiteNode(@NotNull String name, @NotNull AbstractNode parent) {
    super(name, parent);
  }
}
