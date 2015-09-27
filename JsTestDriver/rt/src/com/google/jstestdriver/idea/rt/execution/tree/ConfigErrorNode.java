package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.jstestdriver.idea.rt.execution.tc.TC;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class ConfigErrorNode extends AbstractNodeWithParent<ConfigErrorNode> {
  public ConfigErrorNode(@NotNull ConfigNode parent) {
    super("Error", parent);
  }

  @NotNull
  @Override
  public ConfigNode getParent() {
    return (ConfigNode) super.getParent();
  }

  @Override
  public String getProtocolId() {
    return getParent().getProtocolId();
  }

  @Override
  public String getLocationPath() {
    return getParent().getLocationPath();
  }

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    return TC.newConfigErrorStartedMessage(this);
  }
}
