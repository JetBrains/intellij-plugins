package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class ConfigNode extends AbstractSuiteNode<BrowserNode> {
  public ConfigNode(@NotNull String name, @NotNull RootNode parent) {
    super(name, parent);
  }
}
