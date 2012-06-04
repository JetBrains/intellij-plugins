package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class AbstractNodeWithParent<T extends AbstractJstdNode> extends AbstractJstdNode<T> {

  private final AbstractJstdNode myParent;

  public AbstractNodeWithParent(@NotNull String name, @NotNull AbstractJstdNode parent) {
    super(name, parent.getTreeManager());
    myParent = parent;
  }

  @NotNull
  public AbstractJstdNode getParent() {
    return myParent;
  }
}
