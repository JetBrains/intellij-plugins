package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.jstestdriver.idea.rt.execution.tc.TC;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
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

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    return TC.newRootErrorStartedMessage(this);
  }
}
