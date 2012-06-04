package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class AbstractSuiteNode<T extends AbstractJstdNode> extends AbstractNodeWithParent<T> {
  public AbstractSuiteNode(@NotNull String name, @NotNull AbstractJstdNode parent) {
    super(name, parent);
  }
}
