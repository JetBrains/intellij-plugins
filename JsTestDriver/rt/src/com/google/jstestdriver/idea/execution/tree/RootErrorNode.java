package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class RootErrorNode extends AbstractNodeWithParent<RootErrorNode> {
  public RootErrorNode(@NotNull RootNode parent) {
    super("Error", parent);
  }

  @Override
  public String getProtocolId() {
    return null;
  }

  @Override
  public String getLocationPath() {
    return null;
  }
}
