package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.jstestdriver.idea.rt.execution.tc.TCAttribute;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class BrowserNode extends AbstractSuiteNode<TestCaseNode> {
  public BrowserNode(@NotNull String browserName, @NotNull ConfigNode parent) {
    super(browserName, parent);
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
  public ConfigNode getParent() {
    return (ConfigNode) super.getParent();
  }

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    TCMessage message = super.createStartedMessage();
    String basePath = getParent().getAbsoluteBasePath();
    if (basePath != null) {
      message.addAttribute(TCAttribute.NODE_TYPE, "browser");
      message.addAttribute(TCAttribute.NODE_ARGS, basePath);
    }
    return message;
  }
}
