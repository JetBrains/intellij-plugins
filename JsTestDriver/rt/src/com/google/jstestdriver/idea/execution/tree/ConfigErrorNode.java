package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class ConfigErrorNode extends AbstractNodeWithParent<ConfigErrorNode> {
  public ConfigErrorNode(@NotNull ConfigNode parent) {
    super("Error", parent);
  }
}
