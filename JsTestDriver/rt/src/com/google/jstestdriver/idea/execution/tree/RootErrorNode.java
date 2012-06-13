package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class RootErrorNode extends AbstractNodeWithParent<RootErrorNode> {
  public RootErrorNode(@NotNull RootNode parent) {
    super("Error", parent);
  }
}
